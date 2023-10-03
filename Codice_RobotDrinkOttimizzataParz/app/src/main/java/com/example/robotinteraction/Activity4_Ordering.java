package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Activity4_Ordering extends AppCompatActivity {

    private TextView textViewRecommendedDrink; // TextView per visualizzare il drink raccomandato
    private Spinner spinnerDrinks;  // Spinner per selezionare un drink dalla lista
    private final List<String> drinkList = new ArrayList<>();  // Lista dei drink
    private Animation buttonAnimation;  // Animazione per i pulsanti
    private Button buttonOrderRecommendedDrink, buttonOrder;  // Pulsanti per ordinare
    private static final long TIME_THRESHOLD = 60000; // 60 secondi
    private final Handler handler = new Handler(Looper.getMainLooper());  // Handler per il timer di inattività
    private Runnable runnable;  // Runnable per la logica del timer di inattività
    private Socket_Manager socket;  // Manager del socket per la comunicazione con il server
    private TextView textViewLoggedIn;
    private final Random random = new Random();
    private String sessionID = "-1", user = "Guest", recommendedDrink;
    private ImageButton exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4ordering);
        getWindow().setWindowAnimations(0);

        connection();
        initUIComponents();
        setupListeners();
        receiveParam();
        setUpComponent();

    }

    private void connection() {
        socket = Socket_Manager.getInstance(); // Ottieni l'istanza del gestore del socket
        runnable = () -> navigateTo(sessionID, user);
    }
    private void initUIComponents() {
        textViewRecommendedDrink = findViewById(R.id.textViewDrinkName);
        spinnerDrinks = findViewById(R.id.spinnerDrinks);
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        buttonOrderRecommendedDrink = findViewById(R.id.buttonOrderRecommendedDrink);
        buttonOrder = findViewById(R.id.buttonOrder);
        textViewLoggedIn = findViewById(R.id.textViewLoggedIn);
        exitButton = findViewById(R.id.exitToggle);
    }
    private void setupListeners() {
        setTouchListenerForAnimation(buttonOrderRecommendedDrink);
        setTouchListenerForAnimation(buttonOrder);
        setTouchListenerForAnimation(exitButton);
    }
    private void setTouchListenerForAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                resetInactivityTimer();
                applyButtonAnimation(v);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return false;
        });
    }
    private void applyButtonAnimation(View v) {
        v.startAnimation(buttonAnimation);
        new Handler().postDelayed(v::clearAnimation, 100);
    }
    private void navigateTo(String param1, String param2) {
        Intent intent = new Intent(Activity4_Ordering.this, Activity0_OutOfSight.class);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        startActivity(intent);
    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2, String param3) {
        if(!("Guest".equals(user))) {
            try {
                socket.send("USER_STOP_ORDERING");
                Thread.sleep(500);
                socket.send(sessionID);
                Thread.sleep(500);
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Errore nella connessione. Continuerai come Ospite...", Toast.LENGTH_SHORT).show());
                sessionID = "-1";
                user = "Guest";
            }
        }

        Intent intent = new Intent(Activity4_Ordering.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        intent.putExtra("param3", param3);
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
    public void onClickOrdina(View v) {
        v.setClickable(false);
        resetInactivityTimer();
        String selectedDrink = spinnerDrinks.getSelectedItem().toString();
        navigateToParam(Activity5_Serving.class, sessionID, user, selectedDrink);
    }
    public void onClickConsigliato(View v) {
        v.setClickable(false);
        resetInactivityTimer(); // Aggiungi questa linea per reimpostare il timer
        String recommendedDrink = textViewRecommendedDrink.getText().toString();
        navigateToParam(Activity5_Serving.class, sessionID, user, recommendedDrink);
    }
    public void onClickExit(View v) {
        v.setClickable(false);
        resetInactivityTimer(); // Aggiungi questa linea per reimpostare il timer
        navigateToParam(Activity8_Gone.class, sessionID, user, "ORDERING");
    }
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            sessionID = intent.getStringExtra("param1");
            user = intent.getStringExtra("param2");
            String inputString = intent.getStringExtra("param4");
            recommendedDrink = intent.getStringExtra("param5");

            String[] subStrings = inputString.split(",");
            Collections.addAll(drinkList, subStrings);
            int atIndex = user.indexOf("@");

            if (atIndex != -1) {
                String username = user.substring(0, atIndex);
                textViewLoggedIn.setText(username); // Direttamente, senza runOnUiThread
            } else {
                textViewLoggedIn.setText(user); // Direttamente, senza runOnUiThread
            }

            new Thread(() -> {
                if(!("Guest".equals(user))) {
                    try {
                        socket.send("ADD_USER_ORDERING");
                        socket.receive();
                        // gestisci la risposta qui se necessario
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Errore nella connessione. Continuerai come Ospite...", Toast.LENGTH_SHORT).show());
                        sessionID = "-1";
                        user = "Guest";
                    }
                }
            }).start();
        }
    }
    private void setUpComponent() {
        setRandomRecommendedDrink();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, drinkList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDrinks.setAdapter(adapter); // Direttamente, senza runOnUiThread
    }
    private void setRandomRecommendedDrink() {
         textViewRecommendedDrink.setText(recommendedDrink);
    }

    public void ExitOrdering(View v) {

        v.setClickable(false);
        if(!("Guest".equals(user)) && socket != null) {
            try {
                socket.send("USER_GONE");
                Thread.sleep(500);
                if(Integer.parseInt(sessionID) != 0) {
                    socket.send(sessionID);
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Errore nella connessione. Continuerai come Ospite...", Toast.LENGTH_SHORT).show());
                sessionID = "-1";
                user = "Guest";
            }
        }else if(socket != null){
            socket.close();
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishAffinity();
        finish();

    }

}
