package com.example.robotinteraction;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class Activity3_Waiting extends AppCompatActivity {
    private int queueCount = 0;
    private int queueTime = 0;
    private TextView textViewQueueCount;
    private TextView textViewWaitTime;
    private ProgressBar progressBarWaiting;
    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    private String sessionID = null;

    private SocketManager socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3waiting);


        // Get parameters from previous activity
        Intent intent = getIntent();
        queueCount = intent.getIntExtra("PARAM_NAME", 0);
        sessionID = intent.getStringExtra("SESSION_ID");

        textViewQueueCount = findViewById(R.id.textViewQueueCount);
        textViewWaitTime = findViewById(R.id.textViewWaitTime);
        progressBarWaiting = findViewById(R.id.progressBarWaiting);

        textViewQueueCount.setText("Persone in coda: " + queueCount);

        queueTime = queueCount * 30; // Calculate queue time in seconds
        textViewWaitTime.setText("Tempo di attesa: " + queueTime + " secondi");

        progressBarWaiting.setMax(queueTime);

        // Initialize and start the countdown timer
        new CountDownTimer(queueTime * 1000, 1000) {
            private int secondCounter = 0; // Variabile per il conteggio dei secondi

            @Override
            public void onTick(long millisUntilFinished) {
                progressBarWaiting.setProgress((int) (queueTime - millisUntilFinished / 1000));
                textViewWaitTime.setText("Tempo di attesa: " + (int) (millisUntilFinished / 1000) + " secondi");

                // Incrementa il conteggio dei secondi
                secondCounter++;

                // Controlla se sono passati 30 secondi
                if (secondCounter >= 30) {
                    // Azzerare la variabile dei secondi
                    secondCounter = 0;

                    // Diminuire il numero di persone in coda di 1
                    queueCount--;
                    textViewQueueCount.setText("Persone in coda: " + queueCount);
                }
            }

            @Override
            public void onFinish() {
                // Handle countdown finish
                // For example, navigate to the "Ordering" activity
                Intent intent = new Intent(Activity3_Waiting.this, Activity4_Ordering.class);
                intent.putExtra("SESSION_ID",sessionID);

                // Informo il server dell'update da fare
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            socket.sendMessage("ADD_USER_ORDERING");
                            socket.sendMessage(sessionID);

                        }catch (IOException e){
                            Log.d("Acitivty3_Waiting","Errore nella add_user_ordering");
                        }
                    }
                }).start();

                startActivity(intent);
            }
        }.start();

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Activity3_Waiting.this, Activity0_OutOfSight.class);
                startActivity(intent);
            }
        };

        startInactivityTimer();
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


}
