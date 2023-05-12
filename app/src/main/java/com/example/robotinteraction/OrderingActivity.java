package com.example.robotinteraction;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Random;


import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class OrderingActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ordering);

        ArrayList<String> drinks = new ArrayList<>();
        drinks.add("Negroni");
        drinks.add("Sex on the beach");
        drinks.add("Manhattan");
        drinks.add("Passion fruit Martini");
        drinks.add("Margarita");
        drinks.add("Cosmopolitan");
        drinks.add("Gimlet");
        drinks.add("Old Fashioned");
        drinks.add("Daiguiri");
        drinks.add("Black Russian");
        drinks.add("Bloody Mary");
        drinks.add("Mojito");

        Random rand = new Random();
        int randomIndex = rand.nextInt(drinks.size());



        ListView chatListView = findViewById(R.id.chatListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        adapter.add("Ti andrebbe un "+drinks.get(randomIndex)+"? Rispondi con si oppure no.");
        chatListView.setAdapter(adapter);

        EditText messageEditText = findViewById(R.id.messageEditText);
        ImageView sendMessage = findViewById(R.id.sendMessage);
        sendMessage.setOnClickListener(new View.OnClickListener(){

            boolean shownlist = false;

            @Override
            public void onClick(View v){

                String userResponseMessage = messageEditText.getText().toString();
                messageEditText.setText("");

                adapter.add(userResponseMessage);
                adapter.notifyDataSetChanged();

                if(!shownlist){
                    if(userResponseMessage.equalsIgnoreCase("si")){
                        adapter.add("Il tuo ordine è stato preso. Grazie!");
                    }
                    else if(userResponseMessage.equalsIgnoreCase("no")){
                        drinks.remove(randomIndex);
                        adapter.add("Ecco a te la lista dei drink disponibili: ");
                        adapter.addAll(drinks);
                        shownlist = true;

                    }
                    else{
                        adapter.add("Mi dispiace, non ho capito. Rispondi con si oppure no.");
                    }
                }
                else{
                    for(String drink : drinks){
                        if(drink.equalsIgnoreCase(userResponseMessage)){
                            adapter.add("Hai scelto il drink: "+drink+". Grazie!");

                        }
                        else{
                            adapter.add("Il drink che hai inserito non è presente nella lista dei drink disponibili.");

                        }
                        break;
                    }
                }

            }
        });




    }
}