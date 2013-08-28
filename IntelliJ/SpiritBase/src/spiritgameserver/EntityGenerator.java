/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

/**
 *
 * @author jamesw
 */
public interface EntityGenerator {
    public void makeEntity(SpiritServer s, long[] pos, Entity owner);
}
