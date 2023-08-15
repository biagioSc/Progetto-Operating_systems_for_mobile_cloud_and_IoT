package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class Activity8_Gone extends AppCompatActivity {

    private Button buttonExit;

    private Animation buttonAnimation;

    private SocketManager socket;

    private String sessionID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_8gone);

        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);

        // Prendo il sessionID
        Intent intent = getIntent();
        if(intent != null)
            sessionID = intent.getStringExtra("SESSION_ID");

        buttonExit = findViewById(R.id.buttonExit);

        // Gestisci il click sul bottone "Esci"
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                exitApp();
            }
        });
        buttonExit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Applica l'animazione di scala quando il bottone viene premuto
                        v.startAnimation(buttonAnimation);

                        // Avvia un Handler per ripristinare le dimensioni dopo un secondo
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Ripristina le dimensioni originali
                                v.clearAnimation();
                            }
                        }, 200); // 1000 millisecondi = 1 secondo
                        break;
                }
                return false;
            }
        });

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
