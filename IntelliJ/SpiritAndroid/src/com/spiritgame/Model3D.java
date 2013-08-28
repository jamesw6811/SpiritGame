package com.spiritgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

import javax.microedition.khronos.opengles.GL10;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * @author jamesw
 */
public class Model3D {

    public List<Vector3f> verticies = new ArrayList<Vector3f>();
    public List<Vector3f> normals = new ArrayList<Vector3f>();
    public List<Vector3f> textures = new ArrayList<Vector3f>();
    public List<Face3D> faces = new ArrayList<Face3D>();
    FloatBuffer _VertexBuffer;
    ShortBuffer _IndexBuffer;
    FloatBuffer _TextureBuffer;
    FloatBuffer _NormalBuffer;
    public boolean textured;
    public boolean hasTextCoords;
    private int texture;
    private int quads = 0;
    private int tris = 0;
    private int numinds;

    public Model3D() {
    }

    public void setTexture(int t) {
        textured = true;
        texture = t;
    }

    public int getTexture() {
        return texture;
    }
    private void makeBuffers(){
        LinkedList<Float> verts = new LinkedList<Float>();
        LinkedList<Float> texts = new LinkedList<Float>();
        LinkedList<Float> norms = new LinkedList<Float>();
        LinkedList<Integer> inds = new LinkedList<Integer>();
        short x = 0;
        for(Face3D face : faces){
            if(face.quad){
                Float TextureCoords[] = null;
                if (hasTextCoords) {
                    Vector3f t1 = textures.get((int) face.texture[0] - 1);
                    Vector3f t2 = textures.get((int) face.texture[1] - 1);
                    Vector3f t3 = textures.get((int) face.texture[2] - 1);
                    Vector3f t4 = textures.get((int) face.texture[3] - 1);
                    Float temp[] =
                            {
                                    t1.x, t1.y,
                                    t2.x, t2.y,
                                    t3.x, t3.y,
                                    t4.x, t4.y
                            };
                    TextureCoords = temp;
                    texts.addAll(Arrays.asList(TextureCoords));
                }
                Vector3f n1 = normals.get((int) face.normal[0] - 1);
                Vector3f n2 = normals.get((int) face.normal[1] - 1);
                Vector3f n3 = normals.get((int) face.normal[2] - 1);
                Vector3f n4 = normals.get((int) face.normal[3] - 1);
                Float NormalCoords[] = {
                        n1.x, n1.y, n1.z,
                        n2.x, n2.y, n2.z,
                        n3.x, n3.y, n3.z,
                        n4.x, n4.y, n4.z
                };
                norms.addAll(Arrays.asList(NormalCoords));
                Vector3f v1 = verticies.get((int) face.vertex[0] - 1);
                Vector3f v2 = verticies.get((int) face.vertex[1] - 1);
                Vector3f v3 = verticies.get((int) face.vertex[2] - 1);
                Vector3f v4 = verticies.get((int) face.vertex[3] - 1);
                Float VertexCoords[] = {
                        v1.x, v1.y, v1.z,
                        v2.x, v2.y, v2.z,
                        v3.x, v3.y, v3.z,
                        v4.x, v4.y, v4.z
                };
                verts.addAll(Arrays.asList(VertexCoords));

                Integer[] _Indices = {x+0, x+1, x+2, x+0, x+2, x+3};
                inds.addAll(Arrays.asList(_Indices));
                x+=4;
            }   else {

                Float TextureCoords[] = null;
                if (hasTextCoords) {
                    Vector3f t1 = textures.get((int) face.texture[0] - 1);
                    Vector3f t2 = textures.get((int) face.texture[1] - 1);
                    Vector3f t3 = textures.get((int) face.texture[2] - 1);
                    Float temp[] =
                            {
                                    t1.x, t1.y,
                                    t2.x, t2.y,
                                    t3.x, t3.y
                            };
                    TextureCoords = temp;
                    texts.addAll(Arrays.asList(TextureCoords));
                }
                Vector3f n1 = normals.get((int) face.normal[0] - 1);
                Vector3f n2 = normals.get((int) face.normal[1] - 1);
                Vector3f n3 = normals.get((int) face.normal[2] - 1);
                Float NormalCoords[] = {
                        n1.x, n1.y, n1.z,
                        n2.x, n2.y, n2.z,
                        n3.x, n3.y, n3.z
                };
                norms.addAll(Arrays.asList(NormalCoords));
                Vector3f v1 = verticies.get((int) face.vertex[0] - 1);
                Vector3f v2 = verticies.get((int) face.vertex[1] - 1);
                Vector3f v3 = verticies.get((int) face.vertex[2] - 1);
                Float VertexCoords[] = {
                        v1.x, v1.y, v1.z,
                        v2.x, v2.y, v2.z,
                        v3.x, v3.y, v3.z
                };
                verts.addAll(Arrays.asList(VertexCoords));

                Integer[] _Indices = {x+0, x+1, x+2};
                inds.addAll(Arrays.asList(_Indices));
                x+=3;
            }


        }
        ByteBuffer vbb = ByteBuffer.allocateDirect(verts.size() * 4);
        vbb.order(ByteOrder.nativeOrder());

        _VertexBuffer = vbb.asFloatBuffer();
        _VertexBuffer.put(toPrimFloat(verts));
        _VertexBuffer.position(0);


        ByteBuffer nbb = ByteBuffer.allocateDirect(norms.size() * 4);
        nbb.order(ByteOrder.nativeOrder());

        _NormalBuffer = nbb.asFloatBuffer();
        _NormalBuffer.put(toPrimFloat(norms));
        _NormalBuffer.position(0);

        ByteBuffer ibb = ByteBuffer.allocateDirect(inds.size() * 2);
        ibb.order(ByteOrder.nativeOrder());

        _IndexBuffer = ibb.asShortBuffer();
        _IndexBuffer.put(toPrimInt(inds));
        numinds = inds.size();
        _IndexBuffer.position(0);
        if (hasTextCoords) {
            ByteBuffer tbb = ByteBuffer.allocateDirect(texts.size() * 4);
            tbb.order(ByteOrder.nativeOrder());

            _TextureBuffer = tbb.asFloatBuffer();
            _TextureBuffer.put(toPrimFloat(texts));
            _TextureBuffer.position(0);
        }
    }
    public void render(GL10 gl){
        render(gl, getTexture());
    }
	public void render(GL10 gl, int customtexture){
	        _VertexBuffer.position(0);
	        _NormalBuffer.position(0);
	        _IndexBuffer.position(0);
	        if (textured) {
	            _TextureBuffer.position(0);
	            gl.glEnable(gl.GL_TEXTURE_2D);
	            gl.glColor4f(1f, 1f, 1f, 1f);
	            gl.glBindTexture(gl.GL_TEXTURE_2D, customtexture);
	            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, _TextureBuffer);
	        } else {
	            gl.glDisable(gl.GL_TEXTURE_2D);
	            gl.glBindTexture(gl.GL_TEXTURE_2D, 0);
	        }
	        gl.glNormalPointer(3, GL10.GL_FLOAT, _NormalBuffer);
	        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, _VertexBuffer);
	        gl.glDrawElements(GL10.GL_TRIANGLES, numinds,
	                GL10.GL_UNSIGNED_SHORT, _IndexBuffer);
	        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
	    }
    private float[] toPrimFloat(LinkedList<Float> x){
        float[] y = new float[x.size()];
        for(int z = 0; z<x.size(); z++){
            y[z] = x.get(z);
        }
        return y;
    }
    private short[] toPrimInt(LinkedList<Integer> x){
        short[] y = new short[x.size()];
        for(int z = 0; z<x.size(); z++){
            y[z] = (short)(int)x.get(z);
        }
        return y;
    }

	public static int loadTexture(final Context context, final int resourceId, GL10 gl) {
	        //First setup the integer array to hold texture numbers which OpenGL generates
	        int textureHandle[] = new int[1];

	//Generate and bind to the texture (gl is my GL10 object)
	        gl.glGenTextures(1, textureHandle, 0);
	        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureHandle[0]);

	        Bitmap wood = BitmapFactory.decodeResource(context.getResources(), resourceId);

	//Setup optional texture parameters
	        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
	        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

	//Set the texture image
	        gl.glGetError();
	        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, wood, 0);
	        System.out.println("GLERROR:"+gl.glGetError());
	//Enable texture related flags (Important)
	        gl.glEnable(GL10.GL_TEXTURE_2D);

	        if (textureHandle[0] == 0) {
	            throw new RuntimeException("Error loading texture.");
	        }
			wood.recycle();
	        return textureHandle[0];
	    }
	public static int loadTexture(Bitmap wood, GL10 gl) {
	        //First setup the integer array to hold texture numbers which OpenGL generates
	        int textureHandle[] = new int[1];

	//Generate and bind to the texture (gl is my GL10 object)
	        gl.glGenTextures(1, textureHandle, 0);
	        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureHandle[0]);


	//Setup optional texture parameters
	        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
	        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

	//Set the texture image
	        gl.glGetError();
	        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, wood, 0);
	        System.out.println("GLERROR:"+gl.glGetError());
	//Enable texture related flags (Important)
	        gl.glEnable(GL10.GL_TEXTURE_2D);

	        if (textureHandle[0] == 0) {
	            throw new RuntimeException("Error loading texture.");
	        }
	        return textureHandle[0];
	    }


    public static Model3D loadModel(InputStream f) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(f));
        Model3D m = new Model3D();
        String line = reader.readLine();

        while (line != null) {
			try{
            if (line.startsWith("v ")) {
                float x = Float.valueOf(line.split(" ")[1]);
                float y = Float.valueOf(line.split(" ")[2]);
                float z = Float.valueOf(line.split(" ")[3]);
                m.verticies.add(new Vector3f(x, y, z));
            }
            if (line.startsWith("vt ")) {
                m.hasTextCoords = true;
                float x = Float.valueOf(line.split(" ")[1]);
                float y = Float.valueOf(line.split(" ")[2]);
                m.textures.add(new Vector3f(x, 1.0f - y, 0));
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
                    m.quads++;
                    qu = true;
                }
                if (sp.length == 4) {
                    m.tris++;
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
			}catch (Exception e){
				e.printStackTrace();
				System.out.println("Spirit Model read line:"+line);
				throw new RuntimeException("model load error");
			}
        }

        reader.close();
        m.makeBuffers();
        return m;
    }
}
