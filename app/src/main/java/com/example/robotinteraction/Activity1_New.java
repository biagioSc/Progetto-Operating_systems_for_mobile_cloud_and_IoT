package com.example.robotinteraction;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
    private EditText editTextEmail, editTextPassword;
    private TextInputLayout emailTextInputLayout, passwordTextInputLayout;
    private Button buttonLogin;
    private TextView textViewError, textViewSignUp;

    private Animation buttonAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

    public void onClickSignUp(View v) {
        // Vai alla schermata di registrazione
        Intent intent = new Intent(Activity1_New.this, Activity9_Signup.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
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
        } else if (!isValidEmail(email) || !isValidPassword(password)) {
            textViewError.setText("Email e/o password non valide");
            textViewError.setVisibility(View.VISIBLE);
        } else {
            // Vai alla schermata di benvenuto
            Intent intent = new Intent(Activity1_New.this, Activity2_Welcome.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            //finish();
            textViewError.setVisibility(View.INVISIBLE);  // Nascondi il messaggio di errore se necessario
        }
    }

    // Metodo per verificare la validità dell'email
    private boolean isValidEmail(String email) {
        // Implementa la logica per la validazione dell'email
        return true;
    }

    // Metodo per verificare la validità della password
    private boolean isValidPassword(String password) {
        // Implementa la logica per la validazione della password
        return true;
    }

}
