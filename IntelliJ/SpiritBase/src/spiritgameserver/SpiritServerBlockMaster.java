/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

import spiritshared.LoginInfo;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import spiritshared.constants;

/**
 *
 * @author jamesw
 */
public class SpiritServerBlockMaster implements Runnable, SpiritCatcher {

    private SpiritServerAcceptor ssa;
    private boolean running;
    private SpiritServerBlock originblock;
    private HashSet<SpiritServerBlock> blocklist;
    private LinkedList<Client> tologin;
    private final static long[] origin = {0, 0};
    private HashMap<String, byte[]> logins;
    private HashSet<String> loggedin;
    private HashMap<String, ControlledEntity> savedentities;

    public SpiritServerBlockMaster() {
        blocklist = new HashSet<SpiritServerBlock>();
        tologin = new LinkedList<Client>();
        loggedin = new HashSet<String>();
        savedentities = new HashMap<String, ControlledEntity>();
        logins = new HashMap<String, byte[]>();
        originblock = new SpiritServerBlockLocal(this, origin);
        originblock.attachNewBlocks();
        new Thread(originblock).start();
        ssa = new SpiritServerAcceptor(this, 6789);
    }

    public synchronized void addBlock(SpiritServerBlock ssb) {
        blocklist.add(ssb);
        System.out.println(ssb.toString());
    }

    public void addConnection(Socket s) {
		synchronized(tologin){
			tologin.add(new Client(s));
		}
    }

    public synchronized SpiritServerBlock newBlockAt(long[] pos) {
        System.out.println("adding new block at x:" + pos[0] + " y:" + pos[1]);
        SpiritServerBlockLocal ssb = new SpiritServerBlockLocal(this, pos);
        ssb.pairAllBlocks(searchAdjacentBlocks(pos));
        addBlock(ssb);
        new Thread(ssb).start();
        return ssb;
    }

    private synchronized Collection<SpiritServerBlock> searchAdjacentBlocks(long[] pos) {
        LinkedList<SpiritServerBlock> blocks = new LinkedList<SpiritServerBlock>();
        SpiritServerBlock ssb = findBlockAt(new long[]{pos[0] + constants.SIZE_BLOCK, pos[1]});
        if (ssb != null) {
            blocks.add(ssb);
        }
        ssb = findBlockAt(new long[]{pos[0] - constants.SIZE_BLOCK, pos[1]});
        if (ssb != null) {
            blocks.add(ssb);
        }
        ssb = findBlockAt(new long[]{pos[0], pos[1] - constants.SIZE_BLOCK});
        if (ssb != null) {
            blocks.add(ssb);
        }
        ssb = findBlockAt(new long[]{pos[0], pos[1] + constants.SIZE_BLOCK});
        if (ssb != null) {
            blocks.add(ssb);
        }
        ssb = findBlockAt(new long[]{pos[0] + constants.SIZE_BLOCK, pos[1] + constants.SIZE_BLOCK});
        if (ssb != null) {
            blocks.add(ssb);
        }
        ssb = findBlockAt(new long[]{pos[0] - constants.SIZE_BLOCK, pos[1] + constants.SIZE_BLOCK});
        if (ssb != null) {
            blocks.add(ssb);
        }
        ssb = findBlockAt(new long[]{pos[0] + constants.SIZE_BLOCK, pos[1] - constants.SIZE_BLOCK});
        if (ssb != null) {
            blocks.add(ssb);
        }
        ssb = findBlockAt(new long[]{pos[0] - constants.SIZE_BLOCK, pos[1] - constants.SIZE_BLOCK});
        if (ssb != null) {
            blocks.add(ssb);
        }
        return blocks;
    }

    public synchronized SpiritServerBlock findBlockAt(long[] pos) {
        Iterator<SpiritServerBlock> it = blocklist.iterator();
        while (it.hasNext()) {
            SpiritServerBlock ssb = it.next();
            if (ssb.insideBlockPosition(pos)) {
                return ssb;
            }
        }
        return null;
    }

    public void run() {
		System.out.println("VERSION0.0001");
        Thread ssat = new Thread(ssa);
        ssat.start();
        running = true;
        long lastloop = System.currentTimeMillis();
        long ticknum = 0;
        long timeleft = 0;
        try {
            while (running) {

                doLogins();
                ticknum++;
                timeleft = System.currentTimeMillis() - lastloop - 50;
                if (timeleft < 0) {
                    Thread.yield();
                }
                timeleft = System.currentTimeMillis() - lastloop - 50;
                if (timeleft < 0) {
                    Thread.sleep(-timeleft);
                }
                if (ticknum % 20 == 0) {
                    System.out.println("Loop length:" + (System.currentTimeMillis() - lastloop));
                    //System.out.print(".");
                }
                lastloop = System.currentTimeMillis();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doLogins() {
		synchronized(tologin){
            Iterator<Client> it = tologin.iterator();
            while (it.hasNext()) {
                try {
                    Client logme = it.next();
                    LoginInfo li = logme.doLogin();
                    byte[] pass = logins.get(li.username);
                    if (pass == null) {
                        makeNewUser(logme, li);
                    } else {
                        if (Arrays.equals(pass, li.password)){
                            if(loggedin.contains(li.username)){
                                System.out.println("already logged in!");
                            } else {
                                connectUser(logme, li);
                            }
                        } else {
                            System.out.println("bad login, realpass:"+new String(pass)+" incorrect:"+new String(li.password));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            tologin.clear();
        
    }}

    private synchronized void makeNewUser(Client c, LoginInfo li) {
        logins.put(li.username, li.password);
        SpiritEntity e = new SpiritEntity();
        savedentities.put(li.username, e);
        c.setControl(e);
        loggedin.add(li.username);
        e.switchServer(originblock);
    }

    private void connectUser(Client c, LoginInfo li) {
        ControlledEntity e = savedentities.get(li.username);
        c.setControl(e);
        SpiritServerBlock ssb = findBlockAt(e.getPosition());
        e.switchServer(ssb);
        loggedin.add(li.username);
    }
    
    public synchronized void delog(String username){
        loggedin.remove(username);
    }

    public static void main(String argv[]) throws Exception {
        SpiritServerBlockMaster server = new SpiritServerBlockMaster();
        new Thread(server).start();
        long[] treepos = {50, 100};
        TerrainEntity t = new TerrainEntity(1);
        t.setPosition(treepos);
        t.switchServer(server.originblock);
    }
}
