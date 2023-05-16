package com.example.robotinteraction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;


public class  InterviewActivity extends AppCompatActivity {

    private CheckBox drinkCheckBox1, drinkCheckBox2, drinkCheckBox3, drinkCheckBox4;
    private CheckBox topicCheckBox1, topicCheckBox2, topicCheckBox3, topicCheckBox4;
    private SocketManager socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interview);

        drinkCheckBox1 = findViewById(R.id.checkBoxDrink1);
        drinkCheckBox2 = findViewById(R.id.checkBoxDrink2);
        drinkCheckBox3 = findViewById(R.id.checkBoxDrink3);
        drinkCheckBox4 = findViewById(R.id.checkBoxDrink4);

        topicCheckBox1 = findViewById(R.id.checkBoxTopic1);
        topicCheckBox2 = findViewById(R.id.checkBoxTopic2);
        topicCheckBox3 = findViewById(R.id.checkBoxTopic3);
        topicCheckBox4 = findViewById(R.id.checkBoxTopic4);

        socket = SocketManager.getInstance();
    }


    public void onSignUpDoneClick(View view){
        String name = getIntent().getStringExtra("name");
        String surname = getIntent().getStringExtra("surname");
        String email = getIntent().getStringExtra("email");
        String password = getIntent().getStringExtra("password");

        ArrayList<String> drinkPreferences = new ArrayList<>();
        ArrayList<String> topicsPreferences = new ArrayList<>();


        if (drinkCheckBox1.isChecked()) drinkPreferences.add(drinkCheckBox1.getText().toString());
        if (drinkCheckBox2.isChecked()) drinkPreferences.add(drinkCheckBox2.getText().toString());
        if (drinkCheckBox3.isChecked()) drinkPreferences.add(drinkCheckBox3.getText().toString());
        if (drinkCheckBox4.isChecked()) drinkPreferences.add(drinkCheckBox4.getText().toString());

        if (topicCheckBox1.isChecked()) topicsPreferences.add(topicCheckBox1.getText().toString());
        if (topicCheckBox2.isChecked()) topicsPreferences.add(topicCheckBox2.getText().toString());
        if (topicCheckBox3.isChecked()) topicsPreferences.add(topicCheckBox3.getText().toString());
        if (topicCheckBox4.isChecked()) topicsPreferences.add(topicCheckBox4.getText().toString());

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.sendMessage(name);
                    socket.sendMessage(surname);
                    socket.sendMessage(email);
                    socket.sendMessage(password);

                    for(String drink : drinkPreferences){
                        socket.sendMessage(drink);
                    }
                    for(String topic : topicsPreferences){
                        socket.sendMessage(topic);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }

                String response = null;
               try {
                   response = socket.receiveMessage();
               }catch (IOException e){
                   e.printStackTrace();
               }
                
                if(response != null){
                    String finalResponse = response;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(InterviewActivity.this, finalResponse, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
             

            }
        }).start();

    }

    public void onBackToSignUpClick(View view){
        finish();
    }
}
