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

import java.io.IOException;

public class Activity2_Welcome extends Activity {

    private Button buttonCheckNextState, buttonLogOut;
    private Animation buttonAnimation;
    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    private String sessionID = null;

    private SocketManager socket;

    private int numPeopleInQueue = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2welcome);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Log.d("Activity1_New", "[CONNECTION] Tentativo di connessione...");

                        // Crea una nuova istanza di SocketManager e tenta la connessione.
                        socket = SocketManager.getInstance();
                        socket.attemptConnection();

                        if (socket.isConnected()) {
                            Log.d("Activity1_New", "[CONNECTION] Connessione stabilita");
                            break;
                        } else {
                            throw new IOException();
                        }

                    } catch (Exception e) {
                        Log.d("Activity1_New", "[] Connessione fallita");

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                }
            }
        }).start();

        buttonCheckNextState = findViewById(R.id.buttonChecknextState);
        buttonLogOut = findViewById(R.id.buttonLogOut);

        // Prendo il sessionID dell'utente
        Intent intent = getIntent();
        if(intent != null)
            sessionID = intent.getStringExtra("SESSION_ID");

        // Carica l'animazione dal file XML
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Activity2_Welcome.this, Activity0_OutOfSight.class);
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

    public void onClickQueue(View v) {
        // Avvia l'animazione
        buttonCheckNextState.startAnimation(buttonAnimation);

        // Avvio collegamento con il server per ricevere numero di utenti in fase di ordering
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Avviso il server della richiesta di utenti nella fase di ordering
                    socket.sendMessage("CHECK_USERS_ORDERING");

                    // Ricevo numero di utenti nello stato di ordering
                    String peopleInOrderingStateStr = socket.receiveMessage();
                    numPeopleInQueue = Integer.parseInt(peopleInOrderingStateStr);


                } catch (IOException e) {
                    Log.d("Activity2_Welcome", "Errore durante i messaggi nella check_" +
                            "users_ordering.");
                }
            }
        }).start();


        Intent intent;
        if (numPeopleInQueue < 2) {
            // Vai alla schermata di Ordering
            intent = new Intent(Activity2_Welcome.this, Activity4_Ordering.class);

            // Informo il server dell'update da fare

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        socket.sendMessage("ADD_USER_ORDERING");
                        socket.sendMessage(sessionID);

                    }catch (IOException e){
                        Log.d("Acitivty2_Welcome","Errore nella add_user_ordering");
                    }
                }
            }).start();

            startActivity(intent);
        } else {
            // Vai alla schermata di Waiting passando il numero di utenti in coda come parametro
            intent = new Intent(Activity2_Welcome.this, Activity3_Waiting.class);
            intent.putExtra("numPeopleInQueue", numPeopleInQueue);

            // Informo il server dell'update da fare
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        socket.sendMessage("ADD_USER_WAITING");
                        socket.sendMessage(sessionID);

                    }catch (IOException e){
                        Log.d("Acitivty2_Welcome","Errore nella add_user_waiting");
                    }
                }
            }).start();


            startActivity(intent);
        }

        // In entrambi i casi (if o else) passo il sessionID
        intent.putExtra("SESSION_ID",sessionID);

    }
    public void onClickExit(View v) {
        // Chiudi l'app
        buttonLogOut.startAnimation(buttonAnimation);

        // Informo il server dell'uscita del client
        try {
            socket.sendMessage("USER_GONE");

            // Prendere il sessionID dalle varie Intent
            socket.sendMessage(sessionID);

            String response = null;
            response = socket.receiveMessage();

            if(response.equalsIgnoreCase("USER_REMOVED")){
                Log.d("Activity2_Welcome","Utente online dal server rimosso correttamente");
            }else
                Log.d("Activity2_Welcome","Utente online dal server non rimosso");
        } catch (IOException e) {
            // Gestisci l'eccezione: potresti registrare un errore o informare l'utente
            Log.d("Activity2_Welcome","Problema durante l'invio del messaggio di GONE" +
                    "al server");
        }
    }

}

