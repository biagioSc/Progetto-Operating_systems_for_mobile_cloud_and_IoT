package com.example.robotinteraction;

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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Activity9_Signup extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword;
    private Button buttonRegisterContinue;
    private TextView textViewError;
    private Animation buttonAnimation;
    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_9signup);

        connection();
        initUIComponents();
        setupListeners();

        setUpComponent();
    }
    private void connection() {
        runnable = new Runnable() { // Azione da eseguire dopo l'inattività
            @Override
            public void run() {

                navigateTo(Activity0_OutOfSight.class);
            }
        };
    }
    private void initUIComponents() {
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegisterContinue = findViewById(R.id.buttonRegisterContinue);
        textViewError = findViewById(R.id.textViewError2);
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
    }
    private void setupListeners() {
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
        new Handler().postDelayed(() -> v.clearAnimation(), 200);
    }
    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(Activity9_Signup.this, targetActivity);
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
        // Ottieni il testo dai campi di input

        editTextFirstName.addTextChangedListener(textWatcher);
        editTextLastName.addTextChangedListener(textWatcher);
        editTextEmail.addTextChangedListener(textWatcher);
        editTextPassword.addTextChangedListener(textWatcher);

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Activity9_Signup.this, Activity0_OutOfSight.class);
                startActivity(intent);
            }
        };
        startInactivityTimer();
    }
    public void onClickRContinue(View view) {
        // Ottieni il testo dai campi di input
        String firstName = editTextFirstName.getText().toString().trim();
        String lastName = editTextLastName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        boolean nomeValido = !firstName.isEmpty() && !firstName.matches(".*\\d.*");
        boolean cognomeValido = !lastName.isEmpty() && !lastName.matches(".*\\d.*");
        boolean emailValido = !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
        boolean passwordValida = password.length() >= 6;

        if (nomeValido==false || cognomeValido ==false || emailValido ==false || passwordValida==false) {
            textViewError.setText("Email e/o password errate");
            textViewError.setVisibility(View.VISIBLE);
        }
        else{
            textViewError.setVisibility(View.INVISIBLE);
            buttonRegisterContinue.setEnabled(nomeValido && cognomeValido && emailValido && passwordValida);
            navigateToParam(Activity9_Interview.class, firstName, lastName, email, password);
        }

    }

    private void updateButtonState() {
        // Verifica se tutti gli EditText non sono vuoti
        boolean allFieldsNotEmpty = !editTextFirstName.getText().toString().isEmpty() &&
                !editTextLastName.getText().toString().isEmpty() &&
                !editTextEmail.getText().toString().isEmpty() &&
                !editTextPassword.getText().toString().isEmpty();

        buttonRegisterContinue.setEnabled(allFieldsNotEmpty);


    }
}
