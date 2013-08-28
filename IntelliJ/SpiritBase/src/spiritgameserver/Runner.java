/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import spiritshared.constants;

/**
 *
 * @author jamesw
 */
public class Runner extends Entity implements Viewer {

    protected int[] direction;
    protected long[] destination;
    protected boolean running = false;
    public static final int speed = spiritshared.constants.SPEED_RUNNER;
    protected HashSet<Entity> viewchanges;
    protected HashSet<Entity> viewmem;
    protected HashSet<Entity> collisions;
    protected LinkedList<Chat> chats;
    protected TerrainEntity home;
    protected int chatCount;
    protected boolean following;
    protected Entity followingent;
    protected long givebreathCount = 0;
    protected Random rnd;

    public Runner(TerrainEntity h) {
        direction = new int[2];
        home = h;
        collisions = new HashSet<Entity>();
        viewchanges = new HashSet<Entity>();
        chats = new LinkedList<Chat>();
        viewmem = new HashSet<Entity>();
        chatCount = 0;
        following = false;
        followingent = null;
        
        rnd =new Random();
    }

    @Override
    public int getGraphic() {
        return spiritshared.constants.GRAPHIC_RUNNER;
    }

    protected synchronized HashSet<Entity> getViewChanges() {
        HashSet<Entity> v = viewchanges;
        viewchanges = new HashSet<Entity>();
        return v;
    }

    public synchronized LinkedList<Chat> getChats() {
        LinkedList<Chat> c = chats;
        chats = new LinkedList<Chat>();
        return c;
    }

    protected void chat(String s) {
        server.chat(this, s);
        chatCount = 0;
    }

    public void showChats(LinkedList<Chat> cs) {
        chats.addAll(cs);
    }

    protected void moveTowards(long[] pos) {
        int dx = (int) (pos[0] - mypos[0]);
        int dy = (int) (pos[1] - mypos[1]);
        int mag = dx * dx + dy * dy;
        if (mag < speed) {
            direction[0] = 0;
            direction[1] = 0;
        } else {
            direction[0] = (speed * (dx * Math.abs(dx))) / mag;
            direction[1] = (speed * (dy * Math.abs(dy))) / mag;
        }
    }

    protected void moveAway(long[] pos) {
        int dx = -(int) (pos[0] - mypos[0]);
        int dy = -(int) (pos[1] - mypos[1]);
        int mag = dx * dx + dy * dy;
        if (mag == 0) {
            direction[0] = speed;
            direction[1] = speed;
        } else {
            direction[0] = (speed * (dx * Math.abs(dx))) / mag;
            direction[1] = (speed * (dy * Math.abs(dy))) / mag;
        }
    }

    public void showEntities(HashSet<Entity> ents) {
        viewchanges.addAll(ents);
    }

    public long[] viewPosition() {
        return mypos;
    }
    
    public long[] randomPositionNear(long[] a){
        long ax = a[0];
        long ay = a[1];
        long[] b = {a[0] + rnd.nextInt(constants.MIN_FOLLOW_DISTANCE)-constants.MIN_FOLLOW_DISTANCE/2,
        a[1] + rnd.nextInt(constants.MIN_FOLLOW_DISTANCE-constants.MIN_FOLLOW_DISTANCE/2)};
        return b;
    }

    public void tick() {
        HashSet<Entity> newview = getViewChanges();
        LinkedList<Entity> toRemove = new LinkedList<Entity>();
        direction[0] = 0;
        direction[1] = 0;
        chatCount++;
        running = false;
        if(following&&distanceAwaySimple(followingent)>constants.RUNNER_SCARE_MAG)following=false;
        Iterator<Entity> viewit = viewmem.iterator();
        while (!running && !following && viewit.hasNext()) {
            Entity eo = viewit.next();
            if (eo instanceof SpiritEntity && distanceAwaySimple(eo) < constants.RUNNER_SCARE_MAG) {
                moveAway(eo.getPosition());
                running = true;
            } else {
                if (distanceAwaySimple(eo) > constants.MAX_VIEW_DISTANCE) {
                    toRemove.add(eo);
                }
            }
        }
        viewmem.removeAll(toRemove);
        viewit = newview.iterator();
        while (viewit.hasNext()) {
            Entity eo = viewit.next();
            if (eo instanceof TerrainEntity){
                collisions.add(eo);
            }
            if (!following && eo instanceof SpiritEntity && distanceAwaySimple(eo) < constants.RUNNER_SCARE_MAG) {
                if (eo.getID() == this.home.getOwner()) {
                    following = true;
                    followingent = eo;
                    running = false;
                    //chat("Here to serve you!");
                    moveTowards(eo.getPosition());
                } else {
                    moveAway(eo.getPosition());
                    viewmem.add(eo);
                }
            }
            
        }
        if (following) {
            if(distanceAwaySimple(followingent)>constants.MIN_FOLLOW_DISTANCE){
                moveTowards(followingent.getPosition());
            } else {
                givebreathCount++;
                if(givebreathCount>constants.RUNNER_REGEN_TIME){
                    followingent.doDamage(1, constants.DAMAGETYPE_BREATH_GIVE);
                    givebreathCount = 0;
                }
                moveTowards(randomPositionNear(followingent.getPosition()));
            }
        } else if (running) {
            if (chatCount > 50) {
                //chat("Run away!");
            }
        } else {
            moveTowards(home.getPosition());
            //System.out.println("moving home!" + direction[0] + " , " + direction[1]);
        }

        if (direction[0] != 0 || direction[1] != 0) {
            
            if(canMove(new long[]{mypos[0]+direction[0], mypos[1]+direction[1]})){
                mypos[0] += direction[0];
                mypos[1] += direction[1];
                server.updateView(this);
            }
        } else {

        }
    }
    
    private boolean canMove(long[] pos){
        LinkedList<Entity> removeme = new LinkedList<Entity>();
        Iterator<Entity> colit = collisions.iterator();
        boolean can = false;
        while(colit.hasNext()){
            Entity e = colit.next();
            if(e.exists()){
                if(e.collides(pos, 0))can = true;
            } else {
                removeme.add(e);
            }
        }
        collisions.removeAll(removeme);
        return can;
    }

    @Override
    public void doDamage(int damage, int damagetype) {
        this.destroy();
    }
    
}
