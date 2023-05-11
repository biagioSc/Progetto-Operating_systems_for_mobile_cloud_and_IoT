package com.example.robotinteraction;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;

    private Button buttonSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set degli ID per i componenti grafici
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonSignUp = findViewById(R.id.buttonSignUp);

        // Gestire evento sul pulsante accedi
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                // Verificare i dati con il database
                if(!verifyCredentials(email,password)){
                    //messaggio di errore, inserire nuovamente i dati
                }
                else{
                    //procedere...
                }
            }
        });

        //Gestione evento sul pulsante registrati
        buttonSignUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onLick(View v){
                //aprire pagina di registrazione
            }
        });
    }

    private boolean verifyEmail(String email){
        //connessione al database e verifica della mail
        return false;
    }

    private boolean verifyPassword(String password){
        //connessione al database e verifica della password
        return false;
    };
    private boolean verifyCredentials(String email, String password){
        if(!verifyEmail(email) || !verifyPassword(password)){
            return false;
        }
        return true;
    };
}

//
