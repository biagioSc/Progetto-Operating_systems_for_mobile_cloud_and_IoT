package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class Activity8_Gone extends AppCompatActivity {

    private TextView buttonExit;
    private Animation buttonAnimation;
    private Activity_SocketManager socket;  // Manager del socket per la comunicazione con il server
    private String sessionID = "-1", user = "Guest";
    private TextView textViewGoodbye;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_8gone);

        connection();
        initUIComponents();
        setupListeners();

        receiveParam();
    }
    private void connection() {
        socket = Activity_SocketManager.getInstance(); // Ottieni l'istanza del gestore del socket
    }
    private void initUIComponents() {
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        buttonExit = findViewById(R.id.buttonExit);
        textViewGoodbye = findViewById(R.id.textViewGoodbye);
    }
    private void setupListeners() {
        setTouchListenerForAnimation(buttonExit);
    }
    private void setTouchListenerForAnimation(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    applyButtonAnimation(v);
                }
                return false;
            }
        });
    }
    private void applyButtonAnimation(View v) {
        v.startAnimation(buttonAnimation);
        new Handler().postDelayed(v::clearAnimation, 200);
    }
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            sessionID = intent.getStringExtra("param1");
            user = intent.getStringExtra("param2");
            int atIndex = user.indexOf("@");

            // Verificare se è presente il simbolo "@"
            if (atIndex != -1) {
                String username = user.substring(0, atIndex);
                runOnUiThread(() -> textViewGoodbye.setText(textViewGoodbye + " " + username));

            } else {
                runOnUiThread(() -> textViewGoodbye.setText(textViewGoodbye + " " + user));
            }
        }
    }
    public void onClickExit(View v) {
        if(!("Guest".equals(user))) {
            socket.send("USER_GONE");
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishAffinity();
        System.exit(0);
    }
}
