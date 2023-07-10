package com.example.robotinteraction;


import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import android.util.Log;


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

        // Crea un nuovo thread per gestire il tentativo di connessione continua.
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        Log.d("MainActivity", "[CONNECTION] Tentativo di connessione...");

                        // Crea una nuova istanza di SocketManager e tenta la connessione.
                        socket = SocketManager.getInstance();
                        socket.attemptConnection();

                        if(socket.isConnected()){
                            Log.d("MainActivity","[CONNECTION] Connessione stabilita");
                            break;
                        }else {
                            throw new IOException();
                        }

                    } catch (Exception e) {
                        Log.d("MainActivity","[CONNECTION] Connessione fallita");

                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    // Gestione dell'evento click su accedi
    public void onLoginClick(View view){

        // Recupera l'email e la password inserite dall'utente.
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();



        // Verifico se i campi sono stati riempiti
        if(email.equals("") || password.equals("")){
            Toast.makeText(MainActivity.this, "Inserisci email e password per accedere.",Toast.LENGTH_SHORT).show();
        }else{

            // Controllo nuovamente la connessione al Server
            if(socket == null || !socket.isConnected()){
                Toast.makeText(MainActivity.this, "Connessione al server non riuscita. Riprova.",
                        Toast.LENGTH_SHORT).show();

                socket.attemptConnection();
                return;
            }
            // Gestisco lo scambio di messaggi col Server per completare la funzione di LogIn
            Log.d("MainActivity", "[APP] Controllo dati effettuato correttamente mando tutto al server ");
            manageLogInMessages(email,password);
        }


    }


    public void onSignUpClick(View view){
        Intent intent = new Intent(MainActivity.this,SignUpActivity.class);
        startActivity(intent);
    }


    private void manageLogInMessages(String email, String password){
        // Crea un nuovo thread per gestire l'operazione di rete.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                        socket.sendMessage("LOG_IN");
                        socket.sendMessage(email);
                        socket.sendMessage(password);

                        String response = socket.receiveMessage();
                        Log.d("MainActivity", "[APP]Ho ricevuto effettivamente qualcosa");
                        // Aggiorno l'interfaccia utente con la risposta dal server.
                        // Questo deve essere fatto sul thread principale, quindi utilizziamo runOnUiThread.
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                if(response != null)
                                {
                                    if (response.equalsIgnoreCase("LOG_IN_ERROR")) {
                                        Toast.makeText(MainActivity.this, "Login fallito." +
                                                " Riprova.", Toast.LENGTH_SHORT).show();

                                    } else {
                                        Toast.makeText(MainActivity.this, "Login " +
                                                "effettuato con successo", Toast.LENGTH_SHORT).show();

                                        new Handler().postDelayed(new Runnable()
                                        {
                                            @Override
                                            public void run() {
                                                Intent intent;
                                                if(response.equalsIgnoreCase("ORDERING")){
                                                    intent = new Intent(MainActivity.this, OrderingActivity.class);
                                                    startActivity(intent);
                                                }else if(response.equalsIgnoreCase("WAITING")){
                                                    intent = new Intent(MainActivity.this,
                                                            WaitingActivity.class);
                                                    startActivity(intent);
                                                }else{
                                                    throw new IllegalArgumentException("La risposta " +
                                                            "Server non è stata riconosciuta");
                                                }
                                            }
                                        }, 3000); //ritardo di 3 secondi

                                    }
                                }else{
                                    Toast.makeText(MainActivity.this, "Si è verificato un errore" +
                                            " lato Server", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });




                } catch (IOException e) {
                    Log.d("MainActivity","[ERROR] Connessione persa con il Server");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Errore di rete. " +
                                    "Riprova.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } finally {
                    // Indipendentemente dal fatto che l'operazione di rete sia riuscita o meno,
                    // mi assicuro di aver chiuso la socket per evitare la saturazione della rete.
                    socket.close();
                }
            }
        }).start();  // Avvio il thread.
    }





}









