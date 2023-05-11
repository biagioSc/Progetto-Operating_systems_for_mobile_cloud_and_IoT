package com.example.robotinteraction;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import android.content.Intent;


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
                    showErrorDialog();
                }
                else{
                    Intent switchToWelcomeActivity = new Intent(MainActivity.this, WelcomeActivity.class);
                    startActivity(switchToWelcomeActivity);
                }
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent switchToSignUpActivity = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(switchToSignUpActivity);
            }
        });

    }

    private boolean verifyEmail(String email){
        //connessione al database e verifica della mail
        return true;
    }

    private boolean verifyPassword(String password){
        //connessione al database e verifica della password
        return true;
    };
    private boolean verifyCredentials(String email, String password){
        if(!verifyEmail(email) || !verifyPassword(password)){
            return false;
        }
        return true;
    };

    private void showErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Errore");
        builder.setMessage("Le credenziali inserite non sono corrette. Per favore, riprova.");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


}

//
