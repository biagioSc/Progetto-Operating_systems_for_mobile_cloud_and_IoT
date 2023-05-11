package com.example.robotinteraction;

import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;

public class InterviewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview);

        Button buttonSignUpCompleted = findViewById(R.id.buttonSignUpCompleted);
        buttonSignUpCompleted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switchToMainActivity = new Intent(InterviewActivity.this, MainActivity.class);
                startActivity(switchToMainActivity);

            }
        });

        Button buttonBackToSignUp = findViewById(R.id.buttonBackToSignUp);
        buttonBackToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backToSignUp = new Intent(InterviewActivity.this, SignUpActivity.class);
                startActivity(backToSignUp);

            }
        });
    }

}
