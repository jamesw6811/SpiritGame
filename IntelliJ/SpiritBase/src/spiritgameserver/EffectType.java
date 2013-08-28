/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

import java.util.Iterator;
import java.util.LinkedList;

/**
 *
 * @author jamesw
 */
public class EffectType implements EntityGenerator{
    private int graphic = 0;
    private LinkedList<EffectType> children = new LinkedList<EffectType>();    
    private EntityGenerator toCreate = null;
    private long[] offset = {0,0};
    private int damage = 0;
    private int damagetype = 0;
    private int range = 0;
    private int duration = 0;
    private boolean absorb = false;
    

    public void setDamagetype(int damagetype) {
        this.damagetype = damagetype;
    }
    public void makeEntity(SpiritServer s, long[] pos, Entity owner){
        Effect e = new Effect(this, owner);
        e.setPosition(pos);
        e.switchServer(s);
        Iterator<EffectType> et = getChildren().iterator();
        while(et.hasNext()){
            et.next().makeEntity(s, pos, owner);
        }
    }
    public void effectEntity(Entity e){
        if(damage>0)e.doDamage(damage, damagetype);
        
    }

    public int getGraphic() {
        return graphic;
    }

    public void setGraphic(int graphic) {
        this.graphic = graphic;
    }
    
    public void setAbsorb(boolean b){
        absorb = b;
    }
    public boolean getAbsorb(){
        return absorb;
    }

    public void addChild(EffectType t){
        children.add(t);
    }
    private LinkedList<EffectType> getChildren(){
        return children;
    }

    public void setChildren(LinkedList<EffectType> children) {
        this.children = children;
    }

    public EntityGenerator getToCreate() {
        return toCreate;
    }

    public void setToCreate(EntityGenerator toCreate) {
        this.toCreate = toCreate;
    }

    public long[] getOffset() {
        return offset;
    }

    public void setOffset(long[] offset) {
        this.offset = offset;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
    
}
