package com.example.robotinteraction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Activity5_Serving extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_5serving);

        // Ottieni il parametro "selectedDrink" dalla chiamata all'activity
        String selectedDrink = getIntent().getStringExtra("selectedDrink");

        // Imposta il testo nella TextView
        TextView textViewOrderStatusTitle = findViewById(R.id.textViewOrderStatusTitle);
        textViewOrderStatusTitle.setText("In preparazione: " + selectedDrink);

        // Gestisci il click sul bottone "Sala d'Attesa"
        Button buttonWaitingRoom = findViewById(R.id.buttonWaitingRoom);
        buttonWaitingRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWaitingActivity(selectedDrink);
            }
        });

        // Gestisci il click sul bottone "Quiz"
        Button buttonQuiz = findViewById(R.id.buttonQuiz);
        buttonQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChatActivity(selectedDrink);
            }
        });
    }

    private void openWaitingActivity(String selectedDrink) {
        Intent intent = new Intent(this, Activity6_Attesa.class);
        intent.putExtra("selectedDrink", selectedDrink);
        startActivity(intent);

    }

    private void openChatActivity(String selectedDrink) {
        Intent intent = new Intent(this, Activity6_Chat.class);
        intent.putExtra("selectedDrink", selectedDrink);
        startActivity(intent);

    }
}
