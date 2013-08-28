/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

import spiritshared.LoginInfo;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.security.Key;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import spiritshared.constants;

/**
 *
 * @author jamesw
 */
public class Client {

    private Socket connection;
    private ControlledEntity control;
    private DataInputStream inFromClient;
    private DataOutputStream outToClient;
    private int checkAliveCount = 0;
    private long[] viewOffset;
    private String username;
	private byte[] textureBytes = null;

	public byte[] getTextureBytes()
	{
		return textureBytes;
	}

	public void setTextureBytes(byte[] textureBytes)
	{
		this.textureBytes = textureBytes;
	}


    public Client(Socket connect) {
        connection = connect;
        try {
            inFromClient = new DataInputStream(connection.getInputStream());
            outToClient = new DataOutputStream(connection.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setControl(ControlledEntity c) {
        control = c;
        c.switchClient(this);
        viewOffset = control.viewPosition().clone();
    }
    public String getUsername(){
        return username;
    }
    public LoginInfo doLogin() throws Exception {        
        Key key = new SecretKeySpec(constants.key,"AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, key);
        
        System.out.println("trying login");
        username = inFromClient.readUTF();
        System.out.println("username:"+username);
        int len = inFromClient.readInt();
        System.out.println("passlen:"+len);
        byte[] encpass = new byte[len];
        inFromClient.read(encpass);
        byte[] pass = c.doFinal(encpass);
        System.out.println("pass:"+pass);
        return new LoginInfo(username, pass);
    }

    public void destroy() {
        control.destroy();
    }

    public boolean doUpdate() {
        try {
            checkAliveCount++;
            if (checkAliveCount > 200) {
                throw new Exception("Client disconnected");
            }
            if (inFromClient.available() > 0) {
                checkAliveCount = 0;
                int type = inFromClient.readInt();
                if (type == 1) {
                    long[] newpos = new long[2];
                    newpos[0] = inFromClient.readInt() + control.viewPosition()[0];
                    newpos[1] = inFromClient.readInt() + control.viewPosition()[1];
                    control.moveTowards(newpos);
                } else if (type == -2) {
                    String chat = "";
                    char s = inFromClient.readChar();
                    while (s != '\n') {
                        chat += s;
                        s = inFromClient.readChar();
                    }
                    control.chat(chat);
                }else if (type == constants.COM_TYPE_KEEP_ALIVE) {
                }else if (type == constants.MOUSE_SELECT_LOC){
                    long[] newpos = new long[2];
                    newpos[0] = inFromClient.readInt() + control.viewPosition()[0];
                    newpos[1] = inFromClient.readInt() + control.viewPosition()[1];
                    control.select(newpos);
                } else if (type == constants.CLIENTTOSERVER_TEXTURE){
					int size = inFromClient.readInt();
					byte[] bytesin = new byte[size];
					inFromClient.read(bytesin, 0, size);
					textureBytes = bytesin;
				} else {
                    control.extra(type);
                }
            }
            HashSet<Entity> newview = control.getViewChanges();
            if (!newview.isEmpty()) {
                sendView(newview);
            }
            LinkedList<Chat> newchat = control.getChats();
            if (!newchat.isEmpty()) {
                sendChats(newchat);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void sendView(HashSet<Entity> view) throws Exception {
        Iterator<Entity> it = view.iterator();
        //System.out.println("Sending view size " + view.size());
        while (it.hasNext()) {
            Entity ent = it.next();
            if (ent.exists()) {
                long[] pos = ent.getPosition();
                if (ent.getID() == control.getID()) {
                    outToClient.writeLong(constants.VIEW_SELF);
                    outToClient.writeInt(constants.GRAPHIC_VIEW);
                    outToClient.writeInt((int) (pos[0] - viewOffset[0]));
                    outToClient.writeInt((int) (pos[1] - viewOffset[1]));
                }
                outToClient.writeLong(ent.getID());
                outToClient.writeInt(ent.getGraphic());
                outToClient.writeInt((int) (pos[0] - viewOffset[0]));
                outToClient.writeInt((int) (pos[1] - viewOffset[1]));
            } else {
                outToClient.writeLong(constants.VIEW_REMOVE_ENTITY);
                outToClient.writeLong(ent.getID());
            }
        }
    }

    public ControlledEntity getControl() {
        return control;
    }

    private void sendChats(LinkedList<Chat> chats) throws Exception {
        Iterator<Chat> it = chats.iterator();
        //System.out.println("Sending view size " + view.size());
        while (it.hasNext()) {
            Chat c = it.next();
            Entity ent = c.getEntity();
            String s = c.getChat();
            long[] pos = ent.getPosition();
            //System.out.println("sending chat: " + s);
            outToClient.writeLong(constants.VIEW_CHAT_SIGNAL);
            outToClient.writeInt(constants.GRAPHIC_NONE);
            outToClient.writeInt((int) (pos[0] - viewOffset[0]));
            outToClient.writeInt((int) (pos[1] - viewOffset[1]));
            outToClient.writeChars(s);
            outToClient.writeChar('\n');
        }
        //System.out.println("chat sent");
    }
}
