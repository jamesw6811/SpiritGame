package com.spiritgame;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

/**
 * Created with IntelliJ IDEA.
 * User: jamesw
 * Date: 11/24/12
 * Time: 6:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpiritGLSurfaceView extends GLSurfaceView{
    float mPreviousX;
    float mPreviousY;
    float mDownX;
    float mDownY;
    float TOUCH_SCALE_FACTOR =  100.0f/320;
    float CLICK_TIME_MAX = 1000;
    float CLICK_DISTANCE_MAX = 30;
    long timedown = 0;
    SpiritRenderer mRenderer;
    public SpiritGLSurfaceView(Context context, SpiritRenderer sr) {
        super(context);
        mRenderer = sr;
    }

    public SpiritGLSurfaceView(Context context, AttributeSet attrs, SpiritRenderer sr) {
        super(context, attrs);
        mRenderer = sr;
    }

	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		mRenderer.onActivityResult(requestCode, resultCode, data);
	}
	public void onConfigurationChange(Configuration config){

	}
	public void onResume(){
		super.onResume();
		mRenderer.beginGame(this.getContext());
	}
	public void onPause(){
		super.onPause();
		mRenderer.endGame();
	}

    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;


                mRenderer.changeAngleX((dx * TOUCH_SCALE_FACTOR));
                mRenderer.changeAngleY((dy * TOUCH_SCALE_FACTOR));
                requestRender();
                break;
            case MotionEvent.ACTION_DOWN:
                timedown = System.currentTimeMillis();
                mDownX = x;
                mDownY = y;
                break;
            case MotionEvent.ACTION_UP:
                if(System.currentTimeMillis()-timedown<CLICK_TIME_MAX){
                    if(!(Math.abs(mDownX-x)>CLICK_DISTANCE_MAX||Math.abs(mDownY-y)>CLICK_DISTANCE_MAX))mRenderer.clicked(x, y);
                }
                break;

        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);    //To change body of overridden methods use File | Settings | File Templates.
       // mRenderer.endGame();
    }
}
