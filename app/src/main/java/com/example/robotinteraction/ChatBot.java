package com.example.robotinteraction;
import android.widget.ArrayAdapter;

import java.util.*;

public class ChatBot {
    private ArrayList<String> availableDrinks;
    private ArrayList<String> availableTopics;
    private String botMessage;
    private String userMessage;
    private int botMessageArgument; //argomento del messaggio
    private ArrayList<String> botAcceptedAnswers //in base all'argomento
    private String suggestion;




    public ChatBot(String botMessage){
        setAvailableDrinks();
        setAvailableTopics();
        setUserMessage("null");
        setSuggestion();
        setBotMessage(botMessage);
        setBotMessageArgument(1);
        setBotAcceptedAnswers(1);
    }

    public String getBotMessage() {
        return botMessage;
    }



    //1 - si/no
    //2 - drink
    //3 - topics
    public void setBotAcceptedAnswers(int botMessageArgument) {
        switch (botMessageArgument){
            case 1: {
                this.botAcceptedAnswers.add("Si");
                this.botAcceptedAnswers.add("No");
            }
            case 2:{

                this.botAcceptedAnswers.add("Negroni");
                this.botAcceptedAnswers.add("Sex on the beach");
                this.botAcceptedAnswers.add("Manhattan");
                this.botAcceptedAnswers.add("Passion fruit Martini");
                this.botAcceptedAnswers.add("Margarita");
                this.botAcceptedAnswers.add("Cosmopolitan");
                this.botAcceptedAnswers.add("Gimlet");
                this.botAcceptedAnswers.add("Old Fashioned");
                this.botAcceptedAnswers.add("Daiguiri");
                this.botAcceptedAnswers.add("Black Russian");
                this.botAcceptedAnswers.add("Bloody Mary");
                this.botAcceptedAnswers.add("Mojito");
            }

            case 3:{

                this.botAcceptedAnswers.add("Attualità");
                this.botAcceptedAnswers.add("Politica");
                this.botAcceptedAnswers.add("Sport");
                this.botAcceptedAnswers.add("Tecnologia");
                this.botAcceptedAnswers.add("Arte");
            }

            //per differenziare la richiesta di intrattenimento
            case 4: {
                this.botAcceptedAnswers.add("Si");
                this.botAcceptedAnswers.add("No");
            }
        }
    }

    public ArrayList<String> getBotAcceptedAnswers() {
        return botAcceptedAnswers;
    }

    public void setBotMessageArgument(int botMessageArgument) {
        this.botMessageArgument = botMessageArgument;
    }

    public int getBotMessageArgument() {
        return botMessageArgument;
    }

    public void setAvailableDrinks() {
        this.availableDrinks.add("Negroni");
        this.availableDrinks.add("Sex on the beach");
        this.availableDrinks.add("Manhattan");
        this.availableDrinks.add("Passion fruit Martini");
        this.availableDrinks.add("Margarita");
        this.availableDrinks.add("Cosmopolitan");
        this.availableDrinks.add("Gimlet");
        this.availableDrinks.add("Old Fashioned");
        this.availableDrinks.add("Daiguiri");
        this.availableDrinks.add("Black Russian");
        this.availableDrinks.add("Bloody Mary");
        this.availableDrinks.add("Mojito");
    }

    public void setAvailableTopics() {
        this.availableTopics.add("Attualità");
        this.availableTopics.add("Politica");
        this.availableTopics.add("Sport");
        this.availableTopics.add("Tecnologia");
        this.availableTopics.add("Arte");
    }

    public void setBotMessage(String botMessage) {
        this.botMessage = botMessage + " "+" Suggerimento del giorno: "+this.getSuggestion();
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public ArrayList<String> getAvailableDrinks() {
        return availableDrinks;
    }

    public ArrayList<String> getAvailableTopics() {
        return availableTopics;
    }

    public void answer(String userMessage, ArrayAdapter<String> adapter){
        ArrayList<String> allowedMessages = this.getBotAcceptedAnswers();
        int messageArgument = this.getBotMessageArgument();
        ArrayList<String> drinks = this.getAvailableDrinks();
        int verified = 0;

        for( String message : allowedMessages){
            if(message.equalsIgnoreCase(userMessage))
            {
                verified = 1;

                if(messageArgument == 1 && message.equalsIgnoreCase("si")){
                    adapter.add("Il tuo ordine è stato preso in carico. Grazie!");
                    this.botMessageArgument = 4; //richiesta intrattenimento


                }
                else if(messageArgument == 1 && message.equalsIgnoreCase("no")){
                    adapter.add("Ecco a te la lista dei drink disponibili: ");
                    adapter.addAll(drinks);
                    this.botMessageArgument = 2; //scegliere il drink

                }
                else if(messageArgument == 2){
                    adapter.add("Hai selezionato "+userMessage+". Il tuo ordine è stato preso in carico!");
                    this.botMessageArgument = 4; //richiesta intrattenimento
                }
                else if(messageArgument == 4 && message.equalsIgnoreCase("si")){


                }
                else if(messageArgument == 4 && message.equalsIgnoreCase("no")){

                }
                break;
            }
        }
        //la risposta non è valida
        if(verified == 0){
            adapter.add("Mi dispiace ma non ho capito. Segui le istruzioni per dare risposte ammesse.");
        }

    }

    public void setSuggestion() {
        Random rand = new Random();
        int randomIndex = rand.nextInt(this.availableDrinks.size());
        this.suggestion = availableDrinks.get(randomIndex);
    }

    public String getSuggestion() {
        return suggestion;
    }




}

