/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameclient;

import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author jamesw
 */
public class Face3D {
    public boolean quad;
    public float[] vertex = new float[4]; // 3 indicies -- not coordinates
    public float[] normal = new float[4];
    public float[] texture = new float[4];
    public Face3D(float[] v, float[] n, float[] t, boolean qu){
        vertex = v;
        normal = n;
        texture = t;
        quad = qu;
    }
}
