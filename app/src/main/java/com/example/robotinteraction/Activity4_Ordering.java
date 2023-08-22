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
    private List<String> drinkList = new ArrayList<>();  // Lista dei drink
    private Animation buttonAnimation;  // Animazione per i pulsanti
    private TextView buttonOrderRecommendedDrink, buttonOrder;  // Pulsanti per ordinare
    private static final long TIME_THRESHOLD = 60000; // 60 secondi
    private Handler handler = new Handler(Looper.getMainLooper());  // Handler per il timer di inattività
    private Runnable runnable;  // Runnable per la logica del timer di inattività
    private Activity_SocketManager socket;  // Manager del socket per la comunicazione con il server
    private TextView textViewLoggedIn;
    private Random random = new Random();
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
        socket.send("ADD_USER_ORDERING");

        // Azione da eseguire dopo l'inattività
        runnable = () -> navigateTo(Activity0_OutOfSight.class);
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
        new Handler().postDelayed(() -> v.clearAnimation(), 200);
    }
    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(Activity4_Ordering.this, targetActivity);
        startActivity(intent);
    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2, String param3) {
        socket.send("USER_STOP_ORDERING");
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
        resetInactivityTimer(); // Aggiungi questa linea per reimpostare il timer
        String selectedDrink = spinnerDrinks.getSelectedItem().toString();
        navigateToParam(Activity5_Serving.class, sessionID, user, selectedDrink);
    }
    public void onClickConsigliato(View v) {
        resetInactivityTimer(); // Aggiungi questa linea per reimpostare il timer
        String recommendedDrink = textViewRecommendedDrink.getText().toString();
        navigateToParam(Activity5_Serving.class, sessionID, user, recommendedDrink);
    }
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            sessionID = intent.getStringExtra("param1");
            user = intent.getStringExtra("param2");

            int atIndex = user.indexOf("@");

            // Verificare se è presente il simbolo "@"
            if (atIndex != -1) {
                String username = user.substring(0, atIndex);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewLoggedIn.setText(username);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        textViewLoggedIn.setText(user);
                    }
                });
            }
        }
    }
    private void setUpComponent() {

        try{
            socket.send("DRINK_LIST");
            String inputString = socket.receive();
            String[] subStrings = inputString.split(" ");
            for (String subString : subStrings) {
                drinkList.add(subString);
            }
        }catch (Exception e){
            drinkList.add("Mojito");
            drinkList.add("Martini");
            drinkList.add("Midori");
            drinkList.add("Manhattan");
            drinkList.add("Negroni");
            drinkList.add("Daiquiri");
            drinkList.add("Pina Colada");
            drinkList.add("Gin Lemon");
        }

        setRandomRecommendedDrink();

        // Inizializza il dropdown menu con gli elementi dalla lista dei drink
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, drinkList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDrinks.setAdapter(adapter);

    }
    private void setRandomRecommendedDrink() {
        try{
            socket.send("SUGG_DRINK");
            recommendedDrink = socket.receive();
        }catch (Exception e){
            int randomIndex = random.nextInt(drinkList.size());
            recommendedDrink = drinkList.get(randomIndex);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewRecommendedDrink.setText(recommendedDrink);
            }
        });
    }

}
