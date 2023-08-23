package com.example.robotinteraction;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Activity6_Attesa extends AppCompatActivity {

    private ProgressBar progressBarWaiting;
    private TextView textViewPleaseWait, textViewLoggedIn, textViewTimeElapsed;
    private TextView textViewWaitTime;
    private String selectedDrink;
    private Activity_SocketManager socket;  // Manager del socket per la comunicazione con il server
    private String sessionID = "-1", user = "Guest";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_6attesa);

        connection();
        initUIComponents();

        receiveParam();
        setUpComponent();
    }
    private void connection() {
        socket = Activity_SocketManager.getInstance(); // Ottieni l'istanza del gestore del socket
    }
    private void initUIComponents() {
        textViewLoggedIn = findViewById(R.id.textViewLoggedIn);
        progressBarWaiting = findViewById(R.id.progressBarWaiting);
        textViewPleaseWait = findViewById(R.id.textViewPleaseWait);
        textViewWaitTime = findViewById(R.id.textViewWaitTime);
        textViewTimeElapsed = findViewById(R.id.textViewTimeElapsed);

    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2, String param3) {
        Intent intent = new Intent(Activity6_Attesa.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        intent.putExtra("param3", param3);
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

        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                runOnUiThread(() -> textViewWaitTime.setText("Tempo di attesa: " + seconds + " secondi"));
            }

            public void onFinish() {
                progressBarWaiting.setVisibility(ProgressBar.INVISIBLE);
                runOnUiThread(() -> textViewPleaseWait.setText("Completato!"));
                navigateToParam(Activity7_Farewelling.class, sessionID, user, selectedDrink);
            }
        }.start();
    }

}
