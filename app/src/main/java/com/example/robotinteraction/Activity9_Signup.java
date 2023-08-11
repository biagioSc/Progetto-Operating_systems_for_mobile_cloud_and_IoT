package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Patterns;
import android.text.TextWatcher;

public class Activity9_Signup extends AppCompatActivity {

    private EditText editTextFirstName, editTextLastName, editTextEmail, editTextPassword;
    private Button buttonRegisterContinue;
    private TextView textViewError;

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

        // Imposta un listener per il pulsante di registrazione
        buttonRegisterContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Ottieni il testo dai campi di input
                String firstName = editTextFirstName.getText().toString().trim();
                String lastName = editTextLastName.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                boolean nomeValido = !firstName.isEmpty() && !firstName.matches(".*\\d.*");
                boolean cognomeValido = !lastName.isEmpty() && !lastName.matches(".*\\d.*");
                boolean emailValido = !email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches();
                boolean passwordValida = password.length() >= 6;

                // Effettua il controllo sui campi obbligatori
                if (!nomeValido && !cognomeValido && !emailValido && !passwordValida) {
                    textViewError.setText("Email e/o password errate");
                    textViewError.setVisibility(View.VISIBLE);
                    return;
                }

                // Tutti i campi sono completati correttamente, puoi aprire l'activity "Interview"
                textViewError.setVisibility(View.INVISIBLE);
                buttonRegisterContinue.setEnabled(nomeValido && cognomeValido && emailValido && passwordValida);
                openInterviewActivity(firstName, lastName, email, password);
            }
        });
    }

    // Metodo per aprire l'activity "Interview"
    private void openInterviewActivity(String firstName, String lastName, String email, String password) {
        Intent intent = new Intent(this, Activity9_Interview.class);
        intent.putExtra("selectedDrink", firstName);
        intent.putExtra("selectedDrink", lastName);
        intent.putExtra("selectedDrink", email);
        intent.putExtra("selectedDrink", password);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
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
