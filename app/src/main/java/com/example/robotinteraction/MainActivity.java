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
import com.google.android.material.snackbar.Snackbar;

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
                        // Crea una nuova istanza di SocketManager e tenta la connessione.
                        socket = SocketManager.getInstance();

                        // Se la connessione è riuscita, esce dal loop.
                        break;
                    } catch (Exception e) {
                        // Se la connessione non è riuscita, stampa lo stack trace e attende 5 secondi prima di riprovare.
                        e.printStackTrace();

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



    public void onSignUpClick(View view){
        Intent intent = new Intent(MainActivity.this,SignUpActivity.class);
        startActivity(intent);
    }


    //Gestione dell'evento click su accedi
    public void onLoginClick(View view){
        // Recupera l'email e la password inserite dall'utente.
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        // Crea un nuovo thread per gestire l'operazione di rete.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Invia l'email e la password all server.
                    socket.sendMessage(email);
                    socket.sendMessage(password);

                    // Ricevo la risposta dal Server.
                    String response = socket.receiveMessage();
                    Log.d("MainActivity", "Ho ricevuto msg server: "+response);

                    // Aggiorno l'interfaccia utente con la risposta dal server.
                    // Questo deve essere fatto sul thread principale, quindi utilizziamo runOnUiThread.
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            //IL SERVER DOPO IL LOGIN PUO' RISPONDERE CON 3 TIPI DI MESSAGGIO: ORDERING, WAITING, ERROR
                            //ORDERING: ci sono meno di due utenti connessi e si procede alla chat
                            //WAITING: ci sono più di due utenti connessi e si procede con l'attesa
                            //ERROR: il login non è andato a buon fine
                            if(response != null){
                                if (response.equalsIgnoreCase("Error")) {
                                    Toast.makeText(MainActivity.this, "Login fallito. Riprova.", Toast.LENGTH_SHORT).show();
                                    Log.d("MainActivity", "Sono entrato in errore");
                                } else {
                                    Toast.makeText(MainActivity.this, "Login effettuato con successo", Toast.LENGTH_SHORT).show();

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Dichiaro un intent per spostarmi
                                            Intent intent;
                                            if(response.equalsIgnoreCase("Ordering")){
                                                Log.d("MainActivity", "Sono entrato in ordering");
                                                intent = new Intent(MainActivity.this,OrderingActivity.class);
                                                startActivity(intent);
                                            }else if(response.equalsIgnoreCase("Waiting")){
                                                Log.d("MainActivity", "Sono entrato in waiting");
                                                intent = new Intent(MainActivity.this,WaitingActivity.class);
                                                startActivity(intent);
                                            }else{
                                                throw new IllegalArgumentException("La risposta Server non è stata riconosciuta");
                                            }
                                        }
                                    }, 3000); //ritardo di 3 secondi

                                }
                            }

                        }
                    });

                } catch (IOException e) {
                    // Se si verifica un'eccezione durante l'operazione di rete, stampo lo stack trace
                    // e mostro un messaggio di errore all'utente tramite Toast con runOnUiThread
                    e.printStackTrace();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Errore di rete. Riprova.", Toast.LENGTH_SHORT).show();
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









