/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameclient;

/**
 *
 * @author jamesw
 */
public interface Pop {
    public abstract boolean tick();
    public abstract void render(float camAngle);
}
