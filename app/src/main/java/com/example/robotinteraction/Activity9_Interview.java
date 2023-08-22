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
import android.widget.TextView;
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
    private Activity_SocketManager socket;  // Manager del socket per la comunicazione con il server

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_9interview);

        connection();
        initUIComponents();
        setupListeners();

        receiveParam();
    }
    private void connection() {
        socket = Activity_SocketManager.getInstance(); // Ottieni l'istanza del gestore del socket
        runnable = () -> navigateTo(Activity0_OutOfSight.class);
    }
    private void initUIComponents() {
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
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

    }
    private void setupListeners() {
        setTouchListenerForAnimation(buttonSubmit);
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
        new Handler().postDelayed(() -> v.clearAnimation(), 200);
    }
    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(Activity9_Interview.this, targetActivity);
        startActivity(intent);
    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2, String param3) {
        Intent intent = new Intent(Activity9_Interview.this, targetActivity);
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
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {

            nome = intent.getStringExtra("param1");
            cognome = intent.getStringExtra("param2");
            email = intent.getStringExtra("param3");
            password = intent.getStringExtra("param4");
        }

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
        startInactivityTimer();
    }
    public void onClickRegister(View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.send("SIGN_UP");
                    socket.send(email);
                    socket.send(nome);
                    socket.send(cognome);
                    socket.send(password);
                    int numeropreferezeInt=drinkSelections.size();
                    String numeroprefrenzeString=Integer.toString(numeropreferezeInt);
                    socket.send(numeroprefrenzeString);
                    for(String drink : drinkSelections){
                        socket.send(drink);
                    }
                    numeropreferezeInt=argSelections.size();
                    numeroprefrenzeString=Integer.toString(numeropreferezeInt);
                    socket.send(numeroprefrenzeString);
                    for(String topic : argSelections){
                        socket.send(topic);
                    }

                    String response = socket.receive();
                    if(response != null){
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
                                    navigateToParam(Activity1_New.class, nome, email, password);
                                    Toast.makeText(Activity9_Interview.this,
                                            "Registrazione completata", Toast.LENGTH_LONG).show();

                                }
                            });
                        }
                    }
                }catch (Exception e){
                    Toast.makeText(Activity9_Interview.this,"Registrazione fallita", Toast.LENGTH_SHORT).show();

                    navigateToParam(Activity1_New.class, "ERROR", null, null);
                }
            }
        }).start();
        //Toast.makeText(Activity9_Interview.this, "Registrazione avvenuta con successo!", Toast.LENGTH_SHORT).show();
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
