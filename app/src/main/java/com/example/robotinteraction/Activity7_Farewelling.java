package com.example.robotinteraction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Activity7_Farewelling extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_7farewelling);

        // Ottieni il parametro "selectedDrink" dalla chiamata all'activity
        String selectedDrink = getIntent().getStringExtra("selectedDrink");

        // Ottieni il parametro "selectedDrinkDescription" dalla chiamata all'activity
        String selectedDrinkDescription = getIntent().getStringExtra("selectedDrinkDescription");

        // Imposta il nome del drink nella TextView
        TextView textViewDrinkName = findViewById(R.id.textViewDrinkName);
        textViewDrinkName.setText(selectedDrink);

        // Imposta la descrizione del drink nella TextView
        // [SERVER] MANDO NOME DRINK
        // [SERVER] TORNA DESCRIZIONE DRINK

        TextView textViewDrinkDescription = findViewById(R.id.textViewDrinkDescription);
        textViewDrinkDescription.setText(selectedDrinkDescription);

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
