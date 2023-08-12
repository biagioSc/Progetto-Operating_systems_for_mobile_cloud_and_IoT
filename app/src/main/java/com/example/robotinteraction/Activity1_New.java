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

public class Activity1_New extends AppCompatActivity {

    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private EditText editTextEmail, editTextPassword;
    private TextInputLayout emailTextInputLayout, passwordTextInputLayout;
    private Button buttonLogin;
    private TextView textViewError, textViewSignUp;
    private Animation buttonAnimation;

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
