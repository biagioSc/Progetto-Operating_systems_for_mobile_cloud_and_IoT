package com.example.robotinteraction;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import java.io.IOException;

public class OrderingActivity extends AppCompatActivity {

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
        socket.setMessageListener(new SocketManager.MessageListener() {
            @Override
            public void onNewMessage(final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.add(message);
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });

        // Avvio thread che ascolta per nuovi messaggi
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        socket.receiveMessage();
                    }catch (IOException e){
                        e.printStackTrace();
                        break;
                    }
                }
            }
        }).start();
    }


    public void onSendMessageClick(View view){
        String message = userResponse.getText().toString().trim();
        if(!message.isEmpty()){
            sendChatMessage(message);
            userResponse.setText("");
        }
    }

    public void sendChatMessage(String message) {
        // Aggiungo il messaggio alla ListView della chat.
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

                    //Se si è verificato un errore mostro un messaggio al client con Toast
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




}
