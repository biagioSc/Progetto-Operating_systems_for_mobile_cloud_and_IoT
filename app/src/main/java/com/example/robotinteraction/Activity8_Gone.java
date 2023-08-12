package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class Activity8_Gone extends AppCompatActivity {

    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private Button buttonExit;

    private SocketManager socket;

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
            public void onClick(View v)
            {

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
        // Invia un messaggio al server per informarlo che l'utente se ne è andato
        try {
            socket.sendMessage("USER_GONE");

            // Prendere il sessionID dalle varie Intent
            socket.sendMessage(sessionID);

            String response = null;
            response = socket.receiveMessage();

            if(response.equalsIgnoreCase("USER_REMOVED")){
                Log.d("Activity8_Gone","Utente online dal server rimosso correttamente");
            }else
                Log.d("Activity8_Gone","Utente online dal server non rimosso");
        } catch (IOException e) {
            // Gestisci l'eccezione: potresti registrare un errore o informare l'utente
            Log.d("Activity8_Gone","Problema durante l'invio del messaggio di GONE" +
                    "al server");
        }

        // Procedi con la chiusura dell'app
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishAffinity();
        System.exit(0);
    }
}
