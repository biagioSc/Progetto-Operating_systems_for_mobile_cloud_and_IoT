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

import androidx.appcompat.app.AlertDialog;

import java.util.Random;

public class Activity2_Welcome extends Activity {

    private TextView buttonCheckNextState;
    private Animation buttonAnimation;
    private static final long TIME_THRESHOLD = 60000; // 60 secondi
    private final Handler handler = new Handler(Looper.getMainLooper());
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
        runnable = () -> navigateTo(Activity0_OutOfSight.class, sessionID, user);
    }
    private void initUIComponents() {
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        buttonCheckNextState = findViewById(R.id.buttonChecknextState);
        textViewLoggedIn = findViewById(R.id.textViewLoggedIn);
    }
    private void setupListeners() {

        setTouchListenerForAnimation(buttonCheckNextState);
    }
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            sessionID = intent.getStringExtra("param1");
            user = intent.getStringExtra("param2");

            int atIndex = user.indexOf("@");

            if (atIndex != -1) {
                String username = user.substring(0, atIndex);
                runOnUiThread(() -> textViewLoggedIn.setText(username));
            } else {
                runOnUiThread(() -> textViewLoggedIn.setText(user));
            }
        }
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
        new Handler().postDelayed(v::clearAnimation, 200);
    }
    private void navigateTo(Class<?> targetActivity, String param1, String param2) {
        Intent intent = new Intent(Activity2_Welcome.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        startActivity(intent);
    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2, int param3) {
        Intent intent = new Intent(Activity2_Welcome.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        if(param3 != 0) intent.putExtra("param3", param3);

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
        resetInactivityTimer();

        View loadingView = getLayoutInflater().inflate(R.layout.activity_000popuploading, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(loadingView);
        dialogBuilder.setCancelable(false); // Evita la chiusura del messaggio di caricamento toccando al di fuori
        AlertDialog loadingDialog = dialogBuilder.create();
        loadingDialog.show();

        buttonCheckNextState.setClickable(false);

        if("Guest".equals(user)){
            int min = 0;
            int max = 5;
            Random random = new Random();
            numPeopleInQueue = random.nextInt(max - min + 1) + min;

            if (numPeopleInQueue < 2) {
                navigateToParam(Activity4_Ordering.class, sessionID, user, 0);
            } else {
                navigateToParam(Activity3_Waiting.class, sessionID, user, numPeopleInQueue);
            }
        }else {
            try {
                new Thread(() -> {
                    try {
                        socket.send("CHECK_USERS_ORDERING");
                        Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio
                        String num = socket.receive();
                        Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio
                        numPeopleInQueue = Integer.parseInt(num);

                        if (numPeopleInQueue < 2) {
                            navigateToParam(Activity4_Ordering.class, sessionID, user, 0);
                        } else {
                            navigateToParam(Activity3_Waiting.class, sessionID, user, numPeopleInQueue);
                        }
                        runOnUiThread(() -> {
                            loadingDialog.dismiss(); // Chiudi il messaggio di caricamento
                            buttonCheckNextState.setClickable(true);
                        });
                    }catch (Exception e){
                        loadingDialog.dismiss();
                        throw new RuntimeException(e);
                    }
                }).start();

            } catch (Exception e) {
                int min = 0;
                int max = 5;
                Random random = new Random();
                numPeopleInQueue = random.nextInt(max - min + 1) + min;

                if (numPeopleInQueue < 2) {
                    navigateToParam(Activity4_Ordering.class, sessionID, user, 0);
                } else {
                    navigateToParam(Activity3_Waiting.class, sessionID, user, numPeopleInQueue);
                }
            }
        }
    }

}
