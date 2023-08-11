package com.example.robotinteraction;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

public class Activity2_Welcome extends Activity {

    private Button buttonCheckNextState, buttonLogOut;
    private Animation buttonAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2welcome);

        buttonCheckNextState = findViewById(R.id.buttonChecknextState);
        buttonLogOut = findViewById(R.id.buttonLogOut);

        // Carica l'animazione dal file XML
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);

    }
    public void onClickQueue(View v) {
        // Avvia l'animazione
        buttonCheckNextState.startAnimation(buttonAnimation);

        // Implementa la logica per controllare il numero di persone in coda
        int numPeopleInQueue = 0; // Esempio di numero di utenti in coda

        // [SERVER] COLLEGAMENTO SERVER PER NUMERO UTENTI CODA
        // numPeopleInQueue = NUMERO DAL SERVER.TOINT()

        if (numPeopleInQueue < 2) {
            // Vai alla schermata di Ordering
            Intent intent = new Intent(Activity2_Welcome.this, Activity4_Ordering.class);
            startActivity(intent);
        } else {
            // Vai alla schermata di Waiting passando il numero di utenti in coda come parametro
            Intent intent = new Intent(Activity2_Welcome.this, Activity3_Waiting.class);
            intent.putExtra("numPeopleInQueue", numPeopleInQueue);
            startActivity(intent);
        }
    }
    public void onClickExit(View v) {
        // Chiudi l'app
        buttonLogOut.startAnimation(buttonAnimation);
        finish();
    }
}

