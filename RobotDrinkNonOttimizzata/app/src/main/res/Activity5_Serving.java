package com.example.robotinteraction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Activity5_Serving extends AppCompatActivity {
    private Animation buttonAnimation;
    private Button buttonQuiz, buttonWaitingRoom;
    private String selectedDrink;
    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    private String sessionID = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_5serving);

        // Prendo il sessionID
        Intent intent = getIntent();
        if(intent != null)
            sessionID = intent.getStringExtra("SESSION_ID");

        // Ottieni il parametro "selectedDrink" dalla chiamata all'activity
        selectedDrink = getIntent().getStringExtra("selectedDrink");
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        buttonQuiz = findViewById(R.id.buttonQuiz);
        buttonWaitingRoom = findViewById(R.id.buttonWaitingRoom);

        // Imposta il testo nella TextView
        TextView textViewOrderStatusTitle = findViewById(R.id.textViewOrderStatusTitle);
        textViewOrderStatusTitle.setText("In preparazione: " + selectedDrink);

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Activity5_Serving.this, Activity0_OutOfSight.class);
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

    public void onClickWait(View v) {
        buttonWaitingRoom.startAnimation(buttonAnimation);
        openWaitingActivity(selectedDrink);
    }
    public void onClickQuiz(View v) {
        buttonQuiz.startAnimation(buttonAnimation);
        openChatActivity(selectedDrink);
    }
    private void openWaitingActivity(String selectedDrink) {
        Intent intent = new Intent(this, Activity6_Attesa.class);
        intent.putExtra("selectedDrink", selectedDrink);
        intent.putExtra("SESSION_ID",sessionID);
        startActivity(intent);

    }

    private void openChatActivity(String selectedDrink) {
        Intent intent = new Intent(this, Activity6_Chat.class);
        intent.putExtra("selectedDrink", selectedDrink);
        intent.putExtra("SESSION_ID",sessionID);
        startActivity(intent);

    }
}
