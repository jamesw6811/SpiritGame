package com.spiritgame;

public class Face3D {
    public boolean quad;
    public float[] vertex = new float[4]; // 3 indicies -- not coordinates
    public float[] normal = new float[4];
    public float[] texture = new float[4];

    public Face3D(float[] v, float[] n, float[] t, boolean qu) {
        vertex = v;
        normal = n;
        texture = t;
        quad = qu;
    }
}
