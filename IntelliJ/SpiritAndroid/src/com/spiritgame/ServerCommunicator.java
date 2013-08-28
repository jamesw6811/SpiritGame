package com.spiritgame;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.Key;
import java.util.Iterator;
import java.util.LinkedList;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLSocket;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import spiritshared.LoginInfo;
import spiritshared.constants;

/**
 * @author jamesw
 */
public class ServerCommunicator implements Runnable {

    private boolean running;
    private Socket clientSocket = null;
    private SSLSocket cryptSocket = null;
    private DataOutputStream outToServer = null;
    private DataInputStream inFromServer = null;
    private DataOutputStream cryptoutToServer = null;
    private DataInputStream cryptinFromServer = null;
    private SpiritClient client = null;
    private LinkedList<Integer> toSend = null;
    private LinkedList<String> chatSend = null;
    private int keepAliveCount;
    private LoginInfo login;
    private boolean loggedin = false;
	private byte bytesToSend[];

    public ServerCommunicator(SpiritClient c, LoginInfo li) {
        running = true;
        client = c;
        toSend = new LinkedList<Integer>();
        chatSend = new LinkedList<String>();
        keepAliveCount = 0;
        login = li;
        try {
			System.out.println("ServerConnecting");
            clientSocket = new Socket("ec2-107-21-140-50.compute-1.amazonaws.com", 6789);
            //cryptSocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket("ec2-107-21-140-50.compute-1.amazonaws.com", 6788);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new DataInputStream(clientSocket.getInputStream());
            //cryptoutToServer = new DataOutputStream(cryptSocket.getOutputStream());
            //cryptinFromServer = new DataInputStream(cryptSocket.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            sendLogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (running) {
            try {
                keepAliveCount++;
                if (keepAliveCount > 100) {
                    sendKeepAlive();
                }
                readServer();
                sendServer();
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
                running = false;
            }
        }
    }

    public void destroy() {
		try {
		            if(clientSocket!=null)clientSocket.close();
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
        running = false;
    }

    private void sendLogin() throws Exception {
        Key key = new SecretKeySpec(constants.key, "AES");
        byte[] dataToSend = login.password;
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedData = c.doFinal(dataToSend);
        outToServer.writeUTF(login.username);
        outToServer.writeInt(encryptedData.length);
        outToServer.write(encryptedData);
        System.out.println("login:" + login.username + " passenc:" + new String(encryptedData));
    }

    public void chat(String s) {
        synchronized (chatSend) {
            chatSend.add(s);
        }

    }

    public boolean loggedin() {
        return loggedin;
    }

    private void readServer() throws Exception {
        while (inFromServer.available() > 0) {
            loggedin = true;
            long ID = inFromServer.readLong();
            if (ID == constants.VIEW_CHAT_SIGNAL) {
                int chatID = inFromServer.readInt();
                int[] chatpos = new int[2];
                chatpos[0] = inFromServer.readInt();
                chatpos[1] = inFromServer.readInt();
                String chat = "";
                char s = inFromServer.readChar();
                while (s != '\n') {
                    chat += s;
                    s = inFromServer.readChar();
                }
                client.doChat(chatID, chatpos, chat);
            } else if (ID == constants.VIEW_REMOVE_ENTITY) {
                long removeID = inFromServer.readLong();
                client.removeEntity(removeID);
			} else if (ID == constants.SERVERTOCLIENT_TEXTURE) {
				long textID = inFromServer.readLong();
				int size = inFromServer.readInt();
				byte[] textbytes = new byte[size];
				inFromServer.read(textbytes,0, size);
				BitmapFactory.decodeByteArray(textbytes,0,size);

            } else {
                int[] attr = new int[3];
                attr[0] = inFromServer.readInt();
                attr[1] = inFromServer.readInt();
                attr[2] = inFromServer.readInt();
                //System.out.println(" " + attr[0] + ", " + attr[1] + "," + attr[2]);
                client.updateEntity(ID, attr);
            }
        }
    }
	protected void setProfile(Bitmap bitmap){
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		sendBytes(stream.toByteArray());
	}
	private void sendBytes(byte[] array){
		synchronized (toSend) {
			bytesToSend = array;
			toSend.add(constants.CLIENTTOSERVER_TEXTURE);
		}
	}

    private void sendKeepAlive() {
        synchronized (toSend) {
            toSend.add(constants.COM_TYPE_KEEP_ALIVE);
        }
        keepAliveCount = 0;
    }

    private void sendServer() throws Exception {
        LinkedList<Integer> readysend = null;
        LinkedList<String> readysendchat = null;
        //System.out.println("toSendsize:"+toSend.size());
        synchronized (toSend) {
            readysend = (LinkedList<Integer>) toSend.clone();
            toSend.clear();
        }
        //System.out.println("readySendsize:"+readysend.size());
        Iterator<Integer> it = readysend.iterator();
        while (it.hasNext()) {
			int i = it.next();
			if(i==constants.CLIENTTOSERVER_TEXTURE){
				System.out.println("SeverCommunicator:CLIENTTOSERVER");
				outToServer.writeInt(constants.CLIENTTOSERVER_TEXTURE);
				outToServer.writeInt(bytesToSend.length);
				outToServer.write(bytesToSend);
			}else outToServer.writeInt(i);
        }

        synchronized (chatSend) {
            readysendchat = (LinkedList<String>) chatSend.clone();
            chatSend.clear();
        }
        //System.out.println("readySendsize:"+readysend.size());
        Iterator<String> its = readysendchat.iterator();
        while (its.hasNext()) {
            outToServer.writeInt(-2);
            outToServer.writeChars(its.next());
            outToServer.writeChar('\n');
        }
    }

    public void move(int x, int y) {
        synchronized (toSend) {
            toSend.add(1);
            toSend.add(x);
            toSend.add(y);
        }
    }

    public void mouseSelect(int x, int y) {
        synchronized (toSend) {
            toSend.add(constants.MOUSE_SELECT_LOC);
            toSend.add(x);
            toSend.add(y);
        }
    }

    public void doAction(int x) {
        synchronized (toSend) {
            toSend.add(x);
        }
    }
}

