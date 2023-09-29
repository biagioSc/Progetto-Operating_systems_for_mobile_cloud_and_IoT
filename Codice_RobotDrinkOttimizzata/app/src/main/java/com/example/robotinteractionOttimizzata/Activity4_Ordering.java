package com.example.robotinteractionOttimizzata;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.List;

public class Activity4_Ordering extends AppCompatActivity {

    private TextView textViewRecommendedDrink;
    private Spinner spinnerDrinks;
    private final List<String> drinkList = new ArrayList<>();
    private Animation buttonAnimation;
    private Button buttonOrderRecommendedDrink, buttonOrder;
    private static final long TIME_THRESHOLD = 60000;
    private final Handler handler = new Handler();
    private Runnable runnable;
    private Socket_Manager socket;
    private TextView textViewLoggedIn;
    private String sessionID = "-1", user = "Guest", recommendedDrink;
    private ImageButton exitButton;

    private final View.OnTouchListener animationTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                resetInactivityTimer();
                applyButtonAnimation(v);
            }
            return false;
        }
    };

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
        socket = Socket_Manager.getInstance();
        runnable = () -> navigateTo(Activity0_OutOfSight.class, sessionID, user);
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
        buttonOrderRecommendedDrink.setOnTouchListener(animationTouchListener);
        buttonOrder.setOnTouchListener(animationTouchListener);
        exitButton.setOnTouchListener(animationTouchListener);
    }

    private void applyButtonAnimation(View v) {
        v.startAnimation(buttonAnimation);
        handler.postDelayed(v::clearAnimation, 100);
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
        resetInactivityTimer();
        String recommendedDrink = textViewRecommendedDrink.getText().toString();
        navigateToParam(Activity5_Serving.class, sessionID, user, recommendedDrink);
    }

    public void onClickExit(View v) {
        v.setClickable(false);
        resetInactivityTimer();
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
            for (String subString : subStrings) {
                drinkList.add(subString);
            }
            int atIndex = user.indexOf("@");

            if (atIndex != -1) {
                String username = user.substring(0, atIndex);
                textViewLoggedIn.setText(username);
            } else {
                textViewLoggedIn.setText(user);
            }
            if(!("Guest".equals(user))) {
                try {
                    socket.send("ADD_USER_ORDERING");
                    Thread.sleep(500);
                    socket.send(sessionID);
                    Thread.sleep(500);

                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Errore nella connessione. Continuerai come Ospite...", Toast.LENGTH_SHORT).show();
                    sessionID = "-1";
                    user = "Guest";
                }
            }
        }
    }

    private void setUpComponent() {
        textViewRecommendedDrink.setText(recommendedDrink);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, drinkList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDrinks.setAdapter(adapter);
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
                Toast.makeText(getApplicationContext(), "Errore nella connessione. Continuerai come Ospite...", Toast.LENGTH_SHORT).show();
                sessionID = "-1";
                user = "Guest";
            }
        } else if(socket != null) {
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
