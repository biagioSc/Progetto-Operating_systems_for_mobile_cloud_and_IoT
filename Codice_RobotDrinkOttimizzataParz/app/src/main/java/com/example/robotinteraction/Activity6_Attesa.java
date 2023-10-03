package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Activity6_Attesa extends AppCompatActivity {

    private ProgressBar progressBarWaiting;
    private TextView textViewPleaseWait, textViewLoggedIn, textViewTimeElapsed;
    private TextView textViewWaitTime;
    private String selectedDrink;
    private ImageButton exitButton;
    private Animation buttonAnimation;

    private Socket_Manager socket;
    private String sessionID = "-1", user = "Guest", innerResponseDescription;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_6attesa);
        getWindow().setWindowAnimations(0);

        connection();
        initUIComponents();
        setupListeners();
        receiveParam();
        setUpComponent();
    }

    private void connection() {
        socket = Socket_Manager.getInstance();
    }

    private void initUIComponents() {
        textViewLoggedIn = findViewById(R.id.textViewLoggedIn);
        progressBarWaiting = findViewById(R.id.progressBarWaiting);
        textViewPleaseWait = findViewById(R.id.textViewPleaseWait);
        textViewWaitTime = findViewById(R.id.textViewWaitTime);
        textViewTimeElapsed = findViewById(R.id.textViewTimeElapsed);
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        exitButton = findViewById(R.id.exitToggle);
    }

    private void setupListeners() {
        setTouchListenerForAnimation(exitButton);
    }

    private void setTouchListenerForAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    applyButtonAnimation(v);
                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    break;
            }
            return false;
        });
    }


    private void applyButtonAnimation(View v) {
        v.startAnimation(buttonAnimation);
        new Handler().postDelayed(v::clearAnimation, 100);
    }

    private void navigateToParam(String param1, String param2, String param3, String param4) {
        Intent intent = new Intent(Activity6_Attesa.this, Activity7_Farewelling.class);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        intent.putExtra("param3", param3);
        intent.putExtra("param4", param4);

        startActivity(intent);
        finish();
    }

    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            sessionID = intent.getStringExtra("param1");
            user = intent.getStringExtra("param2");
            selectedDrink = intent.getStringExtra("param3");

            int atIndex = user.indexOf("@");
            String username = (atIndex != -1) ? user.substring(0, atIndex) : user;
            textViewLoggedIn.setText(username);
        }
    }

    private void setUpComponent() {
        textViewTimeElapsed.setText("Il tuo " + selectedDrink + " è quasi pronto");

        new CountDownTimer(20000, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                textViewWaitTime.setText("Tempo di attesa: " + seconds + " secondi");
            }

            public void onFinish() {
                progressBarWaiting.setVisibility(ProgressBar.INVISIBLE);
                textViewPleaseWait.setText("Completato!");
                textViewTimeElapsed.setText("Il tuo drink è pronto");

                new Handler().postDelayed(() -> {
                    if (!("Guest".equals(user))) {
                        executorService.execute(() -> {
                            try {
                                socket.send("DRINK_DESCRIPTION");
                                Thread.sleep(500);
                                socket.send(selectedDrink);
                                Thread.sleep(500);
                                innerResponseDescription = socket.receive();
                                Thread.sleep(500);

                                if (innerResponseDescription.equals("[ERROR]")) {
                                    throw new Exception();
                                }

                                if (innerResponseDescription.equalsIgnoreCase("DRINK_DESCRIPTION_NOT_FOUND")) {
                                    innerResponseDescription = "Descrizione non disponibile!";
                                }
                            } catch (Exception e) {
                                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Errore nella connessione. Continuerai come Ospite...", Toast.LENGTH_SHORT).show());
                                sessionID = "-1";
                                user = "Guest";
                                innerResponseDescription = "Descrizione non disponibile!";
                            }
                            navigateToParam(sessionID, user, selectedDrink, innerResponseDescription);
                        });
                    } else {
                        innerResponseDescription = "Descrizione non disponibile!";
                        navigateToParam(sessionID, user, selectedDrink, innerResponseDescription);
                    }
                }, 3000);
            }

        }.start();
    }

    public void ExitAttesa(View v) {
        v.setClickable(false);

        if (!("Guest".equals(user)) && socket != null) {
            try {
                socket.send("USER_GONE");
                Thread.sleep(500);
                if (Integer.parseInt(sessionID) != 0) {
                    socket.send(sessionID);
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Errore nella connessione. Continuerai come Ospite...", Toast.LENGTH_SHORT).show();
                sessionID = "-1";
                user = "Guest";
            }
        } else if (socket != null) {
            socket.close();
        }

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishAffinity();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
