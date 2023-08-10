package com.example.robotinteraction;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Activity3_Waiting extends AppCompatActivity {
    private int queueCount = 0;
    private int queueTime = 0;
    private TextView textViewQueueCount;
    private TextView textViewWaitTime;
    private ProgressBar progressBarWaiting;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3waiting);

        // Get parameters from previous activity (replace 'PARAM_NAME' with the actual parameter name)
        Intent intent = getIntent();
        queueCount = intent.getIntExtra("PARAM_NAME", 0);

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
                startActivity(intent);
                finish();
            }
        }.start();

        // Initialize and start the background music
        //mediaPlayer = MediaPlayer.create(this, R.raw.background_music);
        //mediaPlayer.setLooping(true);
        //mediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause the music when the activity is paused
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume the music when the activity is resumed
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release resources and stop the music when the activity is destroyed
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // Implement other methods as needed

    public void onExitClick(View view) {
        finish();
    }
}
