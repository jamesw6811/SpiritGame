/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

import java.net.Socket;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import spiritshared.constants;

/**
 *
 * @author jamesw
 */
public class SpiritServerBlockLocal implements SpiritServerBlock {

    private LinkedList<Client> clients;
    private LinkedList<Client> clientsadd;
    private LinkedList<Client> clientsremove;
    private LinkedList<Entity> entities;
    private LinkedList<Entity> entitiesadd;
    private LinkedList<Entity> entitiesremove;
    private LinkedList<Viewer> viewers;
    private LinkedList<Viewer> viewersadd;
    private LinkedList<Viewer> viewersremove;
    private LinkedList<Viewer> viewersrequest;
    private LinkedList<Chat> chats;
    private HashSet<SpiritServerBlock> attached;
    private HashSet<Entity> viewUpdates;
    private boolean running;
    private SpiritServerBlockMaster master;
    private long[] blockorigin;
    private long[] borders; //Starting with top going clockwise up is negative

    public SpiritServerBlockLocal(SpiritServerBlockMaster ssbm, long[] pos) {
        blockorigin = pos;
        borders = new long[4];
        borders[0] = pos[1] - constants.SIZE_BLOCK / 2;
        borders[1] = pos[0] + constants.SIZE_BLOCK / 2;
        borders[2] = pos[1] + constants.SIZE_BLOCK / 2;
        borders[3] = pos[0] - constants.SIZE_BLOCK / 2;
        clients = new LinkedList<Client>();
        clientsadd = new LinkedList<Client>();
        clientsremove = new LinkedList<Client>();
        chats = new LinkedList<Chat>();
        entities = new LinkedList<Entity>();
        entitiesadd = new LinkedList<Entity>();
        entitiesremove = new LinkedList<Entity>();
        viewers = new LinkedList<Viewer>();
        viewersadd = new LinkedList<Viewer>();
        viewersremove = new LinkedList<Viewer>();
        viewersrequest = new LinkedList<Viewer>();
        viewUpdates = new HashSet<Entity>();
        attached = new HashSet<SpiritServerBlock>();
        running = false;
        master = ssbm;
        master.addBlock(this);
    }

    public synchronized void addConnection(Socket s) {
        System.out.println("LOCAL BLOCK NOT AN ENTRY POINT!");
    }

    public String toString() {
        return "SpiritServerBlockLocal origin:" + blockorigin[0] + " , " + blockorigin[1];
    }

    public void attachNewBlocks() {
        long startx = blockorigin[0] - constants.SIZE_BLOCK;
        long starty = blockorigin[1] - constants.SIZE_BLOCK;
        SpiritServerBlockLocal[][] ssba = {{null, null, null}, {null, null, null}, {null, null, null}};
        for (int i = 0; i < 9; i++) {
            if (i != 4) {
                long[] newbpos = {startx + constants.SIZE_BLOCK * (i % 3), starty + constants.SIZE_BLOCK * (i / 3)};
                SpiritServerBlockLocal ss = new SpiritServerBlockLocal(master, newbpos);
                ssba[i % 3][i / 3] = ss;
                attached.add(ss);
                ss.attachBlock(this);
                new Thread(ss).start();
            }
        }
        ssba[0][0].attachBlock(ssba[0][1]);
        ssba[0][0].attachBlock(ssba[1][0]);

        ssba[2][0].attachBlock(ssba[2][1]);
        ssba[2][0].attachBlock(ssba[1][0]);

        ssba[2][2].attachBlock(ssba[2][1]);
        ssba[2][2].attachBlock(ssba[1][2]);

        ssba[0][2].attachBlock(ssba[1][2]);
        ssba[0][2].attachBlock(ssba[0][1]);

        ssba[1][0].attachBlock(ssba[0][0]);
        ssba[1][0].attachBlock(ssba[2][0]);
        ssba[1][0].attachBlock(ssba[0][1]);
        ssba[1][0].attachBlock(ssba[2][1]);

        ssba[2][1].attachBlock(ssba[1][0]);
        ssba[2][1].attachBlock(ssba[2][0]);
        ssba[2][1].attachBlock(ssba[1][2]);
        ssba[2][1].attachBlock(ssba[2][2]);

        ssba[1][2].attachBlock(ssba[0][1]);
        ssba[1][2].attachBlock(ssba[0][2]);
        ssba[1][2].attachBlock(ssba[2][1]);
        ssba[1][2].attachBlock(ssba[2][2]);

        ssba[0][1].attachBlock(ssba[0][0]);
        ssba[0][1].attachBlock(ssba[1][0]);
        ssba[0][1].attachBlock(ssba[1][2]);
        ssba[0][1].attachBlock(ssba[0][2]);

    }

    public void attachBlock(SpiritServerBlock ssb) {
        attached.add(ssb);
    }

    public void pairAllBlocks(Collection<SpiritServerBlock> ssbc) {
        attached.addAll(ssbc);
        Iterator<SpiritServerBlock> it = ssbc.iterator();
        while (it.hasNext()) {
            it.next().attachBlock(this);
        }
    }

    public void addClient(Client c) {
        synchronized (clientsadd) {
            clientsadd.add(c);
        }
    }

    public void removeClient(Client c) {
        synchronized (clientsremove) {
            clientsremove.add(c);
        }
    }

    private void addClients() {
        synchronized (clientsadd) {
            Iterator<Client> it = clientsadd.iterator();
            while (it.hasNext()) {
                clients.add(it.next());
            }
            clientsadd = new LinkedList<Client>();
        }
    }

    public synchronized void addEntity(Entity e) {
        entitiesadd.add(e);
    }

    private void addEntities() {
        Iterator<Entity> it = entitiesadd.iterator();
        while (it.hasNext()) {
            Entity e = it.next();
            entities.add(e);
            updateView(e);
        }
        entitiesadd = new LinkedList<Entity>();
    }

    public synchronized void removeEntity(Entity e) {
        entitiesremove.add(e);
    }

    private void removeEntities() {
        Iterator<Entity> it = entitiesremove.iterator();
        while (it.hasNext()) {
            Entity e = it.next();
            entities.remove(e);
        }
        entitiesremove = new LinkedList<Entity>();
    }

    public synchronized void addViewer(Viewer v) {
        viewersadd.add(v);
        requestView(v);
    }

    private void addViewers() {
        Iterator<Viewer> it = viewersadd.iterator();
        while (it.hasNext()) {
            viewers.add(it.next());
        }
        viewersadd = new LinkedList<Viewer>();
    }

    public synchronized void removeViewer(Viewer v) {
        viewersremove.add(v);
    }

    private void removeViewers() {
        Iterator<Viewer> it = viewersremove.iterator();
        while (it.hasNext()) {
            viewers.remove(it.next());
        }
        viewersremove = new LinkedList<Viewer>();
    }

    public void chat(Entity e, String s) {
        synchronized (chats) {
            Chat newc = new Chat(s, e);
            chats.add(newc);
            Iterator<SpiritServerBlock> atit = attached.iterator();
            while (atit.hasNext()) {
                atit.next().transferChatUpdate(newc);
            }
        }
    }

    public void transferChatUpdate(Chat c) {
        synchronized (chats) {
            chats.add(c);
        }
    }

    public void run() {
        running = true;
        long lastloop = System.currentTimeMillis();
        long ticknum = 0;
        long timeleft = 0;
        try {
            while (running) {
                //System.out.print(".");
                addClients();
                removeClients();
                addEntities();
                removeEntities();
                addViewers();
                removeViewers();
                tickAll();
                sendRequestedViews();
                sendViewUpdates();
                sendChatUpdates();
                updateClients();
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
            System.out.println("BIG ERROR!");
        }
    }

    public boolean insideBlockPosition(long[] pos) {
        return pos[1] >= borders[0] && pos[0] <= borders[1] && pos[1] <= borders[2] && pos[0] >= borders[3];
    }

    public void transferViewUpdate(Entity e) {
        synchronized (viewUpdates) {
            viewUpdates.add(e);
        }
    }

    public void updateView(Entity e) {
        synchronized (viewUpdates) {
            if (insideBlockPosition(e.getPosition())) {
                viewUpdates.add(e);
                Iterator<SpiritServerBlock> it = attached.iterator();
                while (it.hasNext()) {
                    it.next().transferViewUpdate(e);
                }
            } else {
                Iterator<SpiritServerBlock> it = attached.iterator();
                boolean found = false;
                while (!found && it.hasNext()) {
                    SpiritServerBlock ssb = it.next();
                    if (ssb.insideBlockPosition(e.getPosition())) {
                        found = true;
                        transferEntity(e, ssb);
                    }
                }
                if (!found) {
                    transferEntityNew(e);
                }
            }
        }
    }

    private void transferEntity(Entity e, SpiritServerBlock ssb) {
        e.switchServer(ssb);
    }

    private void transferEntityNew(Entity e) {
        long[] errpos = e.getPosition();

        System.out.println("Lost at " + errpos[0] + " , " + errpos[1]);
        System.out.println("Borders:" + borders[0] + " " + borders[1] + " " + borders[2] + " " + borders[3] + " ");
        long[] newblockpos = {blockorigin[0] - constants.SIZE_BLOCK, blockorigin[1] - constants.SIZE_BLOCK};
        if (errpos[0] > borders[1]) {
            newblockpos[0] = blockorigin[0] + constants.SIZE_BLOCK;
        } else if (errpos[0] >= borders[3]) {
            newblockpos[0] = blockorigin[0];
        }
        if (errpos[1] > borders[2]) {
            newblockpos[1] = blockorigin[1] + constants.SIZE_BLOCK;
        } else if (errpos[1] >= borders[0]) {
            newblockpos[1] = blockorigin[1];
        }
        SpiritServerBlock ssb = master.newBlockAt(newblockpos);
        e.switchServer(ssb);
    }

    public synchronized void updateClients() {
        LinkedList<Client> toRemove = null;
        Iterator<Client> it = clients.iterator();
        while (it.hasNext()) {
            Client next = it.next();
            if (!next.doUpdate()) {
                if (toRemove == null) {
                    toRemove = new LinkedList<Client>();
                }
                toRemove.add(next);
            }
        }
        if (toRemove != null) {
            it = toRemove.iterator();
            while (it.hasNext()) {
                disconnect(it.next());
            }
        }
    }

    public synchronized void disconnect(Client c) {
        System.out.println("Disconn:" + c.getControl().getID());
        c.destroy();
        master.delog(c.getUsername());
    }

    private void removeClients() {
        Iterator<Client> it = clientsremove.iterator();
        while (it.hasNext()) {
            clients.remove(it.next());
        }
        clientsremove = new LinkedList<Client>();
    }

    private synchronized void tickAll() {
        Iterator<Entity> it = entities.iterator();
        while (it.hasNext()) {
            it.next().tick();
        }
    }

    private void sendViewUpdates() {
        synchronized (viewUpdates) {
            Iterator<Viewer> it = viewers.iterator();
            while (it.hasNext()) {
                it.next().showEntities(viewUpdates);
            }
        }
        viewUpdates = new HashSet<Entity>();
    }

    private void sendChatUpdates() {
        synchronized (chats) {
            Iterator<Viewer> it = viewers.iterator();
            while (it.hasNext()) {
                it.next().showChats(chats);
            }
            chats = new LinkedList<Chat>();
        }
    }

    public void requestView(Viewer v) {
        viewersrequest.add(v);
        Iterator<SpiritServerBlock> atit = attached.iterator();
        while (atit.hasNext()) {
            atit.next().transferViewRequest(v);
        }
    }

    public void transferViewRequest(Viewer v) {
        synchronized (viewersrequest) {
            viewersrequest.add(v);
        }
    }

    private void sendRequestedViews() {
        synchronized (viewersrequest) {
            Iterator<Viewer> it = viewersrequest.iterator();
            while (it.hasNext()) {
                it.next().showEntities(new HashSet<Entity>(entities));
            }
            viewersrequest = new LinkedList<Viewer>();
        }
    }

    public void transferEffect(EffectType e, long[] pos) {
        doEffectSecondary(e, pos);
    }

    public void doEffect(EffectType e, long[] pos) {
        doEffectPrimary(e, pos);
        doEffectSecondary(e, pos);
    }

    private void doEffectPrimary(EffectType e, long[] pos) {
        Iterator<SpiritServerBlock> atit = attached.iterator();
        while (atit.hasNext()) {
            atit.next().transferEffect(e, pos); // Maybe check to see if range is outside borders? may go faster?
        }
    }

    private void doEffectSecondary(EffectType e, long[] pos) {
        if (e.getRange() > 0) {
            Iterator<Entity> it = entities.iterator();
            while (it.hasNext()) {
                Entity ent = it.next();
                if (ent.distanceAwaySimple(pos) < e.getRange()) {
                    e.effectEntity(ent);
                }
            }
        }
    }


}
