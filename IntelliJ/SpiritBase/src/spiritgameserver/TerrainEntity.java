/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

import spiritshared.constants;

/**
 *
 * @author jamesw
 */
public class TerrainEntity extends Entity{
    private int graphic;
    private long runnerRegen = 0;
    private long ownerID;
    private Runner runner = null;
    public TerrainEntity(int g){
        graphic = g;
        ownerID = -1;
        setSize(constants.TERRAIN_DEFAULT_SIZE);
    }
    @Override
    public int getGraphic() {
        return graphic;
    }
    public void setSize(int s){
        size = s;
    }
    
    public void possess(long id){
        ownerID = id;
    }
    public long getOwner(){
        return ownerID;
    }
    @Override
    public boolean collides(long[] pos, int type){
        if(this.distanceAwaySimple(pos)<getSize()){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void tick() {
        super.tick();
        runnerRegen++;
        if(runner == null || !runner.exists()){
            if(runnerRegen>constants.RUNNER_REGEN_TIME){
                runner = new Runner(this);
                runner.setPosition(mypos);
                runner.switchServer(server);
            }
        } else {
            runnerRegen = 0;
        }
    }

    @Override
    public void doDamage(int damage, int damagetype) {
        
    }
    
    
}
