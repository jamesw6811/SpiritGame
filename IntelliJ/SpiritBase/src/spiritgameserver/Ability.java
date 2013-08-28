/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author jamesw
 */
public class Ability {
    
    private int range;
    private int breath;
    private EffectType type;
    private boolean noterrain = false;

    public Ability(EffectType t) {
        type = t;
        range = 0;
        breath = 0;
    }

    public Ability(EffectType t, int r) {
        type = t;
        range = r;
        breath = 0;
    }

    public Ability(EffectType t, int r, int b) {
        type = t;
        range = r;
        breath = b;
    }

    public int getRange() {
        return range;
    }

    public EffectType getType() {
        return type;
    }

    public int getBreath() {
        return breath;
    }

    public void setNoTerrain(boolean b) {
        noterrain = b;
    }

    public boolean validTarget(Set<Entity> es, long[] pos) {
        Iterator<Entity> e = es.iterator();
        boolean valid = true;
        if (noterrain) {
            while (e.hasNext()) {
                Entity mye = e.next();
                if (mye instanceof TerrainEntity && mye.collides(pos, 0)) {
                    valid = false;
                }
            }
        }
        return valid;
    }
}
