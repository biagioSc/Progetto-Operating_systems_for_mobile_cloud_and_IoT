package com.example.robotinteraction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

public class Activity2_Welcome extends Activity {

    private Button buttonCheckNextState;
    private Animation buttonAnimation;
    private static final long TIME_THRESHOLD = 60000; // 60 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private String sessionID = "-1", user = "Guest";
    private Activity_SocketManager socket;
    private TextView textViewLoggedIn;
    private int numPeopleInQueue = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2welcome);

        connection();
        initUIComponents();
        setupListeners();

        receiveParam();
    }

    private void connection() {
        socket = Activity_SocketManager.getInstance(); // Ottieni l'istanza del gestore del socket
        boolean connesso = socket.isConnected();

        /*if(connesso==false){
            showPopupMessage();
        }*/

        runnable = new Runnable() { // Azione da eseguire dopo l'inattività
            @Override
            public void run() {

                navigateTo(Activity0_OutOfSight.class);
            }
        };
    }
    private void initUIComponents() {
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        buttonCheckNextState = findViewById(R.id.buttonChecknextState);
        textViewLoggedIn = findViewById(R.id.textViewLoggedIn);
    }
    private void setupListeners() {
        setTouchListenerForAnimation(buttonCheckNextState);
    }
    private void setTouchListenerForAnimation(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    resetInactivityTimer();
                    applyButtonAnimation(v);
                }
                return false;
            }
        });
    }
    private void applyButtonAnimation(View v) {
        v.startAnimation(buttonAnimation);
        new Handler().postDelayed(() -> v.clearAnimation(), 200);
    }
    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(Activity2_Welcome.this, targetActivity);
        startActivity(intent);
    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2) {
        Intent intent = new Intent(Activity2_Welcome.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        startActivity(intent);
        finish();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        resetInactivityTimer();
        return super.onTouchEvent(event);
    }
    @Override
    protected void onResume() {
        super.onResume();
        resetInactivityTimer();
    }
    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
    private void startInactivityTimer() {

        handler.postDelayed(runnable, TIME_THRESHOLD);
    }
    private void resetInactivityTimer() {
        handler.removeCallbacks(runnable);
        startInactivityTimer();
    }
    public void onClickQueue(View v) {
        resetInactivityTimer(); // Aggiungi questa linea per reimpostare il timer
        int min = 0;
        int max = 5;
        Random random = new Random();
        //numPeopleInQueue = random.nextInt(max - min + 1) + min;
        numPeopleInQueue = 2;
        Intent intent;
        if (numPeopleInQueue < 2) {
            // Vai alla schermata di Ordering
            intent = new Intent(Activity2_Welcome.this, Activity4_Ordering.class);
        } else {
            // Vai alla schermata di Waiting passando il numero di utenti in coda come parametro
            intent = new Intent(Activity2_Welcome.this, Activity3_Waiting.class);
            intent.putExtra("param3", numPeopleInQueue);
        }

        // In entrambi i casi (if o else) passo il sessionID
        intent.putExtra("param1",sessionID);
        intent.putExtra("param2",user);
        startActivity(intent);
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewLoggedIn.setText(username);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewLoggedIn.setText(user);
                    }
                });
            }
        }
    }

}
