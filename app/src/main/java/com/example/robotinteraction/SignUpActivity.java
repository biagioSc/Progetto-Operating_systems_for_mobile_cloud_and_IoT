package com.example.robotinteraction;

import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    private EditText nameEditText, surnameEditText, emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Set the id for the graphic components
        nameEditText = findViewById(R.id.editTextNome);
        surnameEditText = findViewById(R.id.editTextCognome);
        emailEditText = findViewById(R.id.editTextEmail);
        passwordEditText = findViewById(R.id.editTextPassword);
    }

    public void onNextClick(View view){

        String name,surname,email,password;
        name = nameEditText.getText().toString();
        surname =  surnameEditText.getText().toString();
        email = emailEditText.getText().toString();
        password = passwordEditText.getText().toString();
        boolean emailChecker = checkIsGoodEmail(email);
        boolean nameChecker = checkIsGoodName(name);
        boolean surnameChecker = checkIsGoodSurname(surname);


            if(name.equals("") || surname.equals("") || email.equals("") || password.equals("")){
                Toast.makeText(SignUpActivity.this, "Compila tutti i campi prima di procedere",
                        Toast.LENGTH_SHORT).show();
                return;
            }else{
                //Se l'email non rispetta le condizioni per la mail, mostro un errore
                if(!emailChecker){
                    Toast.makeText(SignUpActivity.this, "Inserisci una email valida.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                //Se il nome non rispetta le condizioni per il nome, mostro un errore
                if(!nameChecker){
                    Toast.makeText(SignUpActivity.this, "Il nome contiene dei caratteri non validi.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                //Se il cognome non rispetta le condizioni per il cognome, mostro un errore
                if(!surnameChecker){
                    Toast.makeText(SignUpActivity.this, "Il cognome contiene dei caratteri non validi.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            }




        // Creo la nuova intent per il passaggio all' interviewActivity
        Intent intent = new Intent(SignUpActivity.this,InterviewActivity.class);

            // Passo i dati dall'activty presente all' interviewActivity che si occuperà di inoltrare
            // tutto al server
            intent.putExtra("name", name);
            intent.putExtra("surname", surname);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            startActivity(intent);
    }

    public void onBackClick(View view){
        // Creo la nuova intent per il passaggio all'activity precedente, MainActivity
        Intent intent = new Intent(SignUpActivity.this,MainActivity.class);
        startActivity(intent);
    }


    private boolean checkIsGoodEmail(String email){

        boolean check = false;

        // Verifica se l'email è valida.
        if (email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")) {
            check = true;
        }

        return check;
    }

    private boolean checkIsGoodName(String name){

        boolean check = false;

        // Verifica se il nome è valido.
        if (name.matches("[a-zA-Z ]+")) {
            check = true;
        }

        return check;
    }

    private boolean checkIsGoodSurname(String surname){

        boolean check = false;

        // Verifica se il cognome è valido.
        if (surname.matches("[a-zA-Z ]+")) {
            check = true;
        }

        return check;
    }

}