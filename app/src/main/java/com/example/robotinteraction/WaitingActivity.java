package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
        setContentView(R.layout.activity_3waiting);
        waitingTextView = findViewById(R.id.textViewQueueCount);
        socket = SocketManager.getInstance();
        updateWaitingCount();
    }

    private void updateWaitingCount() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String message = null;
                while(true) {

                    try
                    {
                        socket.sendMessage("CHECK_WAITING");
                        message = socket.receiveMessage();
                        Thread.sleep(1000);
                    }
                    catch (IOException E)
                    {
                        Log.d("WaitingActvity","[CONNESIONE] Ho inviato il messaggio di wait ma ho ricevuto un Errore ");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    if(message != null){

                        // Supponendo che "Wait Over" sia il messaggio che indica la fine dell'attesa
                        if (message.equalsIgnoreCase("WAIT_OVER")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(WaitingActivity.this,OrderingActivity.class);
                                    startActivity(intent);
                                }
                            });
                            break;
                        }
                        else {
                            try {
                                int counter = Integer.parseInt(message);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        waitingTextView.setText("Persone in coda : " + counter);
                                    }
                                });
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
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
    public void onBackButtonClick(View view)
    {


        // Termino activity corrente e passo all'activity immediatamente precedente Main
        finish();
    }

    //Gestisco il caso 2
    @Override
    public void onBackPressed()
    {
        // Chiamo il comportamento di onBackPressed predefinito.
        super.onBackPressed();
    }

}
