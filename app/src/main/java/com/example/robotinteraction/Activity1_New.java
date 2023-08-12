package com.example.robotinteraction;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;

public class Activity1_New extends AppCompatActivity {

    private static final long TIME_THRESHOLD = 20000; // 20 secondi
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

        //[SERVER] COLLEGAMENTO AL SERVER, SOCKET

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_1new);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        emailTextInputLayout = findViewById(R.id.emailTextInputLayout);
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewError = findViewById(R.id.textViewError);
        textViewSignUp = findViewById(R.id.buttonSignUp);

        // Carica l'animazione dal file XML
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Activity1_New.this, Activity0_OutOfSight.class);
                startActivity(intent);
            }
        };

        startInactivityTimer();

        // Aggiungi un listener per nascondere il messaggio di errore quando l'utente inizia a modificare gli EditText
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
        // Vai alla schermata di registrazione
        Intent intent = new Intent(Activity1_New.this, Activity9_Signup.class);
        startActivity(intent);
    }

    public void onClickAccedi(View v) {
        // Avvia l'animazione
        buttonLogin.startAnimation(buttonAnimation);

        // Ottieni il testo inserito negli EditText
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Esegui la validazione e il controllo
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            textViewError.setText("Inserisci email e password");
            textViewError.setVisibility(View.VISIBLE);
        } else if (!isValidLogin(email, password)) {
            textViewError.setText("Email e/o password non valide");
            textViewError.setVisibility(View.VISIBLE);
        } else {

            // Il formato dei dati è corretto e procedo con la comunicazione col server
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        // Invio messaggio di inizio LOG_IN
                        socket.sendMessage("LOG_IN");

                        // Invio email e password
                        socket.sendMessage(email);
                        socket.sendMessage(password);

                        // Inizializzo la risposta e il sessionID dell'utente
                        String response = null;
                        String sessionID = null;

                        // Ricevo risposta sull'avvenuto o meno LOGIN
                        response = socket.receiveMessage();

                        // Se il login è andato a buon fine, ricevo il sessionID
                        if(response.equalsIgnoreCase("LOG_IN_SUCCESS")){
                            // Ricevo id della sessione attiva dell'utente
                            sessionID = socket.receiveMessage();
                            //PORTARE QUESTO ID NELLE SUCCESSIVE INTENT FINO
                            //ALLA CHIUSURA DELL'APP

                        // Se ricevo Log-in-error il login non è andato a buon fine
                        }else if(response.equalsIgnoreCase("LOG_IN_ERROR")){
                            //Mostrare messaggio di errore: password o email non corretta
                            Log.d("Activity1_New","Email o password non corrette!");

                        // Vuol dire che la response è null
                        }else{

                            // Mostrare messaggio di errore di tipo server
                            Log.d("Activity1_New","Il server ha risposto con Null dopo " +
                                    "l'invio di email e password!");
                        }

                    }catch (IOException e){
                        Log.d("Activity1_New","Si è verificata una eccezione nell'invio dei" +
                                "dati login";
                    }
                }
            }).start();

            // [AGGIUNGERE] PRIMA DI SPOSTARSI PORTARE CON SE L'ID DELLA SESSIONE "sessionID"
            // Vai alla schermata di benvenuto
            textViewError.setVisibility(View.INVISIBLE);  // Nascondi il messaggio di errore se necessario
            Intent intent = new Intent(Activity1_New.this, Activity2_Welcome.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        }
    }

    // Metodo per verificare la validità del login
    private boolean isValidLogin(String email, String password) {
        // [SERVER] COLLEGAMENTO SERVER
        // Implementa la logica per la validazione dell'email
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
