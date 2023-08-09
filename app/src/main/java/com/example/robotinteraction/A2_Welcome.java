package com.example.robotinteraction;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class A2_Welcome extends Activity {
    private Button buttonGoToWaiting;
    private Button buttonLogOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2welcome);

        // Trova i bottoni nel layout tramite i loro ID
        buttonGoToWaiting = findViewById(R.id.buttonGoToWaiting);
        buttonLogOut = findViewById(R.id.buttonLogOut);

        // Imposta un ClickListener per il pulsante "Accedi alla Coda"
        buttonGoToWaiting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Avvia l'attività "WaitingActivity" quando viene premuto il pulsante
                Intent intent = new Intent(A2_Welcome.this, A3_Waiting.class);
                startActivity(intent);
            }
        });

        // Imposta un ClickListener per il pulsante "Esci"
        buttonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Chiudi l'applicazione quando viene premuto il pulsante "Esci"
                finish();
            }
        });
    }
}

