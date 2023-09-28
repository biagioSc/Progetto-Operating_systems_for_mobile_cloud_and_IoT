package com.example.robotinteraction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

public class Activity2_Welcome extends Activity {

    private Button buttonCheckNextState;
    private Animation buttonAnimation;
    private static final long TIME_THRESHOLD = 60000; // 60 secondi
    private final Handler handler = new Handler();
    private Runnable runnable;
    private ImageButton exitButton;
    private String sessionID = "-1", user = "Guest", inputString = "Mojito,Martini,Midori,Manhattan,Negroni,Daiquiri,Pina Colada,Gin Lemon,Spritz", recommendedDrink="Spritz";
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
        socket = Socket_Manager.getInstance();
        runnable = () -> navigateTo(Activity0_OutOfSight.class, sessionID, user);
        startInactivityTimer();
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
                textViewLoggedIn.setText(username);
            } else {
                textViewLoggedIn.setText(user);
            }
        }
    }

    private void setTouchListenerForAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                resetInactivityTimer();
                applyButtonAnimation(v);
            }
            return false;
        });
    }

    private void applyButtonAnimation(View v) {
        v.startAnimation(buttonAnimation);
        handler.postDelayed(v::clearAnimation, 100);
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

        AlertDialog loadingDialog = createLoadingDialog();
        loadingDialog.show();

        buttonCheckNextState.setClickable(false);

        if("Guest".equals(user)) {
            handleGuestQueueLogic(loadingDialog);
        } else {
            handleUserQueueLogic(loadingDialog);
        }
    }

    private AlertDialog createLoadingDialog() {
        View loadingView = getLayoutInflater().inflate(R.layout.activity_00popuploading, null);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(loadingView);
        dialogBuilder.setCancelable(false);
        return dialogBuilder.create();
    }

    private void handleGuestQueueLogic(AlertDialog loadingDialog) {
        Random random = new Random();
        numPeopleInQueue = random.nextInt(4);
        inputString = "Mojito,Martini,Midori,Manhattan,Negroni,Daiquiri,Pina Colada,Gin Lemon,Spritz";
        String[] cocktails = inputString.split(",");
        recommendedDrink = cocktails[random.nextInt(cocktails.length)].trim();

        if (numPeopleInQueue < 2) {
            showPopupMessage();
            handler.postDelayed(() -> {
                navigateToParam(Activity4_Ordering.class, sessionID, user, 0, inputString, recommendedDrink);
            }, 3000);
        } else {
            navigateToParam(Activity3_Waiting.class, sessionID, user, numPeopleInQueue, inputString, recommendedDrink);
        }
    }

    private void handleUserQueueLogic(AlertDialog loadingDialog) {
        new Thread(() -> {
            try {
                socket.send("CHECK_USERS_ORDERING");
                Thread.sleep(500);
                numPeopleInQueue = Integer.parseInt(socket.receive());

                socket.send("DRINK_LIST");
                Thread.sleep(500);
                inputString = socket.receive();

                socket.send("SUGG_DRINK");
                Thread.sleep(500);
                socket.send(sessionID);
                Thread.sleep(500);
                recommendedDrink = socket.receive();

                if(inputString.equals("[ERROR]") || recommendedDrink.equals("[ERROR]")) {
                    throw new Exception();
                }

                runOnUiThread(() -> {
                    buttonCheckNextState.setClickable(true);
                    loadingDialog.dismiss();
                });

                if (numPeopleInQueue < 1) {
                    navigateToParam(Activity4_Ordering.class, sessionID, user, 0, inputString, recommendedDrink);
                } else {
                    navigateToParam(Activity3_Waiting.class, sessionID, user, numPeopleInQueue, inputString, recommendedDrink);
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "Errore nella connessione. Continuerai come Ospite...", Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                });
                handleGuestQueueLogic(loadingDialog);
            }
        }).start();
    }

    public void showPopupMessage() {
        AlertDialog dialog = createWelcomePopup();
        if (!isFinishing() && !dialog.isShowing()) {
            dialog.show();
            handler.postDelayed(dialog::dismiss, 4000);
        }
    }

    private AlertDialog createWelcomePopup() {
        LayoutInflater inflater = LayoutInflater.from(Activity2_Welcome.this);
        View customView = inflater.inflate(R.layout.activity_00popupwelcome, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(Activity2_Welcome.this);
        builder.setCustomTitle(customView).setCancelable(false);
        return builder.create();
    }

    public void ExitWelcome(View v) {
        v.setClickable(false);
        handleExitLogic();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishAffinity();
        finish();
    }

    private void handleExitLogic() {
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
    }
}
