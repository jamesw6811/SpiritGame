package com.spiritgame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import spiritshared.LoginInfo;
import spiritshared.constants;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Render a pair of tumbling cubes.
 */

class SpiritRenderer implements GLSurfaceView.Renderer, SpiritClient
{
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private HashMap<Long, DisplayEntity> entities;

	private LoginInfo logininfo;
	private boolean texturesloaded = false;
	private boolean modelsloaded = false;
	private int moveX;
	private int moveY;
	private int width;
	private int height;
	private float offsetX;
	private float offsetY;
	private long lastMouseClick;
	private LinkedList<Pop> pops;
	private LinkedList<Integer> choice;
	private ServerCommunicator server;
	private boolean chatting = false;
	private String currentchat = "";
	private float cycle;
	private float camAngleX;
	private float camAngleY = -45;
	private String login;
	private String password;
	private boolean mTranslucentBackground;
	private int chatcolor;
	private Typeface chatfont = Typeface.DEFAULT;
	private float mAngle;
	private boolean newlogin = false;
	private Context context;
	private boolean loggedin = false;
	private GLText guitext;
	private EditText editBox;
	private Activity mainAct;
	private Bitmap recentphoto;
	private File mediaStorageDir = new File(
			Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "spirit_profile");
	private File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "profile.jpg");

	/**
	 * Thread which runs the main game loop
	 */
	Thread gameThread;
	/**
	 * is the game loop running
	 */
	boolean running = false;


	public SpiritRenderer(boolean useTranslucentBackground, LoginInfo li, Activity a)
	{
		mTranslucentBackground = useTranslucentBackground;
		mainAct = a;
		if (li == null)
		{
			logininfo = new LoginInfo("jameswdebug", "blahdeblah".getBytes());
		} else logininfo = li;
	}

	public void setTextBox(EditText ed)
	{
		editBox = ed;

	}

	public void beginGame(Context c)
	{
		context = c;
		chatcolor = Color.argb(255, 255, 0, 0);
		texturesloaded = false;
		modelsloaded = false;
		gameThread = new Thread()
		{
			public void run()
			{
				running = true;
				try
				{
					gameLogicLoop();
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		gameThread.start();
	}

	private void stopGame()
	{
		running = false;
		try
		{
			gameThread.join();
			server.destroy();
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	public void onDrawFrame(GL10 gl)
	{
		gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);         // Clear The Screen And The Depth Buffer
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		if (!texturesloaded)
		{
			DisplayEntity.initTextures(context, gl);
			loadCustomTextures(gl);
			texturesloaded = true;
		}
		if (!modelsloaded)
		{
			DisplayEntity.initModels(context, gl);
			modelsloaded = true;
		}
		if (!loggedin)
		{
			guitext.begin(0f, 0f, 1f, 1f);
			guitext.draw("WELCOME!", 50, 200);
			guitext.end();
			newlogin = true;
		} else render(gl);

		gl.glDisable(GL10.GL_BLEND);
		gl.glDisable(GL10.GL_TEXTURE_2D);
	}


	public void render(GL10 gl)
	{
		gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);         // Clear The Screen And The Depth Buffer

		cycle = ((float) (System.currentTimeMillis() % 1000)) / 1000.0f;

		GameTimer gt = new GameTimer();
		make3D(gl, width, height);
		//System.out.println("make3d:" + gt.getTimeMillis());
		gt = new GameTimer();
		renderContent(gl);
		//System.out.println("renderContent:" + gt.getTimeMillis());
		gt = new GameTimer();
		make2D(gl, width, height);
		//System.out.println("make2d:" + gt.getTimeMillis());
		gt = new GameTimer();
		renderGUI(gl);
		//System.out.println("rendergui:" + gt.getTimeMillis());
	}

	public void renderContent(GL10 gl)
	{
		offsetX = DisplayEntity.getOffX();
		offsetY = DisplayEntity.getOffY();
		synchronized (entities)
		{
			Iterator<DisplayEntity> it = entities.values().iterator();
			while (it.hasNext())
			{
				it.next().render(gl, cycle);
			}
		}
		renderPops(gl);
	}

	public void renderGUI(GL10 gl)
	{
		gl.glDisable(gl.GL_TEXTURE_2D);
//     renderBottomBar();
//     renderActionIcons();
//     renderOverText();
//     renderMouse();
		//chatFont.drawString(50, 200, "Click to move. Use SPACE to lay squares.", Color.blue);
	}


	protected void make2D(GL10 gl, float width, float height)
	{
		gl.glEnable(gl.GL_BLEND);
		gl.glShadeModel(gl.GL_SMOOTH);
		gl.glDisable(gl.GL_LIGHTING);
		gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
		gl.glMatrixMode(gl.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrthof(0, width, 0, height, 0.01f, 100.0f);
		gl.glMatrixMode(gl.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glDisable(gl.GL_DEPTH_TEST);
	}

	protected void make3D(GL10 gl, int width, int height)
	{
		gl.glShadeModel(gl.GL_SMOOTH);
		gl.glDisable(gl.GL_TEXTURE_2D);
		gl.glEnable(gl.GL_DEPTH_TEST);
		resizeGL(gl, width, height);
		//setUpLighting(gl);
	}


	protected void resizeGL(GL10 gl, int width, int height)
	{
		this.width = width;
		this.height = height;

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(gl.GL_PROJECTION);                        // Select The Projection Matrix
		gl.glLoadIdentity();                           // Reset The Projection Matrix
		// Calculate The Aspect Ratio Of The Window
		float xmin, xmax, ymin, ymax;
		float zNear = 0.1f;
		float zFar = 100f;
		ymax = 0.1f * (float) Math.tan(45.0f * Math.PI / 360.0);
		ymin = -ymax;
		xmin = ymin * (float) width / (float) height;
		xmax = ymax * (float) width / (float) height;


		gl.glFrustumf(xmin, xmax, ymin, ymax, zNear, zFar);
		gl.glMatrixMode(gl.GL_MODELVIEW);                     // Select The Modelview Matrix
		gl.glLoadIdentity();// Reset The Modelview Matrix

		gl.glTranslatef(0.0f, 0.0f, -10.0f);
		gl.glRotatef(camAngleY, 1.0f, 0.0f, 0.0f);
		gl.glRotatef(camAngleX, 0.0f, 0.0f, 1.0f);
		gl.glTranslatef(0.0f, 0.0f, 10.0f);

	}

	public void endGame()
	{
		stopGame();
	}

	private static void setUpLighting(GL10 gl)
	{
		gl.glEnable(gl.GL_DEPTH_TEST);
		gl.glEnable(gl.GL_LIGHTING);
		gl.glEnable(gl.GL_LIGHT0);
		FloatBuffer fb;
		ByteBuffer vbb = ByteBuffer.allocateDirect(4 * 4);
		vbb.order(ByteOrder.nativeOrder());
		fb = vbb.asFloatBuffer();
		fb.put(new float[]{0.3f, 0.3f, 0.3f, 1f});
		fb.flip();
		gl.glLightModelfv(gl.GL_LIGHT_MODEL_AMBIENT, fb);
		FloatBuffer fb2;
		vbb = ByteBuffer.allocateDirect(4 * 4);
		vbb.order(ByteOrder.nativeOrder());
		fb2 = vbb.asFloatBuffer();
		fb2.put(new float[]{10f, 0f, 0f, 1f});
		fb2.flip();
		gl.glLightfv(gl.GL_LIGHT0, gl.GL_POSITION, fb2);
		gl.glEnable(gl.GL_COLOR_MATERIAL);
	}

	public void changeAngleX(float c)
	{
		camAngleX += c;
	}

	public void changeAngleY(float c)
	{
		camAngleY += c;
		if (camAngleY > 0) camAngleY = 0;
		if (camAngleY < -80) camAngleY = -80;
	}

	public void clicked(float mx, float my)
	{
		mx = mx - width / 2;
		my = -my + height / 2;
		float mycos = (float) Math.cos(camAngleX * Math.PI / 180.0f);
		float mysin = (float) Math.sin(camAngleX * Math.PI / 180.0f);
		moveX = (int) (mx * mycos + my * mysin);
		moveY = (int) (-mx * mysin + my * mycos);
		server.move(moveX, moveY);
		//setMouseDelta();
		System.out.println("MOVED to " + moveX + " , " + moveY);
		/*
				if (Mouse.getEventButton() == 1) {
					server.mouseSelect(moveX, moveY);
					setMouseDelta();
					System.out.println("Selected to " + moveX + " , " + moveY);
				}*/
	}

	public void openBuild()
	{
		server.doAction(constants.MAKE_VILLAGE);
	}

	public void openChat()
	{
		if (editBox.getVisibility() == View.VISIBLE)
		{
			editBox.setVisibility(View.GONE);
			InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			mgr.hideSoftInputFromWindow(editBox.getWindowToken(), 0);
		} else
		{
			editBox.setText("");
			editBox.setVisibility(View.VISIBLE);
			editBox.requestFocus();

			InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			mgr.showSoftInput(editBox, 0);
		}
	}

	public void openOrder()
	{
		captureProfileImage();
	}

	public void openCast()
	{

	}

	private void captureProfileImage()
	{
		// create Intent to take a picture and return control to the calling application
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists())
		{
			if (!mediaStorageDir.mkdirs())
			{
				Log.d("Spirit", "failed to create directory");
				return;
			}
		}

		// Create a media file name
		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mediaFile)); // set the image file name

		// start the image capture Intent
		mainAct.startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
		{

			Bitmap bitmap = null;
			try
			{
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inSampleSize = 16;
				bitmap = BitmapFactory.decodeFile(mediaFile.getAbsolutePath(), opts);
			} catch (OutOfMemoryError e)
			{
				e.printStackTrace();

				System.gc();

				try
				{
					BitmapFactory.Options opts = new BitmapFactory.Options();
					opts.inSampleSize = 16;
					bitmap = BitmapFactory.decodeFile(mediaFile.getAbsolutePath(), opts);
				} catch (OutOfMemoryError e2)
				{
					e2.printStackTrace();
					throw new RuntimeException(e2.toString());
				}
			}

			Bitmap photo = Bitmap.createScaledBitmap(bitmap, 256, 256, false);
			bitmap.recycle();
			if(server!=null&&server.loggedin())
				server.setProfile(photo);
			else
				recentphoto = photo;
			setTexture(constants.VIEW_SELF, photo);
			loggedin = false;
		}
	}

	protected void setTexture(long ID, Bitmap bitmap)
	{
		if (ID == constants.VIEW_SELF) DisplayEntity.setProfile(bitmap);
		else synchronized (entities){
			entities.get(ID).setCustomTexture(bitmap);
		}
		reloadAssets();
	}
	public void reloadAssets(){
		texturesloaded = false;
		modelsloaded = false;
	}
	public void disconnect(){
		if(server!=null){
			server.destroy();
			server = null;
		}
	}

	public void sendChat()
	{
		server.chat(editBox.getText().toString());
		editBox.setVisibility(View.GONE);
		InputMethodManager mgr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		mgr.hideSoftInputFromWindow(editBox.getWindowToken(), 0);
	}

	private void gameLogicLoop()
	{

		entities = new HashMap<Long, DisplayEntity>();
		choice = new LinkedList<Integer>();
		pops = new LinkedList<Pop>();
		loggedin = false;
		while (running)
		{
			while (!loggedin)
			{
				//System.out.println("LOOP");
				try
				{
					if (newlogin)
					{
						loggedin = initNetwork(logininfo);
					}
					Thread.sleep(1000);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			GameTimer gt = new GameTimer();
			moveEnts();
			//System.out.println("moveEnts:" + gt.getTimeMillis());
			gt = new GameTimer();
			tickPops();
			//System.out.println("tickPops:" + gt.getTimeMillis());
			try
			{
				Thread.sleep(10);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public void moveEnts()
	{
		synchronized (entities){
		Iterator<DisplayEntity> it = entities.values().iterator();
		while (it.hasNext())
		{
			DisplayEntity nextent = it.next();
			nextent.tick();
		}
		}

	}

	protected void tickPops()
	{
		synchronized (pops)
		{
			LinkedList<Pop> toRemove = new LinkedList<Pop>();
			Iterator<Pop> popit = pops.iterator();
			while (popit.hasNext())
			{
				Pop p = popit.next();
				if (!p.tick())
				{
					toRemove.add(p);
				}
			}
			pops.removeAll(toRemove);
		}
	}

	protected void loadCustomTextures(GL10 gl)
	{
		synchronized(entities){
		Iterator<DisplayEntity> it = entities.values().iterator();
		while (it.hasNext())
		{
			DisplayEntity nextent = it.next();
			nextent.loadCustomTexture(gl);
		}
		}
	}

	protected void renderPops(GL10 gl)
	{
		synchronized (pops)
		{

			gl.glDisable(gl.GL_DEPTH_TEST);
			Iterator<Pop> popit = pops.iterator();
			gl.glPushMatrix();
			gl.glTranslatef(-offsetX, -offsetY, -10.0f);

			while (popit.hasNext())
			{
				gl.glPushMatrix();
				popit.next().render(camAngleX, camAngleY, gl);
				gl.glPopMatrix();
			}
			gl.glPopMatrix();
			gl.glEnable(gl.GL_DEPTH_TEST);
		}
	}


	public boolean initNetwork(LoginInfo li) throws Exception
	{
		System.out.println("initing network");
		if (server != null)
		{
			server.destroy();
		}
		server = new ServerCommunicator(this, li);
		new Thread(server).start();
		Thread.sleep(3000);
		System.out.println("init done" + server.loggedin());
		if(server.loggedin()&&recentphoto!=null){
			server.setProfile(recentphoto);
			recentphoto = null;
		}
		return server.loggedin();
	}

	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		gl.glViewport(0, 0, width, height);

		// Setup orthographic projection
		gl.glMatrixMode(GL10.GL_PROJECTION);          // Activate Projection Matrix
		gl.glLoadIdentity();                            // Load Identity Matrix
		gl.glOrthof(                                    // Set Ortho Projection (Left,Right,Bottom,Top,Front,Back)
														0, width,
														0, height,
														1.0f, -1.0f
		);

		// Save width and height
		this.width = width;                             // Save Current Width
		this.height = height;                           // Save Current Height
		gl.glEnable(gl.GL_DEPTH_TEST);                        // Enables Depth Testing
		gl.glDepthFunc(gl.GL_LEQUAL);                         // The Type Of Depth Test To Do
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		guitext = new GLText(gl);
		guitext.load(Typeface.DEFAULT_BOLD, 60, 2, 2);
	}

	public synchronized void updateEntity(Long id, int[] attr)
	{
		synchronized (entities)
		{
			if (entities.containsKey(id))
			{
				entities.get(id).update(attr[0], new float[]{attr[1] / 100.0f, attr[2] / 100.0f});
			} else
			{
				entities.put(id, new DisplayEntity(id.longValue(), attr[0],
												   new float[]{attr[1] / 100.0f, attr[2] / 100.0f}));
			}
		}
	}

	public void removeEntity(Long ID)
	{
		synchronized (entities)
		{
			entities.remove(ID);
		}
	}

	public void doChat(int chatID, int[] chatpos, String chat)
	{
		float[] chatposf = {((float) chatpos[0]) / 100.0f, ((float) chatpos[1]) / 100.0f};
		ChatPop cp = new ChatPop(chat, 5000, chatposf, chatfont, chatcolor);
		synchronized (pops)
		{
			pops.add(cp);
		}
	}

}
