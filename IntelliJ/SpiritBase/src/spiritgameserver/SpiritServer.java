/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

import java.net.Socket;
import java.util.LinkedList;

/**
 *
 * @author jamesw
 */
public interface SpiritServer extends Runnable{
    public abstract void addConnection(Socket connection);
    public abstract void disconnect(Client c);
    public abstract void addEntity(Entity e);
    public abstract void removeEntity(Entity e);
    public abstract void doEffect(EffectType e, long[] pos);
    public abstract void addViewer(Viewer v);
    public abstract void removeViewer(Viewer v);
    public abstract void addClient(Client c);
    public abstract void removeClient(Client c);
    public abstract void requestView(Viewer v);
    public abstract void updateView(Entity e);
    public abstract void chat(Entity e, String s);
}
