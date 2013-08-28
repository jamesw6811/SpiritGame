/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

import java.util.HashSet;
import java.util.LinkedList;

/**
 *
 * @author jamesw
 */
public abstract interface Viewer {
    public abstract void showEntities(HashSet<Entity> ents);
    public abstract void showChats(LinkedList<Chat> chats);
    //public abstract HashSet<Entity> getViewChanges();
    //public abstract void requestNewView();
    public abstract long[] viewPosition();
}
