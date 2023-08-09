package com.example.robotinteraction;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
public class A1_Main extends AppCompatActivity {

    // Views
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_1main);

        // Inizializza le viste
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewSignUp = findViewById(R.id.buttonSignUp);

        // Imposta l'azione del pulsante di login
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLoginClick();
            }
        });

        // Imposta l'azione del testo di registrazione
        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSignUpClick();
            }
        });
    }

    // Metodo richiamato quando il pulsante di login viene cliccato
    private void onLoginClick() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            // Uno o entrambi i campi sono vuoti, mostra un messaggio di errore
            Toast.makeText(A1_Main.this, "Inserisci email e password", Toast.LENGTH_SHORT).show();
        } else {
            // Effettua qui la logica per controllare l'email e la password inserite
            Toast.makeText(A1_Main.this, "Accesso riuscito!", Toast.LENGTH_SHORT).show();
            // Avvia l'Activity successiva dopo un accesso riuscito
            Intent intent = new Intent(A1_Main.this, A2_Welcome.class);
            startActivity(intent);
        }
    }


    // Metodo richiamato quando il testo di registrazione viene cliccato
    private void onSignUpClick() {
        // Aggiungi qui la logica per la gestione del click del testo di registrazione
        Intent intent = new Intent(A1_Main.this, A9_Signup.class);
        startActivity(intent);
    }
}
