/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameclient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;

/**
 *
 * @author jamesw
 */
public class Model3D {

    public List<Vector3f> verticies = new ArrayList<Vector3f>();
    public List<Vector3f> normals = new ArrayList<Vector3f>();
    public List<Vector3f> textures = new ArrayList<Vector3f>();
    public List<Face3D> faces = new ArrayList<Face3D>();
    public boolean textured;
    private Texture texture;

    public Model3D() {
    }
    
    public void setTexture(Texture t){
        textured = true;
        texture = t;
    }
    public Texture getTexture(){
        return texture;
    }

    public static Model3D loadModel(InputStream f) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(f));
        Model3D m = new Model3D();
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith("v ")) {
                float x = Float.valueOf(line.split(" ")[1]);
                float y = Float.valueOf(line.split(" ")[2]);
                float z = Float.valueOf(line.split(" ")[3]);
                m.verticies.add(new Vector3f(x, y, z));
            }
            if (line.startsWith("vt ")) {
                float x = Float.valueOf(line.split(" ")[1]);
                float y = Float.valueOf(line.split(" ")[2]);
                m.textures.add(new Vector3f(x, 1.0f-y, 0));
            }
            if (line.startsWith("vn ")) {
                float x = Float.valueOf(line.split(" ")[1]);
                float y = Float.valueOf(line.split(" ")[2]);
                float z = Float.valueOf(line.split(" ")[3]);
                m.normals.add(new Vector3f(x, y, z));
            }
            if (line.startsWith("f ")) {
                String[] sp = line.split(" ");
                boolean qu = false;
                if (sp.length == 5) {
                    qu = true;
                }
                if (sp.length < 4) {
                    System.out.println("less than triangle?");
                } else {
                    float i1 = Float.valueOf(line.split(" ")[1].split("/")[0]);
                    float i2 = Float.valueOf(line.split(" ")[2].split("/")[0]);
                    float i3 = Float.valueOf(line.split(" ")[3].split("/")[0]);
                    float[] vertexInd = null;
                    if (qu) {
                        float[] a = {i1, i2, i3, Float.valueOf(line.split(" ")[4].split("/")[0])};
                        vertexInd = a;
                    } else {
                        float[] a = {i1, i2, i3};
                        vertexInd = a;
                    }
                    float[] textInd = null;
                    if (line.split(" ")[1].split("/")[1].length() > 0) {
                        i1 = Float.valueOf(line.split(" ")[1].split("/")[1]);
                        i2 = Float.valueOf(line.split(" ")[2].split("/")[1]);
                        i3 = Float.valueOf(line.split(" ")[3].split("/")[1]);
                        if (qu) {
                            float[] a = {i1, i2, i3, Float.valueOf(line.split(" ")[4].split("/")[1])};
                            textInd = a;
                        } else {
                            float[] a = {i1, i2, i3};
                            textInd = a;
                        }
                    }

                    i1 = Float.valueOf(line.split(" ")[1].split("/")[2]);
                    i2 = Float.valueOf(line.split(" ")[2].split("/")[2]);
                    i3 = Float.valueOf(line.split(" ")[3].split("/")[2]);
                    float[] normalInd = null;
                    if (qu) {
                        float[] a = {i1, i2, i3, Float.valueOf(line.split(" ")[4].split("/")[2])};
                        normalInd = a;
                    } else {
                        float[] a = {i1, i2, i3};
                        normalInd = a;
                    }
                    m.faces.add(new Face3D(vertexInd, normalInd, textInd, qu));
                }

            }
            line = reader.readLine();
        }

        reader.close();
        return m;
    }
}