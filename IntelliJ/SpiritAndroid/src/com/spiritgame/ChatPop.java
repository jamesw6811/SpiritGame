package com.spiritgame;

import android.content.res.AssetManager;
import android.graphics.Paint;
import android.graphics.Typeface;

import javax.microedition.khronos.opengles.GL10;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * @author jamesw
 */
public class ChatPop implements Pop {
    private String chat;
    private int durationmillis;
    private float[] location;
    private long startTime;
    private Paint font;
    private Typeface f;
    private int color;
    private float width;
    private GLText glText;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;
    private ByteBuffer mIndexBuffer;

    public ChatPop(String c, int dur, float[] l, Typeface f, int co) {
        this.f = f;
        chat = c;
        durationmillis = dur;
        startTime = System.currentTimeMillis();
        location = l;
        color = co;
        font = new Paint();
        font.setColor(color);
        font.setAntiAlias(true);
        font.setStrokeWidth(5);
        font.setStrokeCap(Paint.Cap.ROUND);
        font.setTextSize(24);
        font.setTypeface(f);
        width = font.measureText(chat);
        int one = 0x10000;
        float vertices[] = {
                -5 - width / 2, -5, 0,
                width / 2 + 5, -5, 0,
                width / 2 + 5, 24 + 5, 0,
                -5 - width / 2, 24 + 5, 0
        };

        float colors[] = {
                0, 0, 0, one,
                0, 0, 0, one,
                0, 0, 0, one,
                0, 0, 0, one
        };

        byte indices[] = {
                0, 1, 2, 0, 2, 3
        };

        // Buffers to be passed to gl*Pointer() functions
        // must be direct, i.e., they must be placed on the
        // native heap where the garbage collector cannot
        // move them.
        //
        // Buffers with multi-byte datatypes (e.g., short, int, float)
        // must have their byte order set to native order

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asFloatBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mColorBuffer = cbb.asFloatBuffer();
        mColorBuffer.put(colors);
        mColorBuffer.position(0);

        mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);


    }

    public boolean tick() {
        if (System.currentTimeMillis() > startTime + durationmillis) {
            System.out.println("Chat died");
            return false;

        } else {
            return true;
        }
    }

    public void render(float camAngleX, float camAngleY, GL10 gl) {
        if (glText == null) {
            glText = new GLText(gl);
            glText.load(f, 24, 0, 0);
        }

        //System.out.println("chatting at x: " + location[0] +" and y:" + location[1]);
        gl.glTranslatef(location[0], location[1], 0.5f);
        gl.glRotatef(-camAngleX, 0.0f, 0.0f, 1.0f);
        gl.glRotatef(-camAngleY, 1.0f, 0.0f, 0.0f);
        gl.glScalef(0.02f, 0.02f, 1.0f);
		gl.glColor4f(1f, 1f, 1f, (1.0f - 0.7f * (((float) (System.currentTimeMillis() - startTime)) / durationmillis)));
		gl.glDisable(gl.GL_TEXTURE_2D);
		gl.glDisable(gl.GL_LIGHTING);
		//gl.glFrontFace(GL10.GL_CW);
        mVertexBuffer.position(0);
        mIndexBuffer.position(0);
        mColorBuffer.position(0);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
		gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_BYTE, mIndexBuffer);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        gl.glEnable(GL10.GL_TEXTURE_2D);              // Enable Texture Mapping
        gl.glEnable(GL10.GL_BLEND);                   // Enable Alpha Blend
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);  // Set Alpha Blend Function
        // TEST: render some strings with the font
        glText.begin(1f, 1f, 1f,
                (1.0f - 0.7f * (((float) (System.currentTimeMillis() - startTime)) / durationmillis)));         // Begin Text Rendering (Set Color BLUE)
        glText.draw(chat, -width / 2, 0);        // Draw Test String
        glText.end();                                   // End Text Rendering

        // disable texture + alpha
        gl.glDisable(GL10.GL_BLEND);                  // Disable Alpha Blend
        gl.glDisable(GL10.GL_TEXTURE_2D);             // Disable Texture Mapping

        gl.glEnable(gl.GL_LIGHTING);
        gl.glEnable(gl.GL_TEXTURE_2D);
    }


}
