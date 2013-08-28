/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameserver;

/**
 *
 * @author jamesw
 */
public class Chat {
    private String chat;
    private Entity ent;
    public Chat(String c, Entity e){
        chat = c;
        ent = e;
    }

    public String getChat() {
        return chat;
    }

    public void setChat(String chat) {
        this.chat = chat;
    }

    public Entity getEntity() {
        return ent;
    }

    public void setEntity(Entity ent) {
        this.ent = ent;
    }
    
}
