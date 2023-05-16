package com.example.robotinteraction;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonSignUp;
    private SocketManager socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set degli ID per i componenti grafici
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);

        //creazione socket
        socket = SocketManager.getInstance();

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent switchToSignUpActivity = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(switchToSignUpActivity);
            }
        });

    }


    //Gestione dell'evento click su accedi
    public void onLoginClick(View view){
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    socket.sendMessage(email);
                    socket.sendMessage(password);

                    String response = socket.receiveMessage();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                        }
                    });

                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();



    }


}









