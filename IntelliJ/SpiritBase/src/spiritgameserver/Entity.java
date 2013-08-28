/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

/**
 *
 * @author jamesw
 */
public abstract class Entity {

    public static long nextID = 0;
    private long ID;
    protected long[] mypos = {0,0};
    protected SpiritServer server = null;
    private boolean exists = false;
    protected int size = 0;

    public Entity() {
        ID = nextID++;
    }

    public void switchServer(SpiritServer s) {
        exists = true;
        if (server != s) {
            if (server != null) {
                server.removeEntity(this);
                if (this instanceof Viewer) {
                    server.removeViewer((Viewer) this);
                }
            }
            server = s;
            s.addEntity(this);
            if (this instanceof Viewer) {
                s.addViewer((Viewer) this);
                s.requestView((Viewer) this);
            }
        }

    }

    public long getID() {
        return ID;
    }

    public boolean exists() {
        return exists;
    }
    public boolean collides(long[] pos, int type){
        return false;
    }

    public long[] getPosition() {
        return mypos.clone();
    }
    public void setPosition(long[] pos) {
        mypos = pos.clone();
    }
    public abstract int getGraphic();

    public void tick() {
    }
    public abstract void doDamage(int damage, int damagetype);

    public void destroy() {
        server.removeEntity(this);
        server.updateView(this);
        if (this instanceof Viewer) {
            server.removeViewer((Viewer) this);
        }
        server = null;
        exists = false;
    }

    public int getSize() {
        return size;
    }
    

    protected long distanceAwaySimple(Entity eo) {
        if (!eo.exists()) {
            return Long.MAX_VALUE;
        }
        long[] a = eo.getPosition();
        long[] b = mypos;
        return Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]);
    }    
    protected long distanceAwaySimple(long[] a) {
        long[] b = mypos;
        return Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]);
    }
}
