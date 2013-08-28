/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameclient;

import java.io.InputStream;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.util.ResourceLoader;
import spiritshared.constants;
import static org.lwjgl.opengl.GL11.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

/**
 *
 * @author jamesw
 */
public class DisplayEntity {

    private static Model3D test;
    private static Model3D test2;
    private static Model3D spirittest;
    private static Texture spirittexture;
    private float[] localLoc;
    private float[] remoteLoc;
    private float[] direction;
    private float dx;
    private float dy;
    private int graphic;
    private long ID;
    private static float[] RED = {1.0f, 0.0f, 0.0f};
    private static float[] GREEN = {0.0f, 1.0f, 0.0f};
    private static float[] BLUE = {0.0f, 0.0f, 1.0f};
    private static float[] WHITE = {1.0f, 1.0f, 1.0f};
    public static float offsetX;
    public static float offsetY;
    private final static float SNAP_SPEED = 5.0f / 80.0f;

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
        if (mag < SNAP_SPEED * SNAP_SPEED/10.0f) {
            //System.out.println("MAG over/underload");
            localLoc = remoteLoc.clone();
            if(mag!=0){
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

        if (graphic == constants.GRAPHIC_VIEW) {
            offsetX = localLoc[0];
            offsetY = localLoc[1];
        }

    }

    public void render(float cycle) {
        if (graphic == 0) {
            //drawCube(nextent[1], nextent[2], 0.01f, 0.5f, 0.0f, WHITE);
            drawModel(localLoc[0], localLoc[1], 0.5f, 0.3f, 180.0f * (float) Math.atan2(direction[1], direction[0]) / (float) Math.PI, WHITE, spirittest);
        } else if (graphic == 1) {
            //drawSquare(nextent[1], nextent[2], 3.0f, 1.0f, (float)Math.sin(cycle*2*Math.PI)*30, GREEN);
            drawModel(localLoc[0], localLoc[1], 1f, 0.3f, (float) Math.sin(cycle * 2 * Math.PI) * 30, RED, test);
        } else if (graphic == 2) {
            //drawSquare(nextent[1], nextent[2], 0.0f, 2.0f, 0.0f, RED);
            drawModel(localLoc[0], localLoc[1], 0.0f, 1.0f, 45.0f, GREEN, test2);
        } else if (graphic == 3) {
            drawCube(localLoc[0], localLoc[1], 0.25f, (float) Math.sin(cycle * 2 * Math.PI) * 0.05f + 0.1f, 0.0f, GREEN);
        }
    }

    public static float getOffX() {
        return offsetX;
    }

    public static float getOffY() {
        return offsetY;
    }

    public void drawModel(float x, float y, float h, float size, float rot, float[] color, Model3D model) {
        glPushMatrix();
        glTranslatef(x - offsetX, y - offsetY, h - 10.0f);
        glRotatef(rot, 0.0f, 0.0f, 1.0f);
        glRotatef(90.0f, 1.0f, 0.0f, 0.0f); // OBJ files loaded on wrong axis
        glScalef(size, size, size);
        glColor3f(color[0], color[1], color[2]);
        if(model.textured){
            glEnable(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, model.getTexture().getTextureID());
        } else {
            glDisable(GL_TEXTURE_2D);
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        for (Face3D face : model.faces) {
            if (face.quad) {
                glBegin(GL_QUADS);
                if (model.textured) {
                    Vector3f t1 = model.textures.get((int) face.texture[0] - 1);
                    glTexCoord2f(t1.x, t1.y);
                }
                Vector3f n1 = model.normals.get((int) face.normal[0] - 1);
                glNormal3f(n1.x, n1.y, n1.z);
                Vector3f v1 = model.verticies.get((int) face.vertex[0] - 1);
                glVertex3f(v1.x, v1.y, v1.z);
                
                if (model.textured) {
                    Vector3f t2 = model.textures.get((int) face.texture[1] - 1);
                    glTexCoord2f(t2.x, t2.y);
                }
                Vector3f n2 = model.normals.get((int) face.normal[1] - 1);
                glNormal3f(n2.x, n2.y, n2.z);
                Vector3f v2 = model.verticies.get((int) face.vertex[1] - 1);
                glVertex3f(v2.x, v2.y, v2.z);
                
                if (model.textured) {
                    Vector3f t3 = model.textures.get((int) face.texture[2] - 1);
                    glTexCoord2f(t3.x, t3.y);
                }
                Vector3f n3 = model.normals.get((int) face.normal[2] - 1);
                glNormal3f(n3.x, n3.y, n3.z);
                Vector3f v3 = model.verticies.get((int) face.vertex[2] - 1);
                glVertex3f(v3.x, v3.y, v3.z);
                
                if (model.textured) {
                    Vector3f t4 = model.textures.get((int) face.texture[3] - 1);
                    glTexCoord2f(t4.x, t4.y);
                }
                Vector3f n4 = model.normals.get((int) face.normal[3] - 1);
                glNormal3f(n4.x, n4.y, n4.z);
                Vector3f v4 = model.verticies.get((int) face.vertex[3] - 1);
                glVertex3f(v4.x, v4.y, v4.z);
                glEnd();
            } else {
                glBegin(GL_TRIANGLES);
                if (model.textured) {
                    Vector3f t1 = model.textures.get((int) face.texture[0] - 1);
                    glTexCoord2f(t1.x, t1.y);
                }
                Vector3f n1 = model.normals.get((int) face.normal[0] - 1);
                glNormal3f(n1.x, n1.y, n1.z);
                Vector3f v1 = model.verticies.get((int) face.vertex[0] - 1);
                glVertex3f(v1.x, v1.y, v1.z);
                
                if (model.textured) {
                    Vector3f t2 = model.textures.get((int) face.texture[1] - 1);
                    glTexCoord2f(t2.x, t2.y);
                }
                Vector3f n2 = model.normals.get((int) face.normal[1] - 1);
                glNormal3f(n2.x, n2.y, n2.z);
                Vector3f v2 = model.verticies.get((int) face.vertex[1] - 1);
                glVertex3f(v2.x, v2.y, v2.z);
                
                if (model.textured) {
                    Vector3f t3 = model.textures.get((int) face.texture[2] - 1);
                    glTexCoord2f(t3.x, t3.y);
                }
                Vector3f n3 = model.normals.get((int) face.normal[2] - 1);
                glNormal3f(n3.x, n3.y, n3.z);
                Vector3f v3 = model.verticies.get((int) face.vertex[2] - 1);
                glVertex3f(v3.x, v3.y, v3.z);
                glEnd();
            }
        }
        glPopMatrix();
    }

    public void drawCube(float x, float y, float h, float size, float rot, float[] color) {
        glPushMatrix();
        glTranslatef(x - offsetX, y - offsetY, h - 10.0f);
        glRotatef(rot, 0.0f, 0.0f, 1.0f);
        glColor3f(color[0], color[1], color[2]);
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3f(-size, -size, size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3f(size, -size, size);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3f(size, size, size);
        glTexCoord2f(0.0f, 1.0f);
        glVertex3f(-size, size, size);
        glEnd();
        glColor3f((0.25f + color[0]) % 1.0f, (0.85f + color[1]) % 1.0f, (0.25f + color[2]) % 1.0f);
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3f(-size, -size, 0.0f);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3f(size, -size, 0.0f);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3f(size, -size, size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3f(-size, -size, size);
        glEnd();
        glColor3f((0.75f + color[0]) % 1.0f, (0.25f + color[1]) % 1.0f, (0.25f + color[2]) % 1.0f);
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3f(size, -size, 0.0f);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3f(size, size, 0.0f);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3f(size, size, size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3f(size, -size, size);
        glEnd();
        glColor3f((0.25f + color[0]) % 1.0f, (0.25f + color[1]) % 1.0f, (0.85f + color[2]) % 1.0f);
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3f(size, size, 0.0f);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3f(-size, size, 0.0f);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3f(-size, size, size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3f(size, size, size);
        glEnd();
        glColor3f((0.75f + color[0]) % 1.0f, (0.25f + color[1]) % 1.0f, (0.75f + color[2]) % 1.0f);
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex3f(-size, -size, 0.0f);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3f(-size, size, 0.0f);
        glTexCoord2f(1.0f, 1.0f);
        glVertex3f(-size, size, size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex3f(-size, -size, size);
        glEnd();
        glPopMatrix();
    }

    private static void initTextures() throws Exception {
        spirittexture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("models/spiritlayout.png"));
    }

    public static void initModels() {
        try {
            initTextures();
            System.out.println("Resource exisits?" + ResourceLoader.resourceExists("models/bird.obj"));
            System.out.println("open model");
            InputStream read = ResourceLoader.getResourceAsStream("models/bird.obj");
            System.out.println("load model");
            System.out.println(read.available());
            test = Model3D.loadModel(read);

            read = ResourceLoader.getResourceAsStream("models/housefarm.obj");
            System.out.println("load model");
            System.out.println(read.available());
            test2 = Model3D.loadModel(read);

            read = ResourceLoader.getResourceAsStream("models/spirit.obj");
            System.out.println("load model");
            System.out.println(read.available());
            spirittest = Model3D.loadModel(read);
            spirittest.setTexture(spirittexture);

            System.out.println("model loaded. verts:" + test.verticies.size() + " norms:" + test.normals.size() + " faces:" + test.faces.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
