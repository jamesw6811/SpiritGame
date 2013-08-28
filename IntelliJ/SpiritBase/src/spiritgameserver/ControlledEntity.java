/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

import java.util.HashSet;
import java.util.LinkedList;
import spiritshared.constants;

/**
 *
 * @author jamesw
 */
public abstract class ControlledEntity extends Entity implements Viewer{
    protected int[] direction;
    protected long[] destination;
    protected boolean navigating = false;
    protected Client client;
    public static final int speed = constants.SPEED_CONTROL;
    protected HashSet<Entity> viewchanges;
    protected HashSet<Entity> viewhist;
    protected LinkedList<Chat> chats;
    
    public ControlledEntity(){
        direction = new int[2];
        viewchanges = new HashSet<Entity>();
        viewhist = new HashSet<Entity>();
        chats = new LinkedList<Chat>();
    }
    public void switchClient(Client c){
        if(client!=null){
            if(server!=null)server.removeClient(client);
        }
        client = c;
        if(server!=null)server.addClient(c);
    }
    public synchronized HashSet<Entity> getViewChanges() {
        HashSet<Entity> v = viewchanges;
        viewchanges = new HashSet<Entity>();
        return v;
    }
    public synchronized LinkedList<Chat> getChats() {
        LinkedList<Chat> c = chats;
        chats = new LinkedList<Chat>();
        return c;
    }
    public void chat (String s){
        server.chat(this, s);
    }
    public void showChats(LinkedList<Chat> cs){
        chats.addAll(cs);
    }
    public void moveTowards(long[] pos) {
        destination = pos;
        navigating = true;
        int dx = (int)(pos[0] - mypos[0]);
        int dy = (int)(pos[1] - mypos[1]);
        int mag = dx*dx + dy*dy;
        if(mag<speed){
            direction[0] = 0;
            direction[1] = 0;
        } else {
            direction[0] = (speed*(dx*Math.abs(dx)))/mag;
            direction[1] = (speed*(dy*Math.abs(dy)))/mag;
        }
    }
    
    public void showEntities(HashSet<Entity> ents) {
        viewchanges.addAll(ents);
        viewhist.addAll(ents);
    }



    public void requestNewView() {
        server.requestView(this);
    }
    public long[] viewPosition(){
        return mypos;
    }
    public void tick(){
        if(navigating&&(direction[0]!=0||direction[1]!=0)){
            moveTowards(destination);
           // System.out.println("mypos0:" + mypos[0]);
           // System.out.println("mypos1:" + mypos[1]);
            mypos[0] += direction[0];
            mypos[1] += direction[1];
            server.updateView(this);
            //System.out.print("moving");
        } else {
            boolean navigating = false;
        }
    }

    @Override
    public void switchServer(SpiritServer s) {
        if(server!=null&&server!=s)server.removeClient(client);
        super.switchServer(s);
        s.addClient(client);
    }
    public void destroy(){
        if(server!=null)server.removeClient(client);
        super.destroy();
    }
        
    public abstract void extra(int type);
    public abstract void select(long[] pos);
}
