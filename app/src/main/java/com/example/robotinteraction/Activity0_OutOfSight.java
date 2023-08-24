package com.example.robotinteraction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
public class Activity0_OutOfSight extends Activity {

    private TextView textViewTimerCount;
    private CountDownTimer countDownTimer;
    private TextView buttonRiprendi;
    private Animation buttonAnimation;
    private long timeLeftInMillis = 10000; // Tempo totale in millisecondi
    private final long countDownInterval = 1000; // Intervallo di aggiornamento in millisecondi
    private String sessionID = "-1", user = "Guest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_0outofsight);

        initUIComponents();
        receiveParam();

        startCountdown();
    }
    private void initUIComponents() {
        textViewTimerCount = findViewById(R.id.textViewTimerCount);
        buttonRiprendi = findViewById(R.id.buttonWaitingRoom);
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
    }
    private void startCountdown() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, countDownInterval) {
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountdownText();
            }
            public void onFinish() {
                // Avvia l'activity "Gone"
                Intent intent = new Intent(Activity0_OutOfSight.this, Activity8_Gone.class);
                intent.putExtra("param1", sessionID);
                intent.putExtra("param2", user);
                startActivity(intent);
                finish();
            }
        };

        // Aggiornamento del conteggio ogni secondo
        countDownTimer.start();
    }
    private void updateCountdownText() {
        int seconds = (int) (timeLeftInMillis / 1000);
        String timeFormatted = String.format("%ds", seconds);
        textViewTimerCount.setText(timeFormatted);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Assicurati di annullare il countdown quando l'activity viene distrutta
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            sessionID = intent.getStringExtra("param1");
            user = intent.getStringExtra("param2");
        }
    }
    public void onClickRiprendi(View v) {
        v.startAnimation(buttonAnimation);
        countDownTimer.cancel();
        finish();
    }
}