/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

import java.util.Iterator;

/**
 *
 * @author jamesw
 */
public class Effect extends Entity {

    private EffectType type;
    private Entity owner;
    private long counter = 0;

    public Effect(EffectType t, Entity own) {
        type = t;
        owner = own;
    }

    public Entity getOwner() {
        return owner;
    }

    public int getGraphic() {
        return type.getGraphic();
    }

    public void tick() {
        super.tick();
        if (exists()) {
            counter++;
            server.doEffect(type, getPosition());
            if (type.getToCreate() != null) {
                type.getToCreate().makeEntity(server, getPosition(), owner);
            }
        }
        if (counter >= type.getDuration()) {
            this.destroy();
        }
    }

    @Override
    public void doDamage(int damage, int damagetype) {
    }
}
