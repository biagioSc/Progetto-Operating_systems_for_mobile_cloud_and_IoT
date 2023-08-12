package com.example.robotinteraction;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Activity6_Attesa extends AppCompatActivity {

    private ProgressBar progressBarWaiting;
    private TextView textViewPleaseWait;
    private TextView textViewWaitTime;
    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    private String sessionID = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_6attesa);

        // Prendo il sessionID
        Intent intent = getIntent();
        if(intent != null)
            sessionID = intent.getStringExtra("SESSION_ID");

        progressBarWaiting = findViewById(R.id.progressBarWaiting);
        textViewPleaseWait = findViewById(R.id.textViewPleaseWait);
        textViewWaitTime = findViewById(R.id.textViewWaitTime);

        // Ottieni il parametro "selectedDrink" dalla chiamata all'activity
        final String selectedDrink = getIntent().getStringExtra("selectedDrink");

        // Imposta il testo nella TextView
        TextView textViewTimeElapsed = findViewById(R.id.textViewTimeElapsed);
        textViewTimeElapsed.setText("Il tuo " + selectedDrink + " è quasi pronto");

        // Imposta il timer per 30 secondi
        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Aggiorna il testo del tempo di attesa
                long seconds = millisUntilFinished / 1000;
                textViewWaitTime.setText("Tempo di attesa: " + seconds + " secondi");
            }

            public void onFinish() {
                // Quando il timer termina, nascondi la progress bar e mostra il messaggio di completamento
                progressBarWaiting.setVisibility(ProgressBar.INVISIBLE);
                textViewPleaseWait.setText("Completato!");

                // Apri automaticamente l'Activity "Activity7_Farewelling" e passa il parametro "selectedDrink"
                openFarewellingActivity(selectedDrink);
            }
        }.start();
        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Activity6_Attesa.this, Activity0_OutOfSight.class);
                startActivity(intent);
                finish();
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

    private void openFarewellingActivity(String selectedDrink) {
        Intent intent = new Intent(this, Activity7_Farewelling.class);
        intent.putExtra("selectedDrink", selectedDrink);
        intent.putExtra("SESSION_ID",sessionID);
        startActivity(intent);

    }
}
