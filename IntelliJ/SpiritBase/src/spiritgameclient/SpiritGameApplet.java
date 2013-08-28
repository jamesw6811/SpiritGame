/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package spiritgameclient;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Font;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import spiritshared.LoginInfo;
import spiritshared.constants;

/**
 *
 * @author jamesw
 */
public class SpiritGameApplet extends Applet implements SpiritClient {

    private HashMap<Long, DisplayEntity> entities;
    private int moveX;
    private int moveY;
    private float offsetX;
    private float offsetY;
    private long lastMouseClick;
    private LinkedList<Pop> pops;
    private LinkedList<Integer> choice;
    private ServerCommunicator server;
    private boolean chatting = false;
    private String currentchat = "";
    private UnicodeFont font;
    private UnicodeFont boldfont;
    private float cycle;
    private float camAngle;
    private String login;
    private String password;
    Canvas display_parent;
    /**
     * Thread which runs the main game loop
     */
    Thread gameThread;
    /**
     * is the game loop running
     */
    boolean running = false;

    public void startLWJGL() {
        gameThread = new Thread() {
            public void run() {
                running = true;
                try {
                    Display.setParent(display_parent);
                    Display.create();
                    gameLoop();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        };
        gameThread.start();
    }

    /**
     * Tell game loop to stop running, after which the LWJGL Display will be
     * destoryed. The main thread will wait for the Display.destroy().
     */
    private void stopLWJGL() {
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void start() {
    }

    public void stop() {
    }

    /**
     * Applet Destroy method will remove the canvas, before canvas is destroyed
     * it will notify stopLWJGL() to stop the main game loop and to destroy the
     * Display
     */
    public void destroy() {
        server.destroy();
        running = false;
        remove(display_parent);
        super.destroy();
    }

    public void init() {
        setLayout(new BorderLayout());
        try {
            display_parent = new Canvas() {
                public final void addNotify() {
                    super.addNotify();
                    startLWJGL();
                }

                public final void removeNotify() {
                    stopLWJGL();
                    super.removeNotify();
                }
            };
            display_parent.setSize(getWidth(), getHeight());
            add(display_parent);
            display_parent.setFocusable(true);
            display_parent.requestFocus();
            display_parent.setIgnoreRepaint(true);

            setVisible(true);
        } catch (Exception e) {
            System.err.println(e);
            throw new RuntimeException("Unable to create display");
        }

    }

    public boolean initNetwork(LoginInfo li) throws Exception {
        System.out.println("initing network");
        if (server != null) {
            server.destroy();
        }
        server = new ServerCommunicator(this, li);
        new Thread(server).start();
        Thread.sleep(1000);
        System.out.println("init done" + server.loggedin());
        return server.loggedin();
    }

    public void gameLoop() throws Exception {
        Keyboard.create();
        Mouse.setGrabbed(false);
        Mouse.create();
        entities = new HashMap<Long, DisplayEntity>();
        choice = new LinkedList<Integer>();
        pops = new LinkedList<Pop>();
        initFont();
        initGL();
        DisplayEntity.initModels();
        initCamera();
        boolean loggedin = false;
        while (!loggedin) {
            loggedin = initNetwork(getLogin());
        }
        while (running) {
            moveEnts();
            tickPops();
            if (Display.isVisible()) {
                processKeyboard();
                processMouse();
                render();
            } else {
                if (Display.isDirty()) {
                    render();
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
            }
            Display.sync(60);
            Display.update();
        }

        Display.destroy();
    }

    private LoginInfo getLogin() {
        currentchat = "";
        login = null;
        password = null;
        make2D();
        while (true) {
            if (Display.isVisible()) {
                renderLogin();
                if (processLoginInput()) {
                    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                    Display.update();
                    try{
                        return new LoginInfo(login, password.getBytes("UTF-8"));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            Display.sync(60);
            Display.update();
        }
    }

    private void renderLogin() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDisable(GL_TEXTURE_2D);
        glLoadIdentity();
        glTranslatef(50.0f, 300.0f, 0.0f);
        glRotatef(180.0f, 1.0f, 0.0f, 0.0f);
        String toprint = "Login>";
        if (login != null) {
            toprint += login + "\nPassword>";
        }
        if (password != null) {

            toprint += password.replaceAll(".", "*") + "\nClick to login. Return to return.";
        }
        if (login == null) {
            toprint += currentchat;
        } else {
            toprint += currentchat.replaceAll(".", "*");
        }

        font.drawString(0, 0, toprint, Color.white);
    }

    private boolean processLoginInput() {
        while (Keyboard.next()) {
            if (!Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
                if (login == null) {
                    login = currentchat;
                } else if (password == null) {
                    password = currentchat;
                } else {
                    login = null;
                    password = null;
                }
                currentchat = "";
            } else {
                if (Keyboard.getEventKeyState() && !Keyboard.isRepeatEvent() && Keyboard.getEventCharacter() != Keyboard.CHAR_NONE) {
                    if (Keyboard.getEventKey() == Keyboard.KEY_BACK && currentchat.length() > 0) {
                        currentchat = currentchat.substring(0, currentchat.length() - 1);
                    } else {
                        //System.out.println(Keyboard.getEventCharacter());
                        currentchat += Keyboard.getEventCharacter();
                    }
                }
            }
        }
        boolean clicked = false;
        while (Mouse.next() && Mouse.getEventButtonState());
        if (password != null && Mouse.getEventButtonState()) {
            clicked = true;
        }
        while (Mouse.next());
        //System.out.println("end mouse");
        return clicked;
    }

    protected void initFont() {
        Font awtFont = new Font("Times New Roman", Font.PLAIN, 24);
        font = new UnicodeFont(awtFont);
        font.getEffects().add(new ColorEffect(java.awt.Color.white));
        font.addAsciiGlyphs();
        try {
            font.loadGlyphs();
        } catch (SlickException ex) {
            // Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
        awtFont = new Font("Times New Roman", Font.BOLD, 24);
        boldfont = new UnicodeFont(awtFont);
        boldfont.getEffects().add(new ColorEffect(java.awt.Color.white));
        boldfont.addAsciiGlyphs();
        try {
            boldfont.loadGlyphs();
        } catch (SlickException ex) {
            // Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void cancelChoose() {
        choice.clear();
        server.doAction(constants.CLEAR_ACTION);
    }

    protected void addChoice(int c) {
        choice.add(c);
        if (choice.size() == constants.CHOICE_DEPTH) {
            LinkedList<Integer> action = choice;
            choice = new LinkedList<Integer>();
            doChoice(action);
        }
    }

    protected void doChoice(LinkedList<Integer> action) {
        Iterator<Integer> it = action.iterator();
        int choice = 0;
        int height = 1;
        while (it.hasNext()) {
            choice += height * it.next();
            height *= 5;
        }
        server.doAction(choice);
    }

    public void processKeyboard() throws Exception {
        //Square's Size
        if (chatting) {
            processChat();
        } else {


            while (Keyboard.next()) {
                if (Keyboard.getEventKey() == Keyboard.KEY_RETURN && !Keyboard.getEventKeyState() && getMouseDelta() > 500) {
                    chatting = true;
                }
                if (Keyboard.getEventKeyState() && !Keyboard.isRepeatEvent()) {
                    if (Keyboard.getEventKey() == (Keyboard.KEY_SPACE) && getMouseDelta() > 50) {
                        cancelChoose();
                    }
                    if (Keyboard.getEventKey() == (Keyboard.KEY_1) && getMouseDelta() > 50) {
                        addChoice(1);
                    }
                    if (Keyboard.getEventKey() == (Keyboard.KEY_2) && getMouseDelta() > 50) {
                        addChoice(2);
                    }
                    if (Keyboard.getEventKey() == (Keyboard.KEY_3) && getMouseDelta() > 50) {
                        addChoice(3);
                    }
                    if (Keyboard.getEventKey() == (Keyboard.KEY_4) && getMouseDelta() > 50) {
                        addChoice(4);
                    }
                }
            }
        }
    }

    protected void processChat() throws Exception {
        while (Keyboard.next()) {
            if (!Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
                server.chat(currentchat);
                currentchat = "";
                chatting = false;
            } else {
                if (Keyboard.getEventKeyState() && !Keyboard.isRepeatEvent() && Keyboard.getEventCharacter() != Keyboard.CHAR_NONE) {
                    if (Keyboard.getEventKey() == Keyboard.KEY_BACK && currentchat.length() > 0) {
                        currentchat = currentchat.substring(0, currentchat.length() - 1);
                    } else {
                        //System.out.println(Keyboard.getEventCharacter());
                        currentchat += Keyboard.getEventCharacter();
                    }
                }
            }
        }
    }

    public void doChat(int chatID, int[] chatpos, String chat) {
        float[] chatposf = {((float) chatpos[0]) / 100.0f, ((float) chatpos[1]) / 100.0f};
        ChatPop cp = new ChatPop(chat, 5000, chatposf, boldfont, Color.blue);
        synchronized (pops) {
            pops.add(cp);
        }
    }

    /**
     * Get the time in milliseconds
     *
     * @return The system time in milliseconds
     */
    public long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    private int getMouseDelta() {
        long time = getTime();
        int delta = (int) (time - lastMouseClick);

        return delta;
    }

    private void setMouseDelta() {
        long time = getTime();
        lastMouseClick = time;
    }

    public void processMouse() throws Exception {
        //System.out.println("start mouse");
        if (Mouse.getX() < getWidth() / 12.0f) {
            camAngle -= 1.0f;
        }
        if (Mouse.getX() > 11.0f * getWidth() / 12.0f) {
            camAngle += 1.0f;
        }
        boolean found = false;
        while (Mouse.next()) {
            if (!Mouse.getEventButtonState()
                    && !(Mouse.getEventButton() == -1)
                    && getMouseDelta() > 50) {
                float mycos = (float) Math.cos(camAngle * Math.PI / 180.0f);
                float mysin = (float) Math.sin(camAngle * Math.PI / 180.0f);
                float mx = (float) Mouse.getEventX() * 2.0f - getWidth();
                float my = (float) Mouse.getEventY() * 2.0f - getHeight();
                moveX = (int) (mx * mycos + my * mysin);
                moveY = (int) (-mx * mysin + my * mycos);
                if (Mouse.getEventButton() == 0) {
                    server.move(moveX, moveY);
                    setMouseDelta();
                    System.out.println("MOVED to " + moveX + " , " + moveY);
                }
                if (Mouse.getEventButton() == 1) {
                    server.mouseSelect(moveX, moveY);
                    setMouseDelta();
                    System.out.println("Selected to " + moveX + " , " + moveY);
                }

            }
        }

    }

    public synchronized void moveEnts() {
        Iterator<DisplayEntity> it = entities.values().iterator();
        while (it.hasNext()) {
            DisplayEntity nextent = it.next();
            nextent.tick();
        }

    }

    protected void resizeGL() {

        glViewport(0, 0, getWidth(), getHeight());
        glMatrixMode(GL_PROJECTION);                        // Select The Projection Matrix
        glLoadIdentity();                           // Reset The Projection Matrix
        // Calculate The Aspect Ratio Of The Window
        gluPerspective(45.0f, (float) getWidth() / (float) getHeight(), 0.1f, 100.0f);
        glMatrixMode(GL_MODELVIEW);                     // Select The Modelview Matrix
        glLoadIdentity();// Reset The Modelview Matrix

        glTranslatef(0.0f, 0.0f, -10.0f);
        glRotatef(-45.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(camAngle, 0.0f, 0.0f, 1.0f);
        glTranslatef(0.0f, 0.0f, 10.0f);

    }

    protected void initGL() {
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);                   // Black Background
        glClearDepth(1.0f);                         // Depth Buffer Setup
        glEnable(GL_DEPTH_TEST);                        // Enables Depth Testing
        glDepthFunc(GL_LEQUAL);                         // The Type Of Depth Test To Do
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);          // Really Nice Perspective Calculations
    }

    protected void initCamera() {
        camAngle = 0.0f;
    }

    public void render() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);         // Clear The Screen And The Depth Buffer

        cycle = ((float) (System.currentTimeMillis() % 1000)) / 1000.0f;
        make3D();
        renderContent();
        make2D();
        renderGUI();
    }

    public void renderContent() {
        offsetX = DisplayEntity.getOffX();
        offsetY = DisplayEntity.getOffY();
        synchronized (entities) {
            Iterator<DisplayEntity> it = entities.values().iterator();
            while (it.hasNext()) {
                it.next().render(cycle);
            }
        }
        renderPops();
    }

    public void renderGUI() {
        glDisable(GL_TEXTURE_2D);
        renderBottomBar();
        renderActionIcons();
        renderOverText();
        renderMouse();
        //chatFont.drawString(50, 200, "Click to move. Use SPACE to lay squares.", Color.blue);
    }

    protected void tickPops() {
        synchronized (pops) {
            LinkedList<Pop> toRemove = new LinkedList<Pop>();
            Iterator<Pop> popit = pops.iterator();
            while (popit.hasNext()) {
                Pop p = popit.next();
                if (!p.tick()) {
                    toRemove.add(p);
                }
            }
            pops.removeAll(toRemove);
        }
    }

    protected void renderPops() {
        synchronized (pops) {

            glDisable(GL_DEPTH_TEST);
            Iterator<Pop> popit = pops.iterator();
            glPushMatrix();
            glTranslatef(-offsetX, -offsetY, -10.0f);

            while (popit.hasNext()) {
                glPushMatrix();
                popit.next().render(camAngle);
                glPopMatrix();
            }
            glPopMatrix();
            glEnable(GL_DEPTH_TEST);
        }
    }

    protected void renderBottomBar() {
        glLoadIdentity();
        glColor3f(0.1f, 0.1f, 0.1f);
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex2f(0, 0);
        glTexCoord2f(1.0f, 0.0f);
        glVertex2f(getWidth(), 0);
        glTexCoord2f(1.0f, 1.0f);
        glVertex2f(getWidth(), getHeight() / 8.0f);
        glTexCoord2f(0.0f, 1.0f);
        glVertex2f(0, getHeight() / 8.0f);
        glEnd();
    }

    protected void renderActionIcons() {
        glLoadIdentity();
        glTranslatef(50.0f, 60.0f, 0.0f);
        glRotatef(180.0f, 1.0f, 0.0f, 0.0f);
        Iterator<Integer> it = choice.iterator();
        String toprint = "Fire(1) Earth(2) Wind(3) Water(4) -- Space Clears Selection";
        while (it.hasNext()) {
            int choice = it.next();
            font.drawString(0, 0, toprint.split(" ")[choice - 1], Color.orange);
            glTranslatef(0.0f, -30.0f, 0.0f);
        }
        font.drawString(0, 0, toprint, Color.orange);
    }

    protected void renderOverText() {
        glLoadIdentity();
        glTranslatef(50.0f, 30.0f, 0.0f);
        glRotatef(180.0f, 1.0f, 0.0f, 0.0f);
        String toprint = "ENTER to talk.";
        if (chatting) {
            toprint = currentchat;
        }
        font.drawString(0, 0, toprint, Color.orange);
    }

    protected void renderMouse() {
        glLoadIdentity();
        glTranslatef(Mouse.getX(), Mouse.getY(), 0);
        glScalef(10.0f, 10.0f, 1.0f);
        glColor3f(((float) 2) / 5, 0.5f, 0.5f);
        glLineWidth(2.0f);
        glBegin(GL_LINES);
        double addpi = 2 * Math.PI * cycle;
        for (float t = 0; t < 2 * Math.PI; t += 0.1) {
            glColor3f(cycle, 1.0f - cycle, (0.5f + cycle) % 1.0f);
            glVertex3d(Math.sin(t + addpi), Math.cos(t + addpi), 0.0);
        }
        glEnd();
    }

    public void drawSquare(float x, float y, float h, float size, float rot, float[] color) {
        glPushMatrix();
        glTranslatef(x - offsetX, y - offsetY, h - 10.0f);
        glRotatef(rot, 0.0f, 0.0f, 1.0f);
        glColor3f(color[0], color[1], color[2]);
        glBegin(GL_QUADS);
        glTexCoord2f(0.0f, 0.0f);
        glVertex2f(-size, -size);
        glTexCoord2f(1.0f, 0.0f);
        glVertex2f(size, -size);
        glTexCoord2f(1.0f, 1.0f);
        glVertex2f(size, size);
        glTexCoord2f(0.0f, 1.0f);
        glVertex2f(-size, size);
        glEnd();
        glPopMatrix();
    }

    protected void make2D() {
        glEnable(GL_BLEND);
        glShadeModel(GL_SMOOTH);
        glDisable(GL_LIGHTING);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluOrtho2D(0.0f, getWidth(), 0.0f, getHeight());
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glDisable(GL_DEPTH_TEST);
    }

    protected void make3D() {
        glShadeModel(GL_SMOOTH);
        glDisable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);
        resizeGL();
        setUpLighting();
    }

    private static void setUpLighting() {
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        FloatBuffer fb = BufferUtils.createFloatBuffer(4);
        fb.put(new float[]{0.15f, 0.15f, 0.15f, 1f});
        fb.flip();
        glLightModel(GL_LIGHT_MODEL_AMBIENT, fb);
        FloatBuffer fb2 = BufferUtils.createFloatBuffer(4);
        fb2.put(new float[]{0.25f, 0.25f, 0.25f, 1f});
        fb2.flip();
        glLight(GL_LIGHT0, GL_POSITION, fb2);
        //glEnable(GL_CULL_FACE);
        //glCullFace(GL_BACK);
        glEnable(GL_COLOR_MATERIAL);
        glColorMaterial(GL_FRONT, GL_DIFFUSE);
    }

    public void updateEntity(Long id, int[] attr) {
        synchronized (entities) {
            if (entities.containsKey(id)) {
                entities.get(id).update(attr[0], new float[]{attr[1] / 100.0f, attr[2] / 100.0f});
            } else {
                entities.put(id, new DisplayEntity(id.longValue(), attr[0], new float[]{attr[1] / 100.0f, attr[2] / 100.0f}));
            }
        }
    }

    public void removeEntity(Long ID) {
        synchronized (entities) {
            entities.remove(ID);
        }
    }
}
