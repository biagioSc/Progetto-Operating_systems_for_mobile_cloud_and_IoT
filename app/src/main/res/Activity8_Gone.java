package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class Activity8_Gone extends AppCompatActivity {

    private Button buttonExit;

    private SocketManager socket;

    private String sessionID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_8gone);

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
