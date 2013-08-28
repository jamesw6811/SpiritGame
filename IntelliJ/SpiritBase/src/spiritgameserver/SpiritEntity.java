/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

import java.util.HashMap;
import spiritshared.constants;

/**
 *
 * @author jamesw
 */
public class SpiritEntity extends ControlledEntity {

    private static final long[] origin = {0, 0};
    private int breath;
    protected static final HashMap<Integer, Ability> abilities = new HashMap<Integer, Ability>();
    private int readiedAbility;

    static {
        EffectType spawnFarmEffect = new EffectType();
        EntityGenerator eg = new EntityGenerator() {
            public void makeEntity(SpiritServer s, long[] pos, Entity owner) {
                TerrainEntity mine = new TerrainEntity(2);
                mine.setPosition(pos);
                mine.possess(owner.getID());
                mine.switchServer(s);
            }
        };
        spawnFarmEffect.setToCreate(eg);
        Ability a = new Ability(spawnFarmEffect, 0, 2);
        a.setNoTerrain(true);
        abilities.put(constants.MAKE_VILLAGE, a);

        EffectType fireballEffect = new EffectType();
        fireballEffect.setRange(constants.FIREBALL_SIZE);
        fireballEffect.setDamage(10);
        EffectType fireballLinger = new EffectType();
        fireballLinger.setGraphic(1);
        fireballLinger.setDuration(constants.FIREBALL_GRAPHIC_DURATION);
        fireballEffect.addChild(fireballLinger);
        abilities.put(constants.FIREBALL, new Ability(fireballEffect, constants.FIREBALL_RANGE, 1));
    }

    public SpiritEntity() {
        mypos = origin.clone();
        readiedAbility = 0;
        breath = 10;
    }

    @Override
    public int getGraphic() {
        return 0;
    }

    private void tryAbility(long[] pos, Ability ab) {
        if (ab != null) {
            if (this.distanceAwaySimple(pos) <= ab.getRange()) {
                if (ab.getBreath() < breath) {
                    if (ab.validTarget(viewhist, pos)) {
                        breath -= ab.getBreath();
                        //this.chat("Breath left:" + breath);
                        ab.getType().makeEntity(server, pos, this);
                    } else {
                        this.chat("Not a valid location...");
                    }
                } else {
                    this.chat("Out of breath!");
                }
            } else {
                this.chat("Out of range!");
            }
        } else {
            this.chat("No such ability...");
        }
    }

    @Override
    public void extra(int type) {
        Ability et = abilities.get(type);
        if (et != null) {
            if (et.getRange() == 0) {
                tryAbility(mypos, et);
            } else {
                //this.chat("Ability readied.");
                readiedAbility = type;
            }
        }
    }

    @Override
    public void select(long[] pos) {
        if (readiedAbility == 0) {
            this.chat("No ability ready!");
        } else {
            Ability et = abilities.get(readiedAbility);
            tryAbility(pos, et);
        }
    }

    @Override
    public void doDamage(int damage, int damagetype) {
        if(damagetype==constants.DAMAGETYPE_BREATH_GIVE){
            breath += damage;
            //this.chat("Mmm... breath!");
        }else{
            //this.chat("Ow! Spirits are invulnerable....");
        }
    }
}
