package com.spiritgame;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import spiritshared.LoginInfo;

/**
 * Wrapper activity demonstrating the use of {@link GLSurfaceView}, a view
 * that uses OpenGL drawing into a dedicated surface.
 */
public class SpiritMain extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create our Preview view and set it as the content of our
        // Activity

        LoginInfo li = null;
        if(getIntent().hasExtra("name"))li=new LoginInfo(getIntent().getStringExtra("name"), getIntent().getStringExtra("pass").getBytes());
        final SpiritRenderer sr = new SpiritRenderer(false, li, this);
        mGLSurfaceView = new SpiritGLSurfaceView(this, sr);
        //sr.beginGame(this.getApplicationContext());
        mGLSurfaceView.setRenderer(sr);
        setContentView(mGLSurfaceView);
        LayoutInflater inflater = getLayoutInflater();
        addContentView(inflater.inflate(R.layout.main, null), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));

        EditText editBox = (EditText)findViewById(R.id.chat_text);
        sr.setTextBox(editBox);
        editBox.setVisibility(View.GONE);
        editBox.setSingleLine();
        editBox.setImeOptions(EditorInfo.IME_ACTION_SEND);

        editBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sr.sendChat();
                    return true;
                }
                return false;
            }
        });

        ((Button)findViewById(R.id.btn_build)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sr.openBuild();
            }
        });
        ((Button)findViewById(R.id.btn_chat)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sr.openChat();
            }
        });
        ((Button)findViewById(R.id.btn_order)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sr.openOrder();
            }
        });
        ((Button)findViewById(R.id.btn_cast)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                sr.openCast();
            }
        });



    }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    mGLSurfaceView.onConfigurationChange(newConfig);
	}

    @Override
    protected void onResume() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onResume();
        mGLSurfaceView.onResume();
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode,
							   							   data);    //To change body of overridden methods use File | Settings | File Templates.
		mGLSurfaceView.onActivityResult(requestCode, resultCode, data);
	}

	@Override
    protected void onPause() {
        // Ideally a game should implement onResume() and onPause()
        // to take appropriate action when the activity looses focus
        super.onPause();
        mGLSurfaceView.onPause();
    }

    private SpiritGLSurfaceView mGLSurfaceView;
}
