package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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

        // Inizializza gli elementi UI
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegisterContinue = findViewById(R.id.buttonRegisterContinue);
        textViewError = findViewById(R.id.textViewError2);
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);

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

        buttonRegisterContinue.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                resetInactivityTimer(); // Aggiungi questa linea per reimpostare il timer
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

    // Imposta un listener per il pulsante di registrazione

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

        Log.d("Activity9_Signup",nomeValido + " " + cognomeValido + " " + emailValido + " " + passwordValida);

        // [SERVER] CONTROLLARE DATI

        // Effettua il controllo sui campi obbligatori
        if (nomeValido==false || cognomeValido ==false || emailValido ==false || passwordValida==false) {
            textViewError.setText("Email e/o password errate");
            textViewError.setVisibility(View.VISIBLE);
        }
        else{
            textViewError.setVisibility(View.INVISIBLE);
            buttonRegisterContinue.setEnabled(nomeValido && cognomeValido && emailValido && passwordValida);
            openInterviewActivity(firstName, lastName, email, password);
        }

    }

    // Metodo per aprire l'activity "Interview"
    private void openInterviewActivity(String firstName, String lastName, String email, String password) {
        Intent intent = new Intent(this, Activity9_Interview.class);
        intent.putExtra("nome", firstName);
        intent.putExtra("cognome", lastName);
        intent.putExtra("email", email);
        intent.putExtra("password", password);
        startActivity(intent);
    }

    private void updateButtonState() {
        // Verifica se tutti gli EditText non sono vuoti
        boolean allFieldsNotEmpty = !editTextFirstName.getText().toString().isEmpty() &&
                !editTextLastName.getText().toString().isEmpty() &&
                !editTextEmail.getText().toString().isEmpty() &&
                !editTextPassword.getText().toString().isEmpty();

        // Abilita o disabilita il bottone in base allo stato dei campi EditText
        buttonRegisterContinue.setEnabled(allFieldsNotEmpty);
    }
}
