package com.example.robotinteractionOttimizzata;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Activity9_Signup extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword;
    private Button buttonRegisterContinue;
    private TextView textViewError;
    private Animation buttonAnimation;
    private static final long TIME_THRESHOLD = 60000; // 20 secondi
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private String sessionID = "-1", user = "Guest";
    private ImageButton exitButton;
    private Socket_Manager socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_9signup);
        getWindow().setWindowAnimations(0);

        connection();
        initUIComponents();
        setupListeners();

        setUpComponent();
    }
    private void connection() {
        socket = Socket_Manager.getInstance(); // Ottieni l'istanza del gestore del socket
        runnable = () -> navigateTo(Activity0_OutOfSight.class, "0", null);
    }
    private void initUIComponents() {
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegisterContinue = findViewById(R.id.buttonRegisterContinue);
        textViewError = findViewById(R.id.textViewError2);
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        exitButton = findViewById(R.id.exitToggle);
    }
    private void setupListeners() {
        setTouchListenerForAnimation(exitButton);
        setTouchListenerForAnimation(buttonRegisterContinue);
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
        Intent intent = new Intent(Activity9_Signup.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        startActivity(intent);
    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2, String param3, String param4) {
        Intent intent = new Intent(Activity9_Signup.this, targetActivity);
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
    private void setUpComponent() {
        buttonRegisterContinue.setEnabled(false);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Non necessario per questo caso
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Chiamato quando il testo negli EditText cambia
                updateButtonState();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Non necessario per questo caso
            }
        };

        editTextFirstName.addTextChangedListener(textWatcher);
        editTextLastName.addTextChangedListener(textWatcher);
        editTextEmail.addTextChangedListener(textWatcher);
        editTextPassword.addTextChangedListener(textWatcher);

        startInactivityTimer();
    }
    public void onClickRContinue(View v) {
        v.setClickable(false);
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        boolean nomeValido = !firstName.isEmpty() && !firstName.matches(".*\\d.*");
        boolean cognomeValido = !lastName.isEmpty() && !lastName.matches(".*\\d.*");
        boolean emailValido = !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
        boolean passwordValida = password.length() >= 6;

        if (nomeValido==false || cognomeValido ==false || emailValido ==false || passwordValida==false) {
            textViewError.setText("Email e/o password non conformi");
            textViewError.setVisibility(View.VISIBLE);
            v.setClickable(true);
        }
        else{
            textViewError.setVisibility(View.INVISIBLE);
            buttonRegisterContinue.setEnabled(nomeValido && cognomeValido && emailValido && passwordValida);
            navigateToParam(Activity9_Interview.class, firstName, lastName, email, password);
        }
    }

    private void updateButtonState() {
        boolean allFieldsNotEmpty = !editTextFirstName.getText().toString().isEmpty() &&
                !editTextLastName.getText().toString().isEmpty() &&
                !editTextEmail.getText().toString().isEmpty() &&
                !editTextPassword.getText().toString().isEmpty();

        buttonRegisterContinue.setEnabled(allFieldsNotEmpty);
    }
    public void ExitSignup(View v) {

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
