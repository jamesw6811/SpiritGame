package com.spiritgame;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created with IntelliJ IDEA.
 * User: jamesw
 * Date: 11/25/12
 * Time: 6:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpiritLogin extends Activity {

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.login);

            /* Set OnClickListner to the login button */
            Button login = (Button) findViewById(R.id.btn_login);

            login.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {

                    EditText loginName = (EditText) findViewById(R.id.txt_userName);
                    EditText loginPass = (EditText) findViewById(R.id.password);
                    String  name  = loginName.getText().toString();
                    String  pass  = loginPass.getText().toString();

                    Intent intent  = new Intent(getApplicationContext(),SpiritMain.class);

                    intent.putExtra("name", name);
                    intent.putExtra("pass", pass);

                    startActivity(intent);

                }
            });
        }
    }