package com.spiritgame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

/**
 * Created with IntelliJ IDEA.
 * User: jamesw
 * Date: 11/24/12
 * Time: 5:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShowBitmapTest extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new Panel(this));
    }

    class Panel extends View {
        public Panel(Context context) {
            super(context);
        }

        @Override
        public void onDraw(Canvas canvas) {
            Bitmap _scratch = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.spiritlayout);
            canvas.drawColor(Color.BLACK);
            canvas.drawBitmap(_scratch, 10, 10, null);
        }
    }
}