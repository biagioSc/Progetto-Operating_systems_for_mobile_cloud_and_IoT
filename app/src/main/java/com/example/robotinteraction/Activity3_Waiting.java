package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class Activity3_Waiting extends AppCompatActivity {
    private int queueCount = 0;
    private int queueTime = 0;
    private TextView textViewQueueCount;
    private TextView textViewWaitTime;
    private ProgressBar progressBarWaiting;
    private Runnable runnable;
    private TextView textViewLoggedIn;
    private String sessionID = "-1", user = "Guest";

    private Activity_SocketManager socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3waiting);

        connection();
        initUIComponents();

        receiveParam();
        setUpComponent();
    }

    private void connection() {
        socket = Activity_SocketManager.getInstance();
    }
    private void initUIComponents() {
        textViewQueueCount = findViewById(R.id.textViewQueueCount);
        textViewWaitTime = findViewById(R.id.textViewWaitTime);
        progressBarWaiting = findViewById(R.id.progressBarWaiting);
        textViewLoggedIn = findViewById(R.id.textViewLoggedIn);
    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2) {
        Intent intent = new Intent(Activity3_Waiting.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        startActivity(intent);
        finish();
    }
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            sessionID = intent.getStringExtra("param1");
            user = intent.getStringExtra("param2");
            queueCount = intent.getIntExtra("param3", 0);

            if(!("Guest".equals(user))) {
                try {
                    socket.send("ADD_USER_WAITING");
                    socket.send(sessionID);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

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

        textViewQueueCount.setText("Persone in coda: " + queueCount);
        queueTime = queueCount * 10; // Calculate queue time in seconds
        textViewWaitTime.setText("Tempo di attesa: " + queueTime + " secondi");
        progressBarWaiting.setMax(queueTime);

        new CountDownTimer(queueTime * 1000L, 1000) {
            private int secondCounter = 0; // Variabile per il conteggio dei secondi

            @Override
            public void onTick(long millisUntilFinished) {

                progressBarWaiting.setProgress((int) (queueTime - millisUntilFinished / 1000));
                textViewWaitTime.setText("Tempo di attesa: " + (int) (millisUntilFinished / 1000) + " secondi");
                secondCounter++;

                if (secondCounter >= 10) {
                    if(!("Guest".equals(user))) {
                        new Thread(() -> {
                            //socket.send("UPDATE_QUEUE");
                            //final String receivedData = socket.receive();
                            String receivedData = "0";
                            if (Integer.parseInt(receivedData) != 0) {
                                queueCount = Integer.parseInt(receivedData);
                            }
                        }).start();
                    }
                    secondCounter = 0;
                    queueCount--;
                    textViewQueueCount.setText("Persone in coda: " + queueCount);
                }
            }

            @Override
            public void onFinish() {

                navigateToParam(Activity4_Ordering.class, sessionID, user);
            }
        }.start();
    }

}
