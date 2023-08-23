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

    private TextView textViewLoggedIn, textViewDrinkName, textViewDrinkDescription;
    private Animation buttonAnimation;
    private TextView buttonRetrieve;
    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private Activity_SocketManager socket;  // Manager del socket per la comunicazione con il server
    private String sessionID = "-1", user = "Guest";
    private String selectedDrink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_7farewelling);

        connection();
        initUIComponents();
        setupListeners();

        receiveParam();
        setUpComponent();
    }
    private void connection() {
        socket = Activity_SocketManager.getInstance(); // Ottieni l'istanza del gestore del socket

        // Azione da eseguire dopo l'inattività
        runnable = () -> navigateTo(Activity0_OutOfSight.class);
    }
    private void initUIComponents() {
        textViewDrinkName = findViewById(R.id.textViewDrinkName);
        buttonRetrieve = findViewById(R.id.buttonRetrieve);
        textViewDrinkDescription = findViewById(R.id.textViewDrinkDescription);
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        textViewLoggedIn = findViewById(R.id.textViewLoggedIn);

    }
    private void setupListeners() {
        setTouchListenerForAnimation(buttonRetrieve);
    }
    private void setTouchListenerForAnimation(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    resetInactivityTimer();
                    applyButtonAnimation(v);
                }
                return false;
            }
        });
    }
    private void applyButtonAnimation(View v) {
        v.startAnimation(buttonAnimation);
        new Handler().postDelayed(v::clearAnimation, 200);
    }
    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(Activity7_Farewelling.this, targetActivity);
        startActivity(intent);
    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2) {
        Intent intent = new Intent(Activity7_Farewelling.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        startActivity(intent);
        finish();
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
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            sessionID = intent.getStringExtra("param1");
            user = intent.getStringExtra("param2");
            selectedDrink = intent.getStringExtra("param3");

            int atIndex = user.indexOf("@");

            // Verificare se è presente il simbolo "@"
            if (atIndex != -1) {
                String username = user.substring(0, atIndex);
                runOnUiThread(() -> textViewLoggedIn.setText(username));
            } else {
                runOnUiThread(() -> textViewLoggedIn.setText(user));
            }
        }
    }
    private void setUpComponent() {
        String innerResponseDescription;

        if(!("Guest".equals(user))) {
            try {
                socket.send("DRINK_DESCRIPTION");
                innerResponseDescription = socket.receive();

            } catch (Exception e) {
                innerResponseDescription = "Descrizione non disponibile!";
            }
        }else{
            innerResponseDescription = "Descrizione non disponibile!";
        }

        if(innerResponseDescription != null && !innerResponseDescription.equalsIgnoreCase("DRINK_DESCRIPTION_NOT_FOUND")){
            String finalInnerResponseDescription = innerResponseDescription;
            runOnUiThread(() -> {
                textViewDrinkName.setText(selectedDrink);
                textViewDrinkDescription.setText(finalInnerResponseDescription);
            });

        }else {
            runOnUiThread(() -> {
                textViewDrinkName.setText(selectedDrink);
                textViewDrinkDescription.setText("Descrizione non disponibile!");
            });
        }
    }
    public void onClickRitira(View v) {
        resetInactivityTimer(); // Aggiungi questa linea per reimpostare il timer
        navigateToParam(Activity8_Gone.class, sessionID, user);
    }

}
