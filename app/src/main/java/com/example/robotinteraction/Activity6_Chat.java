package com.example.robotinteraction;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Activity6_Chat extends AppCompatActivity {

    private TextView textDomanda, scoreTextView;
    private Button buttonOption1, buttonOption2, buttonOption3;
    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private String[] selectedTopics = {"Storia", "Geografia"}; // Aggiungi gli argomenti desiderati

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_6chat);

        textDomanda = findViewById(R.id.textDomanda);
        scoreTextView = findViewById(R.id.scoreTextView);
        buttonOption1 = findViewById(R.id.buttonOption1);
        buttonOption2 = findViewById(R.id.buttonOption2);
        buttonOption3 = findViewById(R.id.buttonOption3);

        final String selectedDrink = getIntent().getStringExtra("selectedDrink");

        // Inizializza la lista delle domande
        initializeQuestionList();

        // Avvia il gioco
        startGame(selectedDrink);
    }

    private void initializeQuestionList() {
        questionList = new ArrayList<>();
        // Aggiungi le domande alla lista
        questionList.add(new Question("Storia", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2"));
        questionList.add(new Question("Storia", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2"));
        questionList.add(new Question("Attualità", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2"));
        questionList.add(new Question("Attualità", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2"));
        questionList.add(new Question("Geografia", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2"));
        questionList.add(new Question("Geografia", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2"));
        questionList.add(new Question("Sport", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2"));
        questionList.add(new Question("Sport", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2"));
        questionList.add(new Question("Scienza", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2"));
        questionList.add(new Question("Scienza", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2"));
        questionList.add(new Question("Informatica", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2"));
        questionList.add(new Question("Informatica", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2"));
        questionList.add(new Question("Letteratura", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2"));
        questionList.add(new Question("Letteratura", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2"));
    }

    private void startGame(String selectedDrink) {
        if (currentQuestionIndex < questionList.size()) {
            // Ottieni la domanda corrente
            Question currentQuestion = questionList.get(currentQuestionIndex);

            if (isTopicSelected(currentQuestion.getTopic())) {
                textDomanda.setText(currentQuestion.getQuestion());
                List<String> answerOptions = new ArrayList<>();
                answerOptions.add(currentQuestion.getCorrectAnswer());
                answerOptions.add(currentQuestion.getWrongAnswer1());
                answerOptions.add(currentQuestion.getWrongAnswer2());
                Collections.shuffle(answerOptions);
                buttonOption1.setText(answerOptions.get(0));
                buttonOption2.setText(answerOptions.get(1));
                buttonOption3.setText(answerOptions.get(2));

                // Aggiungi i listener per i pulsanti di risposta
                buttonOption1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkAnswer(buttonOption1.getText().toString(), selectedDrink);
                    }
                });

                buttonOption2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkAnswer(buttonOption2.getText().toString(), selectedDrink);
                    }
                });

                buttonOption3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        checkAnswer(buttonOption3.getText().toString(), selectedDrink);
                    }
                });
            } else {
                // La domanda non corrisponde agli argomenti selezionati, passa alla prossima domanda
                currentQuestionIndex++;
                startGame(selectedDrink);
            }
        } else {
            // Il gioco è finito, vai alla schermata successiva (Activity_7Farewelling)
            openFarewellingActivity(selectedDrink);
        }
    }

    private boolean isTopicSelected(String topic) {
        for (String selectedTopic : selectedTopics) {
            if (selectedTopic.equals(topic)) {
                return true;
            }
        }
        return false;
    }
    private void checkAnswer(String selectedAnswer, String selectedDrink) {
        Question currentQuestion = questionList.get(currentQuestionIndex);
        if (selectedAnswer.equals(currentQuestion.getCorrectAnswer())) {
            // Risposta corretta, aggiorna lo score
            score++;
            scoreTextView.setText("Score: " + score);
        }

        // Passa alla prossima domanda
        currentQuestionIndex++;
        startGame(selectedDrink);
    }

    private void openFarewellingActivity(String selectedDrink) {
        Intent intent = new Intent(this, Activity7_Farewelling.class);
        intent.putExtra("selectedDrink", selectedDrink);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

    }
}
