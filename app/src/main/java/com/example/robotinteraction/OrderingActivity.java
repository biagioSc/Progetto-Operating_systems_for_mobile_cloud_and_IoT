package com.example.robotinteraction;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

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
    }


    public void onSendMessageClick(View view){
        String message = userResponse.getText().toString().trim();
        if(!message.isEmpty()){
            sendChatMessage(message);
            userResponse.setText("");
        }
    }

    private void sendChatMessage(String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                socket.sendMessage(message);

                String  response = null;
                response = socket.receiveMessage();


                if (response != null) {
                    String finalResponse = response;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.add("User: " + message);
                            adapter.add("Server: " + finalResponse);
                            adapter.notifyDataSetChanged();
                            listView.smoothScrollToPosition(adapter.getCount() - 1);
                        }
                    });
                }
            }
        }).start();
    }



}
