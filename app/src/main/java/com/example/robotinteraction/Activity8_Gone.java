package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Activity8_Gone extends AppCompatActivity {

    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private Button buttonExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_8gone);

        buttonExit = findViewById(R.id.buttonExit);

        // Inizializza il Runnable per il controllo dell'inattività
        runnable = new Runnable() {
            @Override
            public void run() {
                // Avvia l'activity OutOfSight
                Intent intent = new Intent(Activity8_Gone.this, Activity0_OutOfSight.class);
                startActivity(intent);
            }
        };

        // Avvia il timer iniziale
        startInactivityTimer();

        // Gestisci il click sul bottone "Esci"
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitApp();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // L'utente ha toccato lo schermo, resetta il timer di inattività
        resetInactivityTimer();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetInactivityTimer(); // Resetta il timer all'avvio dell'activity
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); // Rimuovi il callback quando l'activity è in pausa
    }

    private void startInactivityTimer() {
        handler.postDelayed(runnable, TIME_THRESHOLD);
    }

    private void resetInactivityTimer() {
        handler.removeCallbacks(runnable);
        startInactivityTimer();
    }

    private void exitApp() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishAffinity();
        System.exit(0);
    }
}
