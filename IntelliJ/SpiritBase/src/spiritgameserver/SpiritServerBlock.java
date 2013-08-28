/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

import java.util.Collection;

/**
 *
 * @author jamesw
 */
public interface SpiritServerBlock extends SpiritServer{
    public void attachNewBlocks(); // Attach new SpiritServerBlocks to edges
    public void attachBlock(SpiritServerBlock ssb);
    public void pairAllBlocks(Collection<SpiritServerBlock> ssbc);
    public boolean insideBlockPosition(long[] pos);
    public void transferViewUpdate(Entity e);
    public void transferViewRequest(Viewer v);
    public void transferChatUpdate(Chat c);
    public void transferEffect(EffectType e, long[] pos);
    public String toString();
}
