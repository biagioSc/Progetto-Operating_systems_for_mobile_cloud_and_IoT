package com.example.robotinteraction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class Activity5_Serving extends AppCompatActivity {
    private Animation buttonAnimation;
    private Button buttonQuiz, buttonWaitingRoom;
    private String selectedDrink;
    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private SocketManager socket;  // Manager del socket per la comunicazione con il server

    private String sessionID = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_5serving);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Log.d("Activity4_Ordering", "[CONNECTION] Tentativo di connessione...");

                        // Crea una nuova istanza di SocketManager e tenta la connessione.
                        socket = SocketManager.getInstance();
                        socket.attemptConnection();

                        if (socket.isConnected()) {
                            Log.d("Activity4_Ordering", "[CONNECTION] Connessione stabilita");
                            break;
                        } else {
                            throw new IOException();
                        }

                    } catch (Exception e) {
                        Log.d("Activity4_Ordering", "[] Connessione fallita");

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                }
            }
        }).start();

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
        TextView textViewOrderStatusMessage = findViewById(R.id.textViewOrderStatusMessage);
        textViewOrderStatusTitle.setText("In preparazione: \n" + selectedDrink);

        if(selectedDrink!="") {
            textViewOrderStatusMessage.setText("Il tuo " + selectedDrink + " è attualmente in preparazione. Puoi attendere in sala d'attesa o intrattenerti rispondendo ai quiz."
            );
        }
        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Activity5_Serving.this, Activity0_OutOfSight.class);
                startActivity(intent);
            }
        };

        startInactivityTimer();

        buttonWaitingRoom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Applica l'animazione di scala quando il bottone viene premuto
                        v.startAnimation(buttonAnimation);

                        // Avvia un Handler per ripristinare le dimensioni dopo un secondo
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Ripristina le dimensioni originali
                                v.clearAnimation();
                            }
                        }, 200); // 1000 millisecondi = 1 secondo
                        break;
                }
                return false;
            }
        });

        buttonQuiz.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Applica l'animazione di scala quando il bottone viene premuto
                        v.startAnimation(buttonAnimation);

                        // Avvia un Handler per ripristinare le dimensioni dopo un secondo
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // Ripristina le dimensioni originali
                                v.clearAnimation();
                            }
                        }, 200); // 1000 millisecondi = 1 secondo
                        break;
                }
                return false;
            }
        });
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
        openWaitingActivity(selectedDrink);
    }
    public void onClickQuiz(View v) {
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
