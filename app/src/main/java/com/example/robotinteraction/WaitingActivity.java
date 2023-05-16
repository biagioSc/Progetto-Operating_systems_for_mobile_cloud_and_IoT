package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import java.io.IOException;
import java.lang.Integer;


import androidx.appcompat.app.AppCompatActivity;

public class WaitingActivity extends AppCompatActivity {

    private TextView waitingTextView;
    private SocketManager socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);
        waitingTextView = findViewById(R.id.textViewWaiting);
        socket = SocketManager.getInstance();
        updateWaitingCount();


    }

    private void updateWaitingCount() {

        new Thread(new Runnable() {
            @Override
            public void run() {

                while(true) {
                    String message = null;
                    try {
                        message = socket.receiveMessage();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // Supponendo che "Wait Over" sia il messaggio che indica la fine dell'attesa
                    if (message.equalsIgnoreCase("WAIT_OVER")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(WaitingActivity.this, OrderingActivity.class);
                                startActivity(intent);
                            }
                        });
                        break;
                    }
                    // Altrimenti provo a trasformare il messaggio in un numero per vedere se il Server
                    // mi ha inviato quante persone mancano
                    else {
                        try {
                            int counter = Integer.parseInt(message);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    waitingTextView.setText("Ti precedono n." + counter + " utenti.");
                                }
                            });
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    // La waiting Queue termina in maniera positiva se l'utente passa all'ordering
    // Termina in maniera negativa in due casi:
    // 1. Viene premuto il button Indietro
    // 2. Viene premuto il pulsante indietro integrato in Android

    //Gestisco il caso 1
    public void onBackButtonClick(View view){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.sendMessage("STOP_WAITING");
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();

        // Termino activity corrente e passo all'activity immediatamente precedente Main
        finish();
    }

    //Gestisco il caso 2
    @Override
    public void onBackPressed() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Invio un messaggio al server per informarlo che l'utente ha smesso di aspettare.
                    socket.sendMessage("STOP_WAITING");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Chiamo il comportamento di onBackPressed predefinito.
        super.onBackPressed();
    }

}
