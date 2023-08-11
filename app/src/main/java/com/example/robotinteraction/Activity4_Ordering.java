package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_4ordering);

        textViewRecommendedDrink = findViewById(R.id.textViewRecommendedDrink);
        spinnerDrinks = findViewById(R.id.spinnerDrinks);

        // Carica l'animazione dal file XML
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);

        // [SERVER] COLLEGAMENTO SERVER PER DIRGLI CHE STO IN ORDERING

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

        // Gestisci il click sul bottone "Ordina Drink Raccomandato"
        Button buttonOrderRecommendedDrink = findViewById(R.id.buttonOrderRecommendedDrink);
        buttonOrderRecommendedDrink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonOrderRecommendedDrink.startAnimation(buttonAnimation);
                String recommendedDrink = textViewRecommendedDrink.getText().toString();
                openServingActivity(recommendedDrink);
            }
        });

        // Gestisci il click sul bottone "Ordina"
        Button buttonOrder = findViewById(R.id.buttonOrder);
        buttonOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonOrder.startAnimation(buttonAnimation);
                String selectedDrink = spinnerDrinks.getSelectedItem().toString();
                openServingActivity(selectedDrink);
            }
        });
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
