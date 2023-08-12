package com.example.robotinteraction;
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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class Activity7_Farewelling extends AppCompatActivity {

    private SocketManager socket;
    private Animation buttonAnimation;
    private Button buttonRetrieve;
    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_7farewelling);

        // Ottieni il parametro "selectedDrink" dalla chiamata all'activity
        String selectedDrink = getIntent().getStringExtra("selectedDrink");

        // Prendo l'id del TextView DrinkName
        TextView textViewDrinkName = findViewById(R.id.textViewDrinkName);

        // Setto il testo per il DrinkName
        textViewDrinkName.setText(selectedDrink);

        // Prendo l'id del TextView Description
        TextView textViewDrinkDescription = findViewById(R.id.textViewDrinkDescription);

        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);

        buttonRetrieve = findViewById(R.id.buttonRetrieve);


        // Chiedo al server di inviare la descrizione del drink selezionato
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean operationDone = false;

                while(operationDone == false){
                    try {

                        // Invio messaggio di richiesta di descrizione del drink
                        socket.sendMessage("DRINK_DESCRIPTION");

                        // Invio il nome del drink
                        socket.sendMessage(selectedDrink);

                        // Ricevo descrizione del drink
                        String responseDescription = null;
                        responseDescription = socket.receiveMessage();

                        // Se la risposta è != null e dal messaggio di errore, inserisco il valore
                        // della descrizione nell' EditText corrispondente
                        if(responseDescription != null && !responseDescription.equalsIgnoreCase("DRINK_DESCRIPTION_NOT_FOUND")){
                            textViewDrinkDescription.setText(responseDescription);
                            operationDone = true;
                        }else{
                            textViewDrinkDescription.setText("");
                        }

                    }catch (IOException e){
                        Log.d("Activity7_Farewelling","Problema nello scambio di messaggi per" +
                                "la descrizione del drink!");
                    }
                }

            }
        }).start();

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Activity7_Farewelling.this, Activity0_OutOfSight.class);
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

    public void onClickRitira(View v) {
        buttonRetrieve.startAnimation(buttonAnimation);
        openFarewellingActivity();
    }
    private void openFarewellingActivity() {
        Intent intent = new Intent(this, Activity8_Gone.class);
        startActivity(intent);

    }
}
