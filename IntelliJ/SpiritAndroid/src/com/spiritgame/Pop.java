package com.spiritgame;

import javax.microedition.khronos.opengles.GL10;

public interface Pop {
    public abstract boolean tick();

    public abstract void render(float camAngleX, float camAngleY, GL10 gl);
}

