package com.example.robotinteraction;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Activity6_Attesa extends AppCompatActivity {

    private ProgressBar progressBarWaiting;
    private TextView textViewPleaseWait;
    private TextView textViewWaitTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_6attesa);

        progressBarWaiting = findViewById(R.id.progressBarWaiting);
        textViewPleaseWait = findViewById(R.id.textViewPleaseWait);
        textViewWaitTime = findViewById(R.id.textViewWaitTime);

        // Ottieni il parametro "selectedDrink" dalla chiamata all'activity
        final String selectedDrink = getIntent().getStringExtra("selectedDrink");

        // Imposta il testo nella TextView
        TextView textViewTimeElapsed = findViewById(R.id.textViewTimeElapsed);
        textViewTimeElapsed.setText("Il tuo " + selectedDrink + " è quasi pronto");

        // Imposta il timer per 30 secondi
        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Aggiorna il testo del tempo di attesa
                long seconds = millisUntilFinished / 1000;
                textViewWaitTime.setText("Tempo di attesa: " + seconds + " secondi");
            }

            public void onFinish() {
                // Quando il timer termina, nascondi la progress bar e mostra il messaggio di completamento
                progressBarWaiting.setVisibility(ProgressBar.INVISIBLE);
                textViewPleaseWait.setText("Completato!");

                // Apri automaticamente l'Activity "Activity7_Farewelling" e passa il parametro "selectedDrink"
                openFarewellingActivity(selectedDrink);
            }
        }.start();
    }

    private void openFarewellingActivity(String selectedDrink) {
        Intent intent = new Intent(this, Activity7_Farewelling.class);
        intent.putExtra("selectedDrink", selectedDrink);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

    }
}
