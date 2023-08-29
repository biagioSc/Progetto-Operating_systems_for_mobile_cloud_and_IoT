package com.example.robotinteraction;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Random;

public class Activity5_Serving extends AppCompatActivity {
    private Animation buttonAnimation;
    private TextView textViewOrderStatusTitle, textViewOrderStatusMessage, textViewLoggedIn;
    private Button buttonQuiz, buttonWaitingRoom;
    private String selectedDrink;
    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private Socket_Manager socket;  // Manager del socket per la comunicazione con il server
    private String sessionID = "-1", user = "Guest";
    private String[] selectedTopics = new String[2]; // Aggiungi gli argomenti desiderati

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_5serving);
        getWindow().setWindowAnimations(0);

        connection();
        initUIComponents();
        setupListeners();

        receiveParam();
        setUpComponent();
    }
    private void connection() {
        socket = Socket_Manager.getInstance(); // Ottieni l'istanza del gestore del socket
        runnable = () -> navigateTo(Activity0_OutOfSight.class, sessionID, user);
    }
    private void initUIComponents() {
        textViewLoggedIn = findViewById(R.id.textViewLoggedIn);
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        buttonQuiz = findViewById(R.id.buttonQuiz);
        buttonWaitingRoom = findViewById(R.id.buttonWaitingRoom);
        textViewOrderStatusTitle = findViewById(R.id.textViewOrderStatusTitle);
        textViewOrderStatusMessage = findViewById(R.id.textViewOrderStatusMessage);
    }
    private void setupListeners() {
        setTouchListenerForAnimation(buttonQuiz);
        setTouchListenerForAnimation(buttonWaitingRoom);
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
        new Handler().postDelayed(v::clearAnimation, 200);
    }
    private void navigateTo(Class<?> targetActivity, String param1, String param2) {
        Intent intent = new Intent(Activity5_Serving.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        startActivity(intent);
    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2, String param3, String[] param4) {
        if(!("Guest".equals(user))) {
            try {
                socket.send("USER_STOP_SERVING");
                Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio
                socket.send(sessionID);
                Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Errore nella connessione. Continuerai come Ospite...", Toast.LENGTH_SHORT).show());
                sessionID = "-1";
                user = "Guest";
            }
        }

        Intent intent = new Intent(Activity5_Serving.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        intent.putExtra("param3", param3);
        intent.putExtra("param4", param4);

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
    public void onClickWait(View v) {
        v.setClickable(false);
        resetInactivityTimer(); // Aggiungi questa linea per reimpostare il timer
        navigateToParam(Activity6_Attesa.class, sessionID, user, selectedDrink,null);
    }
    public void onClickQuiz(View v) {
        v.setClickable(false);
        resetInactivityTimer();
        if("Guest".equals(user)){
            String[] allTopics = {"Storia", "Attualità", "Sport", "Scienza", "Informatica", "Letteratura", "Musica", "Geografia"};
            selectedTopics = new String[2];
            Random random = new Random();

            for (int i = 0; i < 2; i++) {
                int randomIndex = random.nextInt(allTopics.length);
                selectedTopics[i] = allTopics[randomIndex];
            }
            navigateToParam(Activity6_Chat.class, sessionID, user, selectedDrink, selectedTopics);

        }else {
            new Thread(() -> {
                try {
                    socket.send("START_CHAT");
                    Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio
                    socket.send(sessionID);
                    Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio
                    String inputString = socket.receive();
                    Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio
                    selectedTopics = inputString.split(",");

                    if (selectedTopics.length < 2) {
                        String[] allTopics = {"Storia", "Attualità", "Sport", "Scienza", "Informatica", "Letteratura", "Musica", "Geografia"};
                        Random random = new Random();

                        String newTopic;

                        do {
                            newTopic = allTopics[random.nextInt(allTopics.length)];
                        } while (Arrays.asList(selectedTopics).contains(newTopic));

                        String[] newSelectedTopics = Arrays.copyOf(selectedTopics, selectedTopics.length + 1);
                        newSelectedTopics[newSelectedTopics.length - 1] = newTopic;

                        selectedTopics = newSelectedTopics;
                    }else if (selectedTopics.length > 2) {

                        int elementsToRemove = selectedTopics.length - 2;
                        Random random = new Random();
                        for (int i = 0; i < elementsToRemove; i++) {
                            int randomIndex = random.nextInt(selectedTopics.length);
                            String[] newArray = new String[selectedTopics.length - 1];
                            int newArrayIndex = 0;
                            for (int j = 0; j < selectedTopics.length; j++) {
                                if (j != randomIndex) {
                                    newArray[newArrayIndex] = selectedTopics[j];
                                    newArrayIndex++;
                                }
                            }
                            selectedTopics = newArray;
                        }
                    }else if("Nessuno".equals(selectedTopics[0])){
                        String[] allTopics = {"Storia", "Attualità", "Sport", "Scienza", "Informatica", "Letteratura", "Musica", "Geografia"};

                        selectedTopics = new String[2];
                        Random random = new Random();

                        for (int i = 0; i < 2; i++) {
                            int randomIndex = random.nextInt(allTopics.length);
                            selectedTopics[i] = allTopics[randomIndex];
                        }
                    }

                    navigateToParam(Activity6_Chat.class, sessionID, user, selectedDrink, selectedTopics);

                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Errore nella connessione. Continuerai come Ospite...", Toast.LENGTH_SHORT).show());
                    sessionID = "-1";
                    user = "Guest";

                    String[] allTopics = {"Storia", "Attualità", "Sport", "Scienza", "Informatica", "Letteratura", "Musica", "Geografia"};

                    selectedTopics = new String[2];
                    Random random = new Random();

                    for (int i = 0; i < 2; i++) {
                        int randomIndex = random.nextInt(allTopics.length);
                        selectedTopics[i] = allTopics[randomIndex];
                    }
                    navigateToParam(Activity6_Chat.class, sessionID, user, selectedDrink, selectedTopics);
                }
            }).start();

        }
    }
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            sessionID = intent.getStringExtra("param1");
            user = intent.getStringExtra("param2");
            selectedDrink = intent.getStringExtra("param3");

            int atIndex = user.indexOf("@");

            if (atIndex != -1) {
                String username = user.substring(0, atIndex);
                runOnUiThread(() -> textViewLoggedIn.setText(username));
            } else {
                runOnUiThread(() -> textViewLoggedIn.setText(user));
            }
        }
    }
    private void setUpComponent() {
        runOnUiThread(() -> {

            textViewOrderStatusTitle.setText("Ordine registrato: \n" + selectedDrink);

            if (selectedDrink != "") {
                textViewOrderStatusMessage.setText("Puoi attendere in sala d'attesa o intrattenerti rispondendo ai quiz.");
            }
        });
    }
}