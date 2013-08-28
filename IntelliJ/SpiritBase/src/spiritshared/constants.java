/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritshared;

/**
 *
 * @author jamesw
 */
public class constants {
    public static final int SIZE_BLOCK = 1000;
    public static final int GRAPHIC_RUNNER = 3;
    public static final int GRAPHIC_NONE = 0;
    public static final int GRAPHIC_VIEW = -1;
    public static final int SPEED_CONTROL = 6;
    public static final int SPEED_RUNNER = 7;
    public static final int RUNNER_SCARE_MAG = 400;
    public static final int RUNNER_REGEN_TIME = 100;
    public static final int TERRAIN_DEFAULT_SIZE = 300;
    public static final int MIN_FOLLOW_DISTANCE = 300;
    public static final int MAX_VIEW_DISTANCE = 10000;
    public static final int FIREBALL_SIZE = 300;
    public static final int FIREBALL_RANGE = 600;
    public static final int FIREBALL_GRAPHIC_DURATION = 30;
    public static final int COM_TYPE_KEEP_ALIVE = -100;
	public static final int CLIENTTOSERVER_TEXTURE = -9;

	public static final long SERVERTOCLIENT_TEXTURE = -8;
    public static final long VIEW_CHAT_SIGNAL = -2;
    public static final long VIEW_REMOVE_ENTITY = -3;
    public static final long VIEW_SELF = -1;

    public static final byte CHOICE_DEPTH = 2;

	public static final int GRAPHIC_SPIRIT = 0;
	public static final int GRAPHIC_HERMITAGE = 2;
	public static final int GRAPHIC_HERMIT = 3;

    
    public static final int DAMAGETYPE_NORMAL = 0;
    public static final int DAMAGETYPE_BREATH_GIVE = 1;
    
    public static final int CLEAR_ACTION = 1*0 + 5*1;
    public static final int MOUSE_SELECT_LOC = 1*2 + 5*0 ;
    public static final int MAKE_VILLAGE = 1*2 + 5*4;
    public static final int FIREBALL = 1*1 + 5*1;
    
    public static final byte[] key = {(byte)0x43, (byte)0xAA, (byte)0xF2, (byte)0xD0, (byte)0x39, (byte)0x3D,(byte)0x39, (byte)0x3D, (byte)0x43, (byte)0xAA, (byte)0xF2, (byte)0xD0, (byte)0x39, (byte)0x3D,(byte)0x39, (byte)0x3D};
    
}
