package com.example.robotinteraction;

import java.io.IOException;
import java.util.function.ToDoubleBiFunction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    public Button buttonChecknextState;
    public Button buttonLogOut;
    private SocketManager socket;
    public String response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2welcome);
        socket = SocketManager.getInstance();

        buttonLogOut = findViewById(R.id.buttonLogOut);
        buttonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent LogOut = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(LogOut);
            }

        });

        buttonChecknextState = findViewById(R.id.buttonChecknextState);
        buttonChecknextState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageState();
            }
        });
    }

    private void manageState() {
        // Codice per gestire lo stato del robot
        // Se lo stato è "WAITING" allora passa alla WaitingActivity
        // Se lo stato è "ORDERING" allora passa alla OrderingActivity
        // Creo un nuovo thread in modo da non bloccare il thread principale
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.sendMessage("CHECK_NEXT_STATE");
                    response = socket.receiveMessage();
                } catch (IOException e) {
                    Log.d("MainActivity", "[ERROR] Next state non ricevuto dal Server");
                    Log.d("MainActivity", "[ERROR] Connessione persa con il Server");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (response != null) {
                            if (response.equalsIgnoreCase("ORDERING")) {
                               Toast.makeText(WelcomeActivity.this, "Perfetto la fase di ordering è libera puoi accedergli !", Toast.LENGTH_LONG)
                                        .show();
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent passaggioOrdering = new Intent(WelcomeActivity.this, OrderingActivity.class);
                                        startActivity(passaggioOrdering);
                                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
                                    }
                                }, 1000); // Ritardo di 1 secondo

                            } else if (response.equalsIgnoreCase("WAITING")) {
                                Toast.makeText(WelcomeActivity.this,
                                        "Purtroppo il robot ora è impegnato ci sono altre persone avanti a te puoi aspettare con calma nella Zona di Waiting",
                                        Toast.LENGTH_LONG);
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent passaggioWaiting = new Intent(WelcomeActivity.this, WaitingActivity.class);
                                        startActivity(passaggioWaiting);
                                    }
                                }, 1000); // Ritardo di 1 secondo
                            } else {
                                Log.d("MainActivity", "[SERVER] Stato utente non riconosciuto");
                            }
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Si è verificato un errore lato Server", Toast.LENGTH_SHORT);
                        }
                    }
                });

            }
        }).start();

    }
}
