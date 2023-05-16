package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class WaitingActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        Button buttonLogOut = findViewById(R.id.buttonLogOut);
        buttonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent LogOut = new Intent(WaitingActivity.this, MainActivity.class);
                startActivity(LogOut);
            }

        });

        Button buttonGoToChat = findViewById(R.id.buttonGoToChat);
        buttonGoToChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent GoToChat = new Intent(WaitingActivity.this, OrderingActivity.class);
                startActivity(GoToChat);
            }

        });
    }
}
