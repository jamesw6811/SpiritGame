/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author jamesw
 */
public class SpiritServerAcceptor implements Runnable{
    private SpiritCatcher server;
    private boolean running;
    private int port;
    public SpiritServerAcceptor(SpiritCatcher ss, int p){
        server = ss;
        port = p;
    }
    public void run(){
        try{
            running = true;
            ServerSocket welcomeSocket = new ServerSocket(port);
            while(running){
                System.out.println("Waiting conn");
                Socket connectionSocket = welcomeSocket.accept();
                System.out.println("Accepted " + connectionSocket.getInetAddress().toString() + " -- passing to server.");
                server.addConnection(connectionSocket);
                System.out.println("Passed to server");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("SpiritServerAcceptor dead.");
    }
}
