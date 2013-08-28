/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameclient;

import org.newdawn.slick.Color;
import org.newdawn.slick.UnicodeFont;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

/**
 *
 * @author jamesw
 */
public class ChatPop implements Pop{
    private String chat;
    private int durationmillis;
    private float[] location;
    private long startTime;
    private UnicodeFont font;
    private Color color;
    
    public ChatPop(String c, int dur, float[] l, UnicodeFont f, Color co){
        chat = c;
        durationmillis = dur;
        startTime = System.currentTimeMillis();
        location = l;
        color = co;
        font = f;
    }

    @Override
    public boolean tick() {
        if(System.currentTimeMillis()>startTime+durationmillis) {
            System.out.println("Chat died");
            return false;
            
        }
        else {
            return true;
        }
    }

    @Override
    public void render(float camAngle) {
        //System.out.println("chatting at x: " + location[0] +" and y:" + location[1]);
        glTranslatef(location[0], location[1], 0.5f);
        glRotatef(-camAngle, 0.0f, 0.0f, 1.0f);
        glRotatef(180.0f, 1.0f, 0.0f, 0.0f);
        glScalef(0.02f, 0.02f, 1.0f);
        glColor4f(1f, 1f, 1f,(1.0f-0.7f*(((float)(System.currentTimeMillis()-startTime))/durationmillis)));
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex2f(-5-font.getWidth(chat)/2, -5);
        glTexCoord2f(1.0f, 0.0f);
        glVertex2f(font.getWidth(chat)/2+5, -5);
        glTexCoord2f(1.0f, 1.0f);
        glVertex2f(font.getWidth(chat)/2+5, 24+5);
        glTexCoord2f(0.0f, 1.0f);
        glVertex2f(-5-font.getWidth(chat)/2, 24+5);
        glEnd();
        
        color.a=(1.0f-0.7f*(((float)(System.currentTimeMillis()-startTime))/durationmillis));
        font.drawString(-font.getWidth(chat)/2, 0, chat, color);
        
        glEnable(GL_LIGHTING);
        glEnable(GL_TEXTURE_2D);
    }
    
}
