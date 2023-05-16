package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button buttonLogOut = findViewById(R.id.buttonLogOut);
        buttonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent LogOut = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(LogOut);
                }

        });

        Button buttonGoToWaiting = findViewById(R.id.buttonGoToWaiting);
        buttonGoToWaiting.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent GoToWaiting = new Intent(WelcomeActivity.this, WaitingActivity.class);
                startActivity(GoToWaiting);
            }
        });
    }
}
