package com.example.robotinteraction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class Activity7_Farewelling extends AppCompatActivity {

    private SocketManager socket;

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




        // Gestisci il click sul pulsante "Ritira"
        Button buttonRetrieve = findViewById(R.id.buttonRetrieve);
        buttonRetrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFarewellingActivity();
            }
        });
    }

    private void openFarewellingActivity() {
        Intent intent = new Intent(this, Activity8_Gone.class);
        startActivity(intent);

    }
}
