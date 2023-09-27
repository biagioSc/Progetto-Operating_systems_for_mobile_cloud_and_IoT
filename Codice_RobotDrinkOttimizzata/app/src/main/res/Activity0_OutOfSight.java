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
    private Button buttonRiprendi;
    private Animation buttonAnimation;

    private long timeLeftInMillis = 10000; // Tempo totale in millisecondi
    private final long countDownInterval = 1000; // Intervallo di aggiornamento in millisecondi


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_0outofsight);

        textViewTimerCount = findViewById(R.id.textViewTimerCount);
        buttonRiprendi = findViewById(R.id.buttonWaitingRoom);
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);

        // Imposta il timer a 10 secondi e avvia il countdown
        startCountdown();

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

    public void onClickRiprendi(View v) {
        v.startAnimation(buttonAnimation);

        // Annulla il countdown
        countDownTimer.cancel();

        // Torna alla schermata precedente
        finish();
    }
}