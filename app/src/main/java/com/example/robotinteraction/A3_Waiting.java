package com.example.robotinteraction;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;


public class A3_Waiting extends Activity {

    private ProgressBar progressBarWaiting;
    private TextView textViewQueueCount;
    private TextView textViewWaitTime;
    private TextView textViewSongName;
    private ProgressBar progressBarSong;
    private int queueCount = 10;
    private int waitTime = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3waiting);

        // Trova le view nel layout tramite i loro ID
        progressBarWaiting = findViewById(R.id.progressBarWaiting);
        textViewQueueCount = findViewById(R.id.textViewQueueCount);
        textViewWaitTime = findViewById(R.id.textViewWaitTime);
        textViewSongName = findViewById(R.id.textViewSongName);
        progressBarSong = findViewById(R.id.progressBarSong);

        // Simula il decremento della coda e del tempo di attesa
        simulateQueueAndSongUpdates();
    }

    // Metodo per simulare il decremento della coda e del tempo di attesa
    private void simulateQueueAndSongUpdates() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                queueCount--;
                waitTime -= 5;
                progressBarWaiting.setProgress(queueCount);
                textViewQueueCount.setText("Persone in coda: " + queueCount);
                textViewWaitTime.setText("Tempo di attesa: " + waitTime + " minuti");

                // Se la coda è vuota, attendi 5 secondi e poi avvia l'attività "Ordering"
                if (queueCount <= 0) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Avvia l'attività "Ordering"
                            //Intent intent = new Intent(A3_Waiting.this, A4_Ordering.class);
                            //startActivity(intent);
                            finish(); // Chiudi l'attuale attività "Waiting"
                        }
                    }, 5000); // Attendi 5 secondi (5000 millisecondi) prima di avviare l'attività "Ordering"
                } else {
                    // Ripeti il decremento della coda finché non raggiunge il valore minimo desiderato
                    handler.postDelayed(this, 1000); // Ripeti l'aggiornamento ogni secondo (1000 millisecondi)
                }
            }
        }, 1000); // Attendi 1 secondo (1000 millisecondi) prima di avviare il primo decremento
    }

}