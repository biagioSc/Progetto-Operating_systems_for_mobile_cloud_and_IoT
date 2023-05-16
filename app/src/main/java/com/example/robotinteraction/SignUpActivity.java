package com.example.robotinteraction;

import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.widget.EditText;


import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    private EditText nameEditText,surnameEditText,emailEditText,passwordEditText;
    private SocketManager socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //set degli id
        nameEditText.findViewById(R.id.editTextNome);
        surnameEditText.findViewById(R.id.editTextCognome);
        emailEditText.findViewById(R.id.editTextEmail);
        passwordEditText.findViewById(R.id.editTextPassword);

        socket = SocketManager.getInstance();
    }

    public void onNextClick(View view){
        Intent intent = new Intent(SignUpActivity.this,InterviewActivity.class);
        intent.putExtra("name", nameEditText.getText().toString());
        intent.putExtra("surname", surnameEditText.getText().toString());
        intent.putExtra("email", emailEditText.getText().toString());
        intent.putExtra("password", passwordEditText.getText().toString());
        startActivity(intent);
    }

    public void onBackClick(View view){
        Intent intent = new Intent(SignUpActivity.this,MainActivity.class);
        startActivity(intent);
    }



}