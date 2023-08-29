package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Activity3_Waiting extends AppCompatActivity {
    private int queueCount = 0;
    private int queueTime = 0;
    private TextView textViewQueueCount;
    private TextView textViewWaitTime;
    private ProgressBar progressBarWaiting;
    private Runnable runnable;
    private TextView textViewLoggedIn;
    private String sessionID = "-1", user = "Guest", inputString, recommendedDrink;

    private Socket_Manager socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3waiting);
        getWindow().setWindowAnimations(0);

        connection();
        initUIComponents();
        receiveParam();
        setUpComponent();
    }

    private void connection() {
        socket = Socket_Manager.getInstance();
    }
    private void initUIComponents() {
        textViewQueueCount = findViewById(R.id.textViewQueueCount);
        textViewWaitTime = findViewById(R.id.textViewWaitTime);
        progressBarWaiting = findViewById(R.id.progressBarWaiting);
        textViewLoggedIn = findViewById(R.id.textViewLoggedIn);
    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2, String param4, String param5) {
        Intent intent = new Intent(Activity3_Waiting.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        intent.putExtra("param4", param4);
        intent.putExtra("param5", param5);
        startActivity(intent);
        finish();
    }
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            sessionID = intent.getStringExtra("param1");
            user = intent.getStringExtra("param2");
            queueCount = intent.getIntExtra("param3", 0);
            inputString = intent.getStringExtra("param4");
            recommendedDrink = intent.getStringExtra("param5");

            if(!("Guest".equals(user))) {
                try {
                    socket.send("ADD_USER_WAITING");
                    Thread.sleep(500);
                    socket.send(sessionID);
                    Thread.sleep(500);
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Errore nella connessione. Continuerai come Ospite...", Toast.LENGTH_SHORT).show());
                    sessionID = "-1";
                    user = "Guest";
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

        if ("Guest".equals(user)) {
            queueTime = queueCount * 10 + 10; // Calculate queue time in seconds
            textViewWaitTime.setText("Tempo di attesa: " + queueTime + " secondi");
            progressBarWaiting.setMax(queueTime);
            new CountDownTimer(queueTime * 1000, 1000) {
                private int secondCounter = 0;
                @Override
                public void onTick(long millisUntilFinished) {
                    runOnUiThread(() -> {
                        progressBarWaiting.setProgress((int) (queueTime - millisUntilFinished / 1000));
                        textViewWaitTime.setText("Tempo di attesa: " + (int) (millisUntilFinished / 1000) + " secondi");
                    });
                    secondCounter++;

                    if (secondCounter >= 10) {
                        secondCounter = 0;
                        queueCount--;
                        if(queueCount>-1) runOnUiThread(() -> textViewQueueCount.setText("Persone in coda: " + queueCount));
                    }
                }
                @Override
                public void onFinish() {
                    showPopupMessage();
                    new Handler().postDelayed(() -> {
                        navigateToParam(Activity4_Ordering.class, sessionID, user, inputString, recommendedDrink);
                    }, 3000);
                }
            }.start();
        } else {
            new Thread(() -> {
                try {
                    while (true) {
                        socket.send("UPDATE_QUEUE");
                        Thread.sleep(500);
                        String receivedData = socket.receive();
                        Thread.sleep(500);

                        queueCount = Integer.parseInt(receivedData);

                        if (queueCount > 0) {
                            runOnUiThread(() -> {
                                textViewWaitTime.setText("");
                                textViewQueueCount.setText("Persone in coda: " + queueCount);
                            });
                        } else {
                            navigateToParam(Activity4_Ordering.class, sessionID, user, inputString, recommendedDrink);
                            break; // Esci dal ciclo se la coda è vuota
                        }
                    }
                } catch (InterruptedException e) {
                    runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Errore nella connessione. Continuerai come Ospite...", Toast.LENGTH_SHORT).show());
                    sessionID = "-1";
                    user = "Guest";
                    queueCount=0;
                    runOnUiThread(() -> textViewQueueCount.setText("Persone in coda: " + queueCount));
                    showPopupMessage();
                    new Handler().postDelayed(() -> {
                        navigateToParam(Activity4_Ordering.class, sessionID, user, inputString, recommendedDrink);
                    }, 3000);
                }
            }).start();
        }
    }
    public void showPopupMessage() {
        runOnUiThread(() -> {
            LayoutInflater inflater = LayoutInflater.from(Activity3_Waiting.this);
            View customView = inflater.inflate(R.layout.activity_00popupwelcome, null);

            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(Activity3_Waiting.this);
            builder.setCustomTitle(customView)
                    .setCancelable(false);

            android.app.AlertDialog dialog = builder.create();
            dialog.show();

            // Chiudi il popup dopo 5 secondi
            new Handler().postDelayed(() -> {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }, 3000); // 5000 millisecondi = 5 secondi
        });
    }

}
