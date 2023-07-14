// Classe OrderingActivity
package com.example.robotinteraction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import java.io.IOException;

public class OrderingActivity extends AppCompatActivity implements SocketManager.MessageListener {

    private EditText userResponse;
    private ListView listView;
    private SocketManager socket;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordering);

        listView = findViewById(R.id.chatListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);
        userResponse = findViewById(R.id.messageEditText);

        socket = SocketManager.getInstance();
        socket.setMessageListener(this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String message = socket.receiveMessage();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.sendMessage("START_CHAT");
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OrderingActivity.this, "Errore nell'invio del messaggio. Riprova.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    public void onSendMessageClick(View view) {
        String message = userResponse.getText().toString().trim();
        if (!message.isEmpty()) {
            sendChatMessage(message);
            userResponse.setText("");
        }
    }

    public void onBackClick(View view){
        // Creo la nuova intent per il passaggio all'activity precedente, MainActivity
        Intent intent = new Intent(OrderingActivity.this,MainActivity.class);
        startActivity(intent);
    }
    public void sendChatMessage(String message) {
        // Aggiungo il messaggio alla ListView della chat
        adapter.add(message);
        adapter.notifyDataSetChanged();

        // Invio il messaggio al server su un Thread separato
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OrderingActivity.this, "Errore nell'invio del messaggio. Riprova.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onMessageReceived(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                receiveChatMessage(message);
            }
        });
    }

    public void receiveChatMessage(String message) {
        // Aggiungo il messaggio alla ListView della chat
        adapter.add(message);
        adapter.notifyDataSetChanged();

        // Cambio il colore del messaggio ricevuto
        int lastPosition = adapter.getCount() - 1;
        View listItem = listView.getChildAt(lastPosition);
        if (listItem != null) {
            listItem.setBackgroundColor(Color.parseColor("#FFFF00")); // Imposta il colore di sfondo del messaggio ricevuto (es. giallo)
        }
    }
}
