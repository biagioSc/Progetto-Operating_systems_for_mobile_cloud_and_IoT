package com.example.robotinteractionOttimizzataParz;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Activity4_Ordering extends AppCompatActivity {

    private TextView textViewRecommendedDrink;  // TextView per visualizzare il drink raccomandato
    private Spinner spinnerDrinks;  // Spinner per selezionare un drink dalla lista
    private List<String> drinkList = new ArrayList<>();  // Lista dei drink
    private Animation buttonAnimation;  // Animazione per i pulsanti
    private Button buttonOrderRecommendedDrink, buttonOrder;  // Pulsanti per ordinare
    private static final long TIME_THRESHOLD = 20000; // Soglia di tempo per l'inattività (20 secondi)
    private Handler handler = new Handler(Looper.getMainLooper());  // Handler per il timer di inattività
    private Runnable runnable;  // Runnable per la logica del timer di inattività
    private SocketManager socket;  // Manager del socket per la comunicazione con il server

    private String sessionID = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4ordering);

        // Inizializzazione dei componenti dell'interfaccia utente
        textViewRecommendedDrink = findViewById(R.id.textViewDrinkName);
        spinnerDrinks = findViewById(R.id.spinnerDrinks);
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        buttonOrderRecommendedDrink = findViewById(R.id.buttonOrderRecommendedDrink);
        buttonOrder = findViewById(R.id.buttonOrder);

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

        // Prendo il sessionID
        Intent intent = getIntent();
        if(intent != null)
            sessionID = intent.getStringExtra("SESSION_ID");

        // Comunicazione con il server
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Informa il server che si è entrati nella fase di ordinazione
                    socket.sendMessage("ORDERING");
                    // Invia al server l'ID di sessione dell'utente
                    socket.sendMessage(sessionID);

                    // Riceve dal server il drink suggerito
                    final String responseSuggestedDrink = socket.receiveMessage();

                    if (responseSuggestedDrink != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewRecommendedDrink.setText(responseSuggestedDrink);
                            }
                        });
                    }

                    // Richiede al server la lista dei drink disponibili
                    socket.sendMessage("DRINK_LIST");
                    String drinkListResponse = socket.receiveMessage();

                    if(drinkListResponse != null) {
                        String[] drinks = drinkListResponse.split(",");
                        for(String drink : drinks) {
                            drinkList.add(drink.trim());
                        }

                        // Aggiorna l'interfaccia utente con la lista dei drink
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(Activity4_Ordering.this, android.R.layout.simple_spinner_item, drinkList);
                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinnerDrinks.setAdapter(adapter);
                            }
                        });
                    }

                } catch (IOException e) {
                    Log.d("Activity4_Ordering", "Problema nella comunicazione con il server");
                }
            }
        }).start();

        // Timer di inattività
        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Activity4_Ordering.this, Activity0_OutOfSight.class);
                startActivity(intent);
            }
        };
        startInactivityTimer();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Ad ogni interazione, resetta il timer di inattività
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

    public void onClickOrdina(View v) {
        buttonOrder.startAnimation(buttonAnimation);
        String selectedDrink = spinnerDrinks.getSelectedItem().toString();
        openServingActivity(selectedDrink);
    }

    public void onClickConsigliato(View v) {
        buttonOrderRecommendedDrink.startAnimation(buttonAnimation);
        String recommendedDrink = textViewRecommendedDrink.getText().toString();
        openServingActivity(recommendedDrink);
    }

    private void openServingActivity(String drink) {
        Intent intent = new Intent(this, Activity5_Serving.class);
        // Passo il drink selezionato
        intent.putExtra("selectedDrink", drink);
        // Passo il sessionID
        intent.putExtra("SESSION_ID",sessionID);

        // Informa il server della conclusione dell'ordinazione
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    socket.sendMessage("USER_STOP_ORDERING");
                    socket.sendMessage(sessionID);

                }catch (IOException e){
                    Log.d("Acitivty4_Ordering","Errore nella user_stop_ordering");
                }
            }
        }).start();



        startActivity(intent);

    }
}
