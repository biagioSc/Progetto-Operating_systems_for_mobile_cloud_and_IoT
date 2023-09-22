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
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Activity9_Interview extends AppCompatActivity {

    // [SERVER] DECIDERE SE I CHECHBOX VOGLIAMO RIEMPIRLI DINAMICAMENTE

    private CheckBox[] drinkCheckboxes;
    private CheckBox[] argCheckboxes;
    private Button buttonSubmit;
    private String nome;
    private String cognome;
    private String email;
    private String password;
    private List<String> drinkSelections = new ArrayList<>();
    private List<String> argSelections = new ArrayList<>();
    private Animation buttonAnimation;
    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private SocketManager socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_9interview);

        Intent intent = getIntent();
        nome = intent.getStringExtra("nome");
        cognome = intent.getStringExtra("cognome");
        email = intent.getStringExtra("email");
        password = intent.getStringExtra("password");

        Log.d("Activity9_Interview",nome + " " + cognome + " " + email + " " + password);
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        socket = SocketManager.getInstance();
                        socket.attemptConnection();

                        if(socket.isConnected()){
                            break;
                        }else{
                            throw new IOException();
                        }
                    }catch (Exception e){
                        e.printStackTrace();

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }

                }
            }
        }).start();

        drinkCheckboxes = new CheckBox[]{
                findViewById(R.id.checkBoxDrink1),
                findViewById(R.id.checkBoxDrink2),
                findViewById(R.id.checkBoxDrink3),
                findViewById(R.id.checkBoxDrink4),
                findViewById(R.id.checkBoxDrink5),
                findViewById(R.id.checkBoxDrink6),
                findViewById(R.id.checkBoxDrink7),
                findViewById(R.id.checkBoxDrink8)
        };

        argCheckboxes = new CheckBox[]{
                findViewById(R.id.checkBoxArg1),
                findViewById(R.id.checkBoxArg2),
                findViewById(R.id.checkBoxArg3),
                findViewById(R.id.checkBoxArg4),
                findViewById(R.id.checkBoxArg5),
                findViewById(R.id.checkBoxArg6),
                findViewById(R.id.checkBoxArg7),
                findViewById(R.id.checkBoxArg8)
        };

        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonSubmit.setEnabled(false);

        for (CheckBox checkBox : drinkCheckboxes) {
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateDrinkSelections();
                    updateSubmitButtonState();
                }
            });
        }

        for (CheckBox checkBox : argCheckboxes) {
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateArgSelections();
                    updateSubmitButtonState();

                    if (checkBox.getId() == R.id.checkBoxArg8 && checkBox.isChecked()) {
                        for (CheckBox argCheckBox : argCheckboxes) {
                            if (argCheckBox != checkBox) {
                                argCheckBox.setChecked(false);
                            }
                        }
                        updateArgSelections();
                        updateSubmitButtonState();
                    }
                }
            });
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Activity9_Interview.this, Activity0_OutOfSight.class);
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

    public void onClickRegister(View v) {
        //[SERVER] mandare dati al server
        buttonSubmit.startAnimation(buttonAnimation);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.sendMessage("SIGN_UP");
                    socket.sendMessage(email);
                    socket.sendMessage(nome);

                    socket.sendMessage(cognome);
                    socket.sendMessage(password);

                    int numeropreferezeInt=drinkSelections.size();
                    String numeroprefrenzeString=Integer.toString(numeropreferezeInt);
                    socket.sendMessage(numeroprefrenzeString);

                    for(String drink : drinkSelections){
                        socket.sendMessage(drink);

                    }
                    numeropreferezeInt=argSelections.size();
                    numeroprefrenzeString=Integer.toString(numeropreferezeInt);
                    socket.sendMessage(numeroprefrenzeString);

                    for(String topic : argSelections){

                        socket.sendMessage(topic);

                    }


                }catch (IOException e){
                    e.printStackTrace();
                }

                String response = null;

                response = socket.receiveMessage();


                if(response != null)
                {
                    String finalResponse = response;

                    if(finalResponse.equalsIgnoreCase(("SIGN_UP_ERROR"))){

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Activity9_Interview.this,"Registrazione fallita",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                    else if(finalResponse.equalsIgnoreCase("SIGN_UP_SUCCESS")){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(Activity9_Interview.this,
                                        "Registrazione completata",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(Activity9_Interview.this,
                                        Activity1_New.class);
                                startActivity(intent);
                                finish();
                            }
                        });

                    }

                }else{
                    Log.d("InterviewActivity","Il server ha inviato msg = null " +
                            "come risposta alla signup");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(Activity9_Interview.this, "Si è verificato un errore" +
                                    " lato Server", Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            }
        }).start();

        Intent newIntent = new Intent(Activity9_Interview.this, Activity1_New.class);
        startActivity(newIntent);
    }

    private void updateDrinkSelections() {
        drinkSelections.clear();
        for (CheckBox checkBox : drinkCheckboxes) {
            if (checkBox.isChecked()) {
                drinkSelections.add(checkBox.getText().toString());
            }
        }
    }

    private void updateArgSelections() {
        argSelections.clear();
        for (CheckBox checkBox : argCheckboxes) {
            if (checkBox.isChecked()) {
                argSelections.add(checkBox.getText().toString());
            }
        }
    }

    private void updateSubmitButtonState() {
        boolean isDrinkSelected = !drinkSelections.isEmpty();
        boolean isArgSelected = !argSelections.isEmpty() || argCheckboxes[7].isChecked();

        buttonSubmit.setEnabled(isDrinkSelected && isArgSelected);

        if (argCheckboxes[7].isChecked()) {
            for (CheckBox argCheckBox : argCheckboxes) {
                if (argCheckBox != argCheckboxes[7]) {
                    argCheckBox.setChecked(false);
                }
            }
            updateArgSelections();
        }
    }
}
