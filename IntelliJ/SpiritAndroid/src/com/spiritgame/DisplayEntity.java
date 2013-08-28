package com.spiritgame;

import android.content.Context;

import javax.microedition.khronos.opengles.GL10;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.os.Environment;
import spiritshared.constants;

/**
 * @author jamesw
 */
public class DisplayEntity {

    private static Model3D test;
    private static Model3D test2;
    private static Model3D spirittest;
	private static Model3D spiritface;
    private static int spirittexture;
	private static int hermitagetexture;
	private static int selftexture;
	private Map<Integer, Integer> otherstexture = new HashMap<Integer, Integer>();
    private float[] localLoc;
    private float[] remoteLoc;
    private float[] direction;
    private float dx;
    private float dy;
    private int graphic;
	private Bitmap customtexture = null;
	private int customtextureind = -1;
    private long ID;
    private static float[] RED = {1.0f, 0.0f, 0.0f};
    private static float[] GREEN = {0.0f, 1.0f, 0.0f};
    private static float[] BLUE = {0.0f, 0.0f, 1.0f};
    private static float[] WHITE = {1.0f, 1.0f, 1.0f};
    public static float offsetX;
    public static float offsetY;
    private final static float SNAP_SPEED = 5.0f / 80.0f;
	private static Bitmap profile = null;

    public DisplayEntity(long I, int graph, float[] rL) {
        localLoc = rL.clone();
        remoteLoc = rL.clone();
        direction = new float[]{0, 0};
        ID = I;
        graphic = graph;
    }

    public void update(int g, float[] rL) {
        graphic = g;
        remoteLoc = rL;
        //System.out.println("x:" + remoteLoc[0] + "y:" + remoteLoc[1]);
    }

    public void tick() {
        float mag;
        //System.out.println("showing entity id "+ nextent);
        dx = (remoteLoc[0] - localLoc[0]);
        dy = (remoteLoc[1] - localLoc[1]);
        mag = dx * dx + dy * dy;


        //if (mag < SNAP_SPEED * SNAP_SPEED || mag > 100.0f) {
        if (mag < SNAP_SPEED * SNAP_SPEED / 10.0f) {
            //System.out.println("MAG over/underload");
            localLoc = remoteLoc.clone();
            if (mag != 0) {
                direction[0] = dx;
                direction[1] = dy;
            }
        } else {
            //System.out.println("dx:" + dx);
            //System.out.println("abs:" + Math.abs(dx));
            //System.out.println("(SNAP_SPEED * (dx * Math.abs(dx)))" + (SNAP_SPEED * (dx * Math.abs(dx))));
            //System.out.println("(SNAP_SPEED * (dx * Math.abs(dx))) / mag" + (SNAP_SPEED * (dx * Math.abs(dx))) / mag);
            //System.out.println("dy:" + dy);
            //System.out.println("abs:" + Math.abs(dy));
            //System.out.println("mag:" + mag);
            float magpow = (float) Math.pow(mag, 0.75f);
            direction[0] = SNAP_SPEED * (dx * Math.abs(dx)) / magpow;
            direction[1] = SNAP_SPEED * (dy * Math.abs(dy)) / magpow;
            //System.out.println(direction[0]);
            //System.out.println(direction[1]);
            localLoc[0] += direction[0];
            localLoc[1] += direction[1];
        }

        if (graphic == spiritshared.constants.GRAPHIC_VIEW) {
            offsetX = localLoc[0];
            offsetY = localLoc[1];
        }

    }
	public void loadCustomTexture(GL10 gl){
		if(customtexture!=null){
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
	        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, customtexture, 0);
	        System.out.println("GLERROR:"+gl.glGetError());
	//Enable texture related flags (Important)
	        gl.glEnable(GL10.GL_TEXTURE_2D);

	        if (textureHandle[0] == 0) {
	            throw new RuntimeException("Error loading texture.");
	        }
	        customtextureind = textureHandle[0];
		}
	}

    public void render(GL10 gl, float cycle) {
		if(customtexture!=null&&customtextureind==-1){
			loadCustomTexture(gl);
		}
        if (graphic == constants.GRAPHIC_SPIRIT) {
            drawModel(gl, localLoc[0], localLoc[1], 0.25f, 0.3f,
                    180.0f * (float) Math.atan2(direction[1], direction[0]) / (float) Math.PI, BLUE, spirittest);
			drawModel(gl, localLoc[0], localLoc[1], 0.5f, 0.3f,
           			180.0f * (float) Math.atan2(direction[1], direction[0]) / (float) Math.PI + 90.0f, WHITE, spiritface);
        } else if (graphic == 1) {
            //drawSquare(nextent[1], nextent[2], 3.0f, 1.0f, (float)Math.sin(cycle*2*Math.PI)*30, GREEN);
            drawModel(gl, localLoc[0], localLoc[1], 1f, 0.3f, (float) Math.sin(cycle * 2 * Math.PI) * 30, RED, test);
        } else if (graphic == constants.GRAPHIC_HERMITAGE) {
            //drawSquare(nextent[1], nextent[2], 0.0f, 2.0f, 0.0f, RED);
            drawModel(gl, localLoc[0], localLoc[1], 0.0f, 0.2f, 45.0f, GREEN, test2);
        } else if (graphic == constants.GRAPHIC_HERMIT) {
            drawModel(gl, localLoc[0], localLoc[1], 0.0f, 0.05f, 45.0f, GREEN, test);
        }
    }

    public static float getOffX() {
        return offsetX;
    }

    public static float getOffY() {
        return offsetY;
    }

    public void drawModel(GL10 gl, float x, float y, float h, float size, float rot, float[] color, Model3D model) {
        gl.glPushMatrix();
        gl.glTranslatef(x - offsetX, y - offsetY, h - 10.0f);
        gl.glRotatef(rot, 0.0f, 0.0f, 1.0f);
        gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f); // OBJ files loaded on wrong axis
        gl.glScalef(size, size, size);
        gl.glColor4f(color[0], color[1], color[2], 1.0f);
		if(customtextureind==-1) model.render(gl);
		else model.render(gl, customtextureind);
        /*if (model.textured) {
            gl.glEnable(gl.GL_TEXTURE_2D);
            gl.glBindTexture(gl.GL_TEXTURE_2D, model.getTexture());
        } else {
            gl.glDisable(gl.GL_TEXTURE_2D);
            gl.glBindTexture(gl.GL_TEXTURE_2D, 0);
        }
        for (Face3D face : model.faces) {
            if (face.quad) {
                float TextureCoords[] = null;
                if (model.textured) {
                    Vector3f t1 = model.textures.get((int) face.texture[0] - 1);
                    Vector3f t2 = model.textures.get((int) face.texture[1] - 1);
                    Vector3f t3 = model.textures.get((int) face.texture[2] - 1);
                    Vector3f t4 = model.textures.get((int) face.texture[3] - 1);
                    float temp[] =
                            {
                                    t1.x, t1.y,
                                    t2.x, t2.y,
                                    t3.x, t3.y,
                                    t4.x, t4.y
                            };
                    TextureCoords = temp;
                }
                Vector3f n1 = model.normals.get((int) face.normal[0] - 1);
                Vector3f n2 = model.normals.get((int) face.normal[1] - 1);
                Vector3f n3 = model.normals.get((int) face.normal[2] - 1);
                Vector3f n4 = model.normals.get((int) face.normal[3] - 1);
                float NormalCoords[] = {
                        n1.x, n1.y, n1.z,
                        n2.x, n2.y, n2.z,
                        n3.x, n3.y, n3.z,
                        n4.x, n4.y, n4.z
                };

                Vector3f v1 = model.verticies.get((int) face.vertex[0] - 1);
                Vector3f v2 = model.verticies.get((int) face.vertex[1] - 1);
                Vector3f v3 = model.verticies.get((int) face.vertex[2] - 1);
                Vector3f v4 = model.verticies.get((int) face.vertex[3] - 1);
                float VertexCoords[] = {
                        v1.x, v1.y, v1.z,
                        v2.x, v2.y, v2.z,
                        v3.x, v3.y, v3.z,
                        v4.x, v4.y, v4.z
                };
                short[] _Indices = {0, 1, 2, 0, 2, 3};
                FloatBuffer _VertexBuffer;
                ShortBuffer _IndexBuffer;
                FloatBuffer _TextureBuffer;
                FloatBuffer _NormalBuffer;
                ByteBuffer vbb = ByteBuffer.allocateDirect(VertexCoords.length * 4);
                vbb.order(ByteOrder.nativeOrder());

                _VertexBuffer = vbb.asFloatBuffer();
                _VertexBuffer.put(VertexCoords);
                _VertexBuffer.position(0);


                ByteBuffer nbb = ByteBuffer.allocateDirect(NormalCoords.length * 4);
                nbb.order(ByteOrder.nativeOrder());

                _NormalBuffer = nbb.asFloatBuffer();
                _NormalBuffer.put(NormalCoords);
                _NormalBuffer.position(0);

                ByteBuffer ibb = ByteBuffer.allocateDirect(_Indices.length * 2);
                ibb.order(ByteOrder.nativeOrder());

                _IndexBuffer = ibb.asShortBuffer();
                _IndexBuffer.put(_Indices);
                _IndexBuffer.position(0);
                if (model.textured) {
                    ByteBuffer tbb = ByteBuffer.allocateDirect(TextureCoords.length * 4);
                    tbb.order(ByteOrder.nativeOrder());

                    _TextureBuffer = tbb.asFloatBuffer();
                    _TextureBuffer.put(TextureCoords);
                    _TextureBuffer.position(0);

                    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, _TextureBuffer);
                }
                gl.glNormalPointer(3, GL10.GL_FLOAT, _NormalBuffer);
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, _VertexBuffer);
                gl.glDrawElements(GL10.GL_TRIANGLES, _Indices.length,
                        GL10.GL_UNSIGNED_SHORT, _IndexBuffer);

            } else {
                float TextureCoords[] = null;
                if (model.textured) {
                    Vector3f t1 = model.textures.get((int) face.texture[0] - 1);
                    Vector3f t2 = model.textures.get((int) face.texture[1] - 1);
                    Vector3f t3 = model.textures.get((int) face.texture[2] - 1);
                    float temp[] =
                            {
                                    t1.x, t1.y,
                                    t2.x, t2.y,
                                    t3.x, t3.y,
                            };
                    TextureCoords = temp;
                }
                Vector3f n1 = model.normals.get((int) face.normal[0] - 1);
                Vector3f n2 = model.normals.get((int) face.normal[1] - 1);
                Vector3f n3 = model.normals.get((int) face.normal[2] - 1);
                float NormalCoords[] = {
                        n1.x, n1.y, n1.z,
                        n2.x, n2.y, n2.z,
                        n3.x, n3.y, n3.z,
                };

                Vector3f v1 = model.verticies.get((int) face.vertex[0] - 1);
                Vector3f v2 = model.verticies.get((int) face.vertex[1] - 1);
                Vector3f v3 = model.verticies.get((int) face.vertex[2] - 1);
                float VertexCoords[] = {
                        v1.x, v1.y, v1.z,
                        v2.x, v2.y, v2.z,
                        v3.x, v3.y, v3.z,
                };
                short[] _Indices = {0, 1, 2};
                FloatBuffer _VertexBuffer;
                ShortBuffer _IndexBuffer;
                FloatBuffer _TextureBuffer;
                FloatBuffer _NormalBuffer;
                ByteBuffer vbb = ByteBuffer.allocateDirect(VertexCoords.length * 4);
                vbb.order(ByteOrder.nativeOrder());

                _VertexBuffer = vbb.asFloatBuffer();
                _VertexBuffer.put(VertexCoords);
                _VertexBuffer.position(0);


                ByteBuffer nbb = ByteBuffer.allocateDirect(NormalCoords.length * 4);
                nbb.order(ByteOrder.nativeOrder());

                _NormalBuffer = nbb.asFloatBuffer();
                _NormalBuffer.put(NormalCoords);
                _NormalBuffer.position(0);

                ByteBuffer ibb = ByteBuffer.allocateDirect(_Indices.length * 2);
                ibb.order(ByteOrder.nativeOrder());

                _IndexBuffer = ibb.asShortBuffer();
                _IndexBuffer.put(_Indices);
                _IndexBuffer.position(0);
                if (model.textured) {
                    ByteBuffer tbb = ByteBuffer.allocateDirect(TextureCoords.length * 4);
                    tbb.order(ByteOrder.nativeOrder());

                    _TextureBuffer = tbb.asFloatBuffer();
                    _TextureBuffer.put(TextureCoords);
                    _TextureBuffer.position(0);

                    gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, _TextureBuffer);
                }
                gl.glNormalPointer(3, GL10.GL_FLOAT, _NormalBuffer);
                gl.glVertexPointer(3, GL10.GL_FLOAT, 0, _VertexBuffer);
                gl.glDrawElements(GL10.GL_TRIANGLES, _Indices.length,
                        GL10.GL_UNSIGNED_SHORT, _IndexBuffer);
            }
        }*/
        gl.glPopMatrix();
    }

	public static void setProfile(Bitmap b){
		profile = b;
		System.out.println("Profile set. Size:"+profile.getHeight());
	}

    public static void initTextures(Context c, GL10 gl)  {
		try{
        	spirittexture = Model3D.loadTexture(c, R.drawable.spiritlayout, gl);
			hermitagetexture = Model3D.loadTexture(c, R.drawable.hermitage, gl);
		}catch(Exception e){
			e.printStackTrace();
		}
		try{
			if(profile!=null){
				Bitmap bm = profile.copy(profile.getConfig(), true);
				selftexture = Model3D.loadTexture(profile, gl);
				profile = bm;
			} else{
				selftexture = Model3D.loadTexture(c, R.drawable.capture, gl);
			}
		}catch(Exception e){
			System.out.println("profile pic error");
			e.printStackTrace();
		}
    }

	public void setCustomTexture(Bitmap b)  {
			customtexture = b;
	    }

    public static void initModels(Context c, GL10 gl) {
        try {
            System.out.println("open model");
            InputStream read = c.getResources().openRawResource(R.raw.bird);
            System.out.println("load model");
            System.out.println(read.available());
            test = Model3D.loadModel(read);

            read = c.getResources().openRawResource(R.raw.hermitage);
            System.out.println("load model");
            System.out.println(read.available());
            test2 = Model3D.loadModel(read);
			test2.setTexture(hermitagetexture);

            read = c.getResources().openRawResource(R.raw.spiritride);
            System.out.println("load model");
            System.out.println(read.available());
            spirittest = Model3D.loadModel(read);

			read = c.getResources().openRawResource(R.raw.spiritface);
			System.out.println("load model");
			System.out.println(read.available());
			spiritface = Model3D.loadModel(read);
			spiritface.setTexture(selftexture);
        } catch (Exception e) {
			System.out.println("Spirit Error"+e.toString());
            e.printStackTrace();
        }
    }
}

