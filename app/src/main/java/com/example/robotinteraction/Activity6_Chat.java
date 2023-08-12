package com.example.robotinteraction;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
    private Animation buttonAnimation;
    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private String[] selectedTopics = {"Storia", "Geografia"}; // Aggiungi gli argomenti desiderati

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_6chat);

        // [SERVER] AGGIUNGERE LOGICA CHE MI MANDA ARGOMENTI PREFERITI SERVER

        textDomanda = findViewById(R.id.textDomanda);
        scoreTextView = findViewById(R.id.scoreTextView);
        buttonOption1 = findViewById(R.id.buttonOption1);
        buttonOption2 = findViewById(R.id.buttonOption2);
        buttonOption3 = findViewById(R.id.buttonOption3);
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);

        final String selectedDrink = getIntent().getStringExtra("selectedDrink");

        // Inizializza la lista delle domande
        initializeQuestionList();

        // Avvia il gioco
        startGame(selectedDrink);
        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Activity6_Chat.this, Activity0_OutOfSight.class);
                startActivity(intent);
                finish();
            }
        };

        startInactivityTimer();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        resetInactivityTimer();
        return super.onTouchEvent(event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        resetInactivityTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    private void startInactivityTimer() {
        handler.postDelayed(runnable, TIME_THRESHOLD);
    }

    private void resetInactivityTimer() {
        handler.removeCallbacks(runnable);
        startInactivityTimer();
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
                        buttonOption1.startAnimation(buttonAnimation);
                        checkAnswer(buttonOption1.getText().toString(), selectedDrink, buttonOption1);
                    }
                });

                buttonOption2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buttonOption2.startAnimation(buttonAnimation);
                        checkAnswer(buttonOption2.getText().toString(), selectedDrink, buttonOption2);
                    }
                });

                buttonOption3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        buttonOption3.startAnimation(buttonAnimation);
                        checkAnswer(buttonOption3.getText().toString(), selectedDrink, buttonOption3);
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
    private void checkAnswer(String selectedAnswer, String selectedDrink, Button buttonSelected) {
        Question currentQuestion = questionList.get(currentQuestionIndex);
        if (selectedAnswer.equals(currentQuestion.getCorrectAnswer())) {
            // Risposta corretta, aggiorna lo score
            score++;
            scoreTextView.setText("Score: " + score);
            buttonSelected.setBackgroundResource(R.drawable.correct_button_background);
        }
        else{
            buttonSelected.setBackgroundResource(R.drawable.wrong_button_background);
        }

        // Passa alla prossima domanda
        currentQuestionIndex++;
        startGame(selectedDrink);
    }

    private void openFarewellingActivity(String selectedDrink) {
        Intent intent = new Intent(this, Activity7_Farewelling.class);
        intent.putExtra("selectedDrink", selectedDrink);
        startActivity(intent);

    }
}
