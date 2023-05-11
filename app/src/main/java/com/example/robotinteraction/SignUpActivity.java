package com.example.robotinteraction;

import android.content.Intent;
import android.view.View;
import android.os.Bundle;
import android.widget.Button;


import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Button buttonGoToInterview = findViewById(R.id.buttonGoToInterview);
        buttonGoToInterview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switchToInterviewActivity = new Intent(SignUpActivity.this, InterviewActivity.class);
                startActivity(switchToInterviewActivity);

            }
        });

        Button buttonGoToMain = findViewById(R.id.buttonGoToMain);
        buttonGoToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent switchToMainActivity = new Intent(SignUpActivity.this, MainActivity.class);
                startActivity(switchToMainActivity);

            }
        });
    }

}