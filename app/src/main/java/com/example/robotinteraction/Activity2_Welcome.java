package com.example.robotinteraction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import java.util.Random;

import kotlin.UShort;

public class Activity2_Welcome extends Activity {

    private Button buttonCheckNextState;
    private Animation buttonAnimation;
    private static final long TIME_THRESHOLD = 60000; // 60 secondi
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private ImageButton exitButton;
    private String sessionID = "-1", user = "Guest", inputString, recommendedDrink;
    private Socket_Manager socket;
    private TextView textViewLoggedIn;
    private int numPeopleInQueue = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2welcome);

        getWindow().setWindowAnimations(0);

        connection();
        initUIComponents();
        setupListeners();

        receiveParam();
    }
    private void connection() {
        socket = Socket_Manager.getInstance(); // Ottieni l'istanza del gestore del socket
        runnable = () -> navigateTo(Activity0_OutOfSight.class, sessionID, user);
    }
    private void initUIComponents() {
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        textViewLoggedIn = findViewById(R.id.textViewLoggedIn);
        buttonCheckNextState = findViewById(R.id.buttonChecknextState);
        exitButton = findViewById(R.id.exitToggle);
    }
    private void setupListeners() {
        setTouchListenerForAnimation(exitButton);
        setTouchListenerForAnimation(buttonCheckNextState);
    }
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            sessionID = intent.getStringExtra("param1");
            user = intent.getStringExtra("param2");

            int atIndex = user.indexOf("@");

            if (atIndex != -1) {
                String username = user.substring(0, atIndex);
                runOnUiThread(() -> textViewLoggedIn.setText(username));
            } else {
                runOnUiThread(() -> textViewLoggedIn.setText(user));
            }
        }
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
        new Handler().postDelayed(v::clearAnimation, 100);
    }
    private void navigateTo(Class<?> targetActivity, String param1, String param2) {
        Intent intent = new Intent(Activity2_Welcome.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        startActivity(intent);
    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2, int param3, String param4, String param5) {
        Intent intent = new Intent(Activity2_Welcome.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        if(param3 != 0) intent.putExtra("param3", param3);
        intent.putExtra("param4", param4);
        intent.putExtra("param5", param5);

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
    public void onClickQueue(View v) {
        resetInactivityTimer();
        v.setClickable(false);

        View loadingView = getLayoutInflater().inflate(R.layout.activity_00popuploading, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(loadingView);
        dialogBuilder.setCancelable(false); // Evita la chiusura del messaggio di caricamento toccando al di fuori
        AlertDialog loadingDialog = dialogBuilder.create();
        loadingDialog.show();
        loadingDialog.setCancelable(false);

        buttonCheckNextState.setClickable(false);

        if("Guest".equals(user)){
            int min = 0;
            int max = 3;
            Random random = new Random();
            numPeopleInQueue = random.nextInt(max - min + 1) + min;
            inputString = "Mojito,Martini,Midori,Manhattan,Negroni,Daiquiri,Pina Colada,Gin Lemon,Spritz";
            String[] cocktails = inputString.split(",");

            int randomIndex = random.nextInt(cocktails.length);

            String randomCocktail = cocktails[randomIndex].trim();

            if (numPeopleInQueue < 2) {
                showPopupMessage();
                new Handler().postDelayed(() -> {
                    navigateToParam(Activity4_Ordering.class, sessionID, user, 0, inputString, randomCocktail);
                }, 5000);
            } else {
                navigateToParam(Activity3_Waiting.class, sessionID, user, numPeopleInQueue,inputString, randomCocktail);
            }
        }else {
            try {
                new Thread(() -> {
                    try {
                        socket.send("CHECK_USERS_ORDERING");
                        Thread.sleep(500);
                        String num = socket.receive();
                        Thread.sleep(500);
                        numPeopleInQueue = Integer.parseInt(num);

                        socket.send("DRINK_LIST");
                        Thread.sleep(500);
                        inputString = socket.receive();
                        Thread.sleep(500);
                        socket.send("SUGG_DRINK");
                        Thread.sleep(500);
                        socket.send(sessionID);
                        Thread.sleep(500);
                        recommendedDrink = socket.receive();
                        Thread.sleep(500);


                        runOnUiThread(() -> {
                            buttonCheckNextState.setClickable(true);
                        });

                        if (numPeopleInQueue < 1) {
                            loadingDialog.dismiss(); // Chiudi il messaggio di caricamento
                            navigateToParam(Activity4_Ordering.class, sessionID, user, 0, inputString, recommendedDrink);
                        } else {
                            loadingDialog.dismiss(); // Chiudi il messaggio di caricamento
                            navigateToParam(Activity3_Waiting.class, sessionID, user, numPeopleInQueue, inputString, recommendedDrink);
                        }
                    }catch (Exception e){
                        loadingDialog.dismiss();
                        throw new RuntimeException(e);
                    }
                }).start();

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Errore nella connessione. Continuerai come Ospite...", Toast.LENGTH_SHORT).show());
                sessionID = "-1";
                user = "Guest";
                int min = 0;
                int max = 3;
                Random random = new Random();
                numPeopleInQueue = random.nextInt(max - min + 1) + min;
                inputString = "Mojito,Martini,Midori,Manhattan,Negroni,Daiquiri,Pina Colada,Gin Lemon,Spritz";
                String[] cocktails = inputString.split(",");
                int randomIndex = random.nextInt(cocktails.length);
                recommendedDrink = cocktails[randomIndex].trim();

                if (numPeopleInQueue < 2) {
                    showPopupMessage();
                    new Handler().postDelayed(() -> {
                        navigateToParam(Activity4_Ordering.class, sessionID, user, 0, inputString, recommendedDrink);
                    }, 3000);
                } else {
                    navigateToParam(Activity3_Waiting.class, sessionID, user, numPeopleInQueue, inputString, recommendedDrink);
                }
            }
        }
    }
    public void showPopupMessage() {
        runOnUiThread(() -> {
            LayoutInflater inflater = LayoutInflater.from(Activity2_Welcome.this);
            View customView = inflater.inflate(R.layout.activity_00popupwelcome, null);

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Activity2_Welcome.this);
            builder.setCustomTitle(customView)
                    .setCancelable(false);

            android.app.AlertDialog dialog = builder.create();
            dialog.show();

            // Chiudi il popup dopo 5 secondi
            new Handler().postDelayed(() -> {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }, 4000); // 5000 millisecondi = 5 secondi
        });
    }

    public void ExitWelcome(View v) {

        v.setClickable(false);
        if(!("Guest".equals(user)) && socket != null) {
            try {
                socket.send("USER_GONE");
                Thread.sleep(500);
                if(Integer.parseInt(sessionID) != 0) {
                    socket.send(sessionID);
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Errore nella connessione. Continuerai come Ospite...", Toast.LENGTH_SHORT).show());
                sessionID = "-1";
                user = "Guest";
            }
        }else if(socket != null){
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
