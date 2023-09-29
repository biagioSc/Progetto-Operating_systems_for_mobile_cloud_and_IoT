package com.example.robotinteractionOttimizzata;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;

public class Activity1_New extends AppCompatActivity {

    // Dichiarazioni delle variabili per gli elementi dell'interfaccia
    private static final long TIME_THRESHOLD = 20000; // Soglia di inattività di 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private EditText editTextEmail, editTextPassword;
    private TextInputLayout emailTextInputLayout, passwordTextInputLayout;
    private Button buttonLogin;
    private TextView textViewError, textViewSignUp;
    private Animation buttonAnimation;
    private SocketManager socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_1new);

        // Tentativo di connessione continua
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Log.d("Activity1_New", "[CONNECTION] Tentativo di connessione...");

                        // Crea una nuova istanza di SocketManager e tenta la connessione.
                        socket = SocketManager.getInstance();
                        socket.attemptConnection();

                        if (socket.isConnected()) {
                            Log.d("Activity1_New", "[CONNECTION] Connessione stabilita");
                            break;
                        } else {
                            throw new IOException();
                        }

                    } catch (Exception e) {
                        Log.d("Activity1_New", "[CONNECTION] Connessione fallita");

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                }
            }
        }).start();

        // Collegamenti degli elementi dell'interfaccia alle variabili
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        emailTextInputLayout = findViewById(R.id.emailTextInputLayout);
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewError = findViewById(R.id.textViewError);
        textViewSignUp = findViewById(R.id.buttonSignUp);

        // Carica l'animazione del pulsante dal file XML
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);

        // Imposta un timer di inattività per passare all'Activity "OutOfSight"
        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Activity1_New.this, Activity0_OutOfSight.class);
                startActivity(intent);
            }
        };

        startInactivityTimer();

        // Aggiungi listener per nascondere il messaggio di errore quando l'utente modifica gli EditText
        editTextEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    textViewError.setVisibility(View.INVISIBLE);
                }
            }
        });

        editTextPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    textViewError.setVisibility(View.INVISIBLE);
                }
            }
        });

        textViewSignUp.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        SpannableString spannableString = new SpannableString((CharSequence) textViewSignUp);
                        spannableString.setSpan(new UnderlineSpan(), 0, textViewSignUp.length(), 0);
                        textViewSignUp.setText(spannableString);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        textViewSignUp.setText(textViewSignUp.getText().toString()); // Resetta il testo senza sottolineatura
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

    public void onClickSignUp(View v) {
        // Passa all'Activity di registrazione
        Intent intent = new Intent(Activity1_New.this, Activity9_Signup.class);
        startActivity(intent);
    }

    public void onClickAccedi(View v) {
        // Avvia l'animazione del pulsante
        buttonLogin.startAnimation(buttonAnimation);

        // Ottieni le credenziali inserite dall'utente
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Verifica la validità delle credenziali
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            textViewError.setText("Inserisci email e password");
            textViewError.setVisibility(View.VISIBLE);
        } else if (!isValidLogin(email, password)) {
            textViewError.setText("Email e/o password non valide");
            textViewError.setVisibility(View.VISIBLE);
        } else {
            // Se le credenziali sono valide, avvia la comunicazione con il server
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Inizia la procedura di login con il server
                        socket.sendMessage("LOG_IN");
                        socket.sendMessage(email);
                        socket.sendMessage(password);

                        // Ricevi la risposta dal server
                        String response = socket.receiveMessage();
                        if (response.equalsIgnoreCase("LOG_IN_SUCCESS")) {
                            String sessionID = socket.receiveMessage();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(Activity1_New.this, Activity2_Welcome.class);

                                    // Passaggio del sessionID
                                    intent.putExtra("SESSION_ID", sessionID);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            // Gestisci l'errore di login
                            Log.d("Activity1_New", "Email o password non corrette!");
                        }
                    } catch (IOException e) {
                        Log.d("Activity1_New", "Errore durante la comunicazione con il server.");
                    }
                }
            }).start();
        }
    }

    // Metodo per verificare la validità del login (momentaneamente sempre vero)
    private boolean isValidLogin(String email, String password) {
        return true;
    }

    private void startInactivityTimer() {
        handler.postDelayed(runnable, TIME_THRESHOLD);
    }

    private void resetInactivityTimer() {
        handler.removeCallbacks(runnable);
        startInactivityTimer();
    }
}
