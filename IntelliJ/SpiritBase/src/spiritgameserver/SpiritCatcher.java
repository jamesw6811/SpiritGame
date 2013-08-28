/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

import java.net.Socket;

/**
 *
 * @author jamesw
 */
public interface SpiritCatcher {
    public void addConnection(Socket s);
}
