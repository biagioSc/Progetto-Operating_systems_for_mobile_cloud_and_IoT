package com.example.robotinteraction;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Activity6_Attesa extends AppCompatActivity {

    private ProgressBar progressBarWaiting;
    private TextView textViewPleaseWait, textViewLoggedIn, textViewTimeElapsed;
    private TextView textViewWaitTime;
    private String selectedDrink;
    private Socket_Manager socket;  // Manager del socket per la comunicazione con il server
    private String sessionID = "-1", user = "Guest", innerResponseDescription;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_6attesa);
        getWindow().setWindowAnimations(0);

        connection();
        initUIComponents();

        receiveParam();
        setUpComponent();
    }
    private void connection() {
        socket = Socket_Manager.getInstance(); // Ottieni l'istanza del gestore del socket
    }
    private void initUIComponents() {
        textViewLoggedIn = findViewById(R.id.textViewLoggedIn);
        progressBarWaiting = findViewById(R.id.progressBarWaiting);
        textViewPleaseWait = findViewById(R.id.textViewPleaseWait);
        textViewWaitTime = findViewById(R.id.textViewWaitTime);
        textViewTimeElapsed = findViewById(R.id.textViewTimeElapsed);

    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2, String param3, String param4) {
        Intent intent = new Intent(Activity6_Attesa.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        intent.putExtra("param3", param3);
        intent.putExtra("param4", param4);

        startActivity(intent);
        finish();
    }
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            sessionID = intent.getStringExtra("param1");
            user = intent.getStringExtra("param2");
            selectedDrink = intent.getStringExtra("param3");

            int atIndex = user.indexOf("@");

            if (atIndex != -1) {
                String username = user.substring(0, atIndex);
                runOnUiThread(() -> textViewLoggedIn.setText(username));
            } else {
                runOnUiThread(() -> textViewLoggedIn.setText(user));
            }
        }
    }
    private void setUpComponent(){
        textViewTimeElapsed.setText("Il tuo " + selectedDrink + " è quasi pronto");

        new CountDownTimer(20000, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                runOnUiThread(() -> textViewWaitTime.setText("Tempo di attesa: " + seconds + " secondi"));
            }

            public void onFinish() {
                progressBarWaiting.setVisibility(ProgressBar.INVISIBLE);
                runOnUiThread(() -> {
                    textViewPleaseWait.setText("Completato!");
                    textViewTimeElapsed.setText("Il tuo drink è pronto");
                });

                new Handler().postDelayed(() -> {
                    if(!("Guest".equals(user))) {
                        new Thread(() -> {
                            try {
                                socket.send("DRINK_DESCRIPTION");
                                Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio
                                socket.send(selectedDrink);
                                Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio
                                innerResponseDescription = socket.receive();
                                Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio

                                if(innerResponseDescription == null || innerResponseDescription.equalsIgnoreCase("DRINK_DESCRIPTION_NOT_FOUND")) {
                                    innerResponseDescription = "Descrizione non disponibile!";
                                }
                            } catch (Exception e) {
                                innerResponseDescription = "Descrizione non disponibile!";
                            }
                            navigateToParam(Activity7_Farewelling.class, sessionID, user, selectedDrink, innerResponseDescription);

                        }).start();
                    } else {
                        innerResponseDescription = "Descrizione non disponibile!";
                        navigateToParam(Activity7_Farewelling.class, sessionID, user, selectedDrink, innerResponseDescription);
                    }
                }, 3000); // 3000 millisecondi corrispondono a 3 secondi
            }

        }.start();
    }

}
