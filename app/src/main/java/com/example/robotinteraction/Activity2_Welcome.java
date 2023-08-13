package com.example.robotinteraction;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

public class Activity2_Welcome extends Activity {

    private Button buttonCheckNextState, buttonLogOut;
    private Animation buttonAnimation;
    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    private String sessionID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2welcome);

        buttonCheckNextState = findViewById(R.id.buttonChecknextState);
        buttonLogOut = findViewById(R.id.buttonLogOut);

        // Prendo il sessionID dell'utente
        Intent intent = getIntent();
        if(intent != null)
            sessionID = intent.getStringExtra("SESSION_ID");

        // Carica l'animazione dal file XML
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Activity2_Welcome.this, Activity0_OutOfSight.class);
                startActivity(intent);
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

    public void onClickQueue(View v) {
        // Avvia l'animazione
        buttonCheckNextState.startAnimation(buttonAnimation);

        // Implementa la logica per controllare il numero di persone in coda
        int numPeopleInQueue = 0; // Esempio di numero di utenti in coda

        // [SERVER] COLLEGAMENTO SERVER PER NUMERO UTENTI CODA
        // numPeopleInQueue = NUMERO DAL SERVER.TOINT()


        Intent intent;
        if (numPeopleInQueue < 2) {
            // Vai alla schermata di Ordering
            intent = new Intent(Activity2_Welcome.this, Activity4_Ordering.class);
            startActivity(intent);
        } else {
            // Vai alla schermata di Waiting passando il numero di utenti in coda come parametro
            intent = new Intent(Activity2_Welcome.this, Activity3_Waiting.class);
            intent.putExtra("numPeopleInQueue", numPeopleInQueue);
            startActivity(intent);
        }

        // In entrambi i casi (if o else) passo il sessionID
        intent.putExtra("SESSION_ID",sessionID);

    }
    public void onClickExit(View v) {
        // Chiudi l'app
        buttonLogOut.startAnimation(buttonAnimation);
        finish();
    }

}

