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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Activity4_Ordering extends AppCompatActivity {

    private TextView textViewRecommendedDrink;  // TextView per visualizzare il drink raccomandato
    private Spinner spinnerDrinks;  // Spinner per selezionare un drink dalla lista
    private final List<String> drinkList = new ArrayList<>();  // Lista dei drink
    private Animation buttonAnimation;  // Animazione per i pulsanti
    private TextView buttonOrderRecommendedDrink, buttonOrder;  // Pulsanti per ordinare
    private static final long TIME_THRESHOLD = 60000; // 60 secondi
    private final Handler handler = new Handler(Looper.getMainLooper());  // Handler per il timer di inattività
    private Runnable runnable;  // Runnable per la logica del timer di inattività
    private Activity_SocketManager socket;  // Manager del socket per la comunicazione con il server
    private TextView textViewLoggedIn;
    private final Random random = new Random();
    private String sessionID = "-1", user = "Guest", recommendedDrink;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4ordering);

        connection();
        initUIComponents();
        setupListeners();

        receiveParam();
        setUpComponent();

    }

    private void connection() {
        socket = Activity_SocketManager.getInstance(); // Ottieni l'istanza del gestore del socket
        runnable = () -> navigateTo(Activity0_OutOfSight.class, sessionID, user);
    }
    private void initUIComponents() {
        textViewRecommendedDrink = findViewById(R.id.textViewDrinkName);
        spinnerDrinks = findViewById(R.id.spinnerDrinks);
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        buttonOrderRecommendedDrink = findViewById(R.id.buttonOrderRecommendedDrink);
        buttonOrder = findViewById(R.id.buttonOrder);
        textViewLoggedIn = findViewById(R.id.textViewLoggedIn);
    }
    private void setupListeners() {
        setTouchListenerForAnimation(buttonOrderRecommendedDrink);
        setTouchListenerForAnimation(buttonOrder);
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
    private void navigateTo(Class<?> targetActivity, String param1, String param2) {
        Intent intent = new Intent(Activity4_Ordering.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        startActivity(intent);
    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2, String param3) {
        if(!("Guest".equals(user))) {
            try {
                socket.send("USER_STOP_ORDERING");
                Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio
                socket.send(sessionID);
                Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio
            } catch (InterruptedException e) {
                e.printStackTrace();
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
        resetInactivityTimer(); // Aggiungi questa linea per reimpostare il timer
        String selectedDrink = spinnerDrinks.getSelectedItem().toString();
        navigateToParam(Activity5_Serving.class, sessionID, user, selectedDrink);
    }
    public void onClickConsigliato(View v) {
        v.setClickable(false);
        resetInactivityTimer(); // Aggiungi questa linea per reimpostare il timer
        String recommendedDrink = textViewRecommendedDrink.getText().toString();
        navigateToParam(Activity5_Serving.class, sessionID, user, recommendedDrink);
    }
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            sessionID = intent.getStringExtra("param1");
            user = intent.getStringExtra("param2");
            String inputString = intent.getStringExtra("param4");
            recommendedDrink = intent.getStringExtra("param5");

            String[] subStrings = inputString.split(",");
            for (String subString : subStrings) {
                drinkList.add(subString);
            }
            int atIndex = user.indexOf("@");

            if (atIndex != -1) {
                String username = user.substring(0, atIndex);
                runOnUiThread(() -> textViewLoggedIn.setText(username));
            } else {
                runOnUiThread(() -> textViewLoggedIn.setText(user));
            }
            try{
                socket.send("ADD_USER_ORDERING");
                Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio
                socket.send(sessionID);
                Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    private void setUpComponent() {
        setRandomRecommendedDrink();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, drinkList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        runOnUiThread(() -> spinnerDrinks.setAdapter(adapter));
    }
    private void setRandomRecommendedDrink() {
        if(!("Guest".equals(user))) {
            runOnUiThread(() -> textViewRecommendedDrink.setText(recommendedDrink));
        }else{
            runOnUiThread(() -> textViewRecommendedDrink.setText(recommendedDrink));
        }
    }

}
