package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Activity4_Ordering extends AppCompatActivity {

    private TextView textViewRecommendedDrink;
    private Spinner spinnerDrinks;

    private List<String> drinkList = new ArrayList<>();
    private Random random = new Random();
    private Animation buttonAnimation;
    private Button buttonOrderRecommendedDrink, buttonOrder;
    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4ordering);

        textViewRecommendedDrink = findViewById(R.id.textViewRecommendedDrink);
        spinnerDrinks = findViewById(R.id.spinnerDrinks);

        // Carica l'animazione dal file XML
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        buttonOrderRecommendedDrink = findViewById(R.id.buttonOrderRecommendedDrink);
        buttonOrder = findViewById(R.id.buttonOrder);

        // [SERVER] COLLEGAMENTO SERVER PER DIRGLI CHE STO IN ORDERING
        // [SERVER] COLLEGAMENTO SERVER PER DRINK CONSIGLIATO
        // [SERVER] COLLEGAMENTO SERVER PER LISTA DRINK

        // Aggiungi gli elementi alla lista dei drink
        drinkList.add("Mojito");
        drinkList.add("Martini");
        drinkList.add("Midori");
        drinkList.add("Manhattan");
        drinkList.add("Negroni");
        drinkList.add("Daiquiri");
        drinkList.add("Pina Colada");
        drinkList.add("Gin Lemon");

        // Imposta il drink raccomandato casualmente
        setRandomRecommendedDrink();

        // Inizializza il dropdown menu con gli elementi dalla lista dei drink
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, drinkList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDrinks.setAdapter(adapter);

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Activity4_Ordering.this, Activity0_OutOfSight.class);
                startActivity(intent);
                finish();
            }
        };

        startInactivityTimer();
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
        buttonOrder.startAnimation(buttonAnimation);
        String selectedDrink = spinnerDrinks.getSelectedItem().toString();
        openServingActivity(selectedDrink);
    }
    public void onClickConsigliato(View v) {
        buttonOrderRecommendedDrink.startAnimation(buttonAnimation);
        String recommendedDrink = textViewRecommendedDrink.getText().toString();
        openServingActivity(recommendedDrink);
    }
    private void setRandomRecommendedDrink() {
        int randomIndex = random.nextInt(drinkList.size());
        String recommendedDrink = drinkList.get(randomIndex);
        textViewRecommendedDrink.setText(recommendedDrink);
    }

    private void openServingActivity(String drink) {
        Intent intent = new Intent(this, Activity5_Serving.class);
        intent.putExtra("selectedDrink", drink);
        startActivity(intent);
        //[SERVER] DIGLI FINE
    }
}
