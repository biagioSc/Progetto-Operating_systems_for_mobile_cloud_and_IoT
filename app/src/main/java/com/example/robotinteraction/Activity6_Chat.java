package com.example.robotinteraction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Activity6_Chat extends AppCompatActivity {

    private TextView textDomanda, textViewLoggedIn, scoreTextView;
    private RadioGroup answerRadioGroup;
    private Button confirmButton;
    private List<Activity_Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private Animation buttonAnimation;
    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private String[] selectedTopics = {"Storia", "Geografia"}; // Aggiungi gli argomenti desiderati
    private String sessionID = "-1", user = "Guest", selectedDrink;
    private Activity_SocketManager socket;  // Manager del socket per la comunicazione con il server
    private ProgressBar progressBar;
    private List<Activity_Question> selectedQuestions = new ArrayList<>();
    private static final long DELAY_BEFORE_NEXT_QUESTION = 1000; // Ritardo di 10 secondi
    private static final int DARK_GREEN_COLOR = Color.parseColor("#00A859"); // Colore verde scuro

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_6chat);

        connection();
        initUIComponents();
        setupListeners();

        receiveParam();
        setUpComponent();
    }
    private void connection() {
        socket = Activity_SocketManager.getInstance(); // Ottieni l'istanza del gestore del socket
        boolean connesso = socket.isConnected();

        /*if(connesso==false){
            showPopupMessage();
        }*/

        runnable = new Runnable() { // Azione da eseguire dopo l'inattività
            @Override
            public void run() {

                navigateTo(Activity0_OutOfSight.class);
            }
        };
    }
    private void initUIComponents() {
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        textDomanda = findViewById(R.id.textDomanda);
        answerRadioGroup = findViewById(R.id.answerRadioGroup);
        confirmButton = findViewById(R.id.confirmButton);
        scoreTextView = findViewById(R.id.scoreTextView);
        textViewLoggedIn = findViewById(R.id.textViewLoggedIn);
        progressBar = findViewById(R.id.timerProgressBar);

    }
    private void setupListeners() {
        setTouchListenerForAnimation(confirmButton);
        setOnClickListener(confirmButton);

    }
    private void setTouchListenerForAnimation(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    resetInactivityTimer();
                    applyButtonAnimation(v);
                }
                return false;
            }
        });
    }
    private void setOnClickListener(View view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
            }
        });
    }
    private void applyButtonAnimation(View v) {
        v.startAnimation(buttonAnimation);
        new Handler().postDelayed(() -> v.clearAnimation(), 200);
    }
    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(Activity6_Chat.this, targetActivity);
        startActivity(intent);
    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2, String param3) {
        Intent intent = new Intent(Activity6_Chat.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        intent.putExtra("param3", param3);
        startActivity(intent);
        finish();
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
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            sessionID = intent.getStringExtra("param1");
            user = intent.getStringExtra("param2");
            selectedDrink = intent.getStringExtra("param3");
            int atIndex = user.indexOf("@");

            // Verificare se è presente il simbolo "@"
            if (atIndex != -1) {
                String username = user.substring(0, atIndex);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textViewLoggedIn.setText(username);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        textViewLoggedIn.setText(user);
                    }
                });
            }
        }
    }
    private void setUpComponent() {
        initializeQuestionList();

        int totalQuestionsForSelectedTopics = 0;
        for (Activity_Question question : questionList) {
            if (isTopicSelected(question.getTopic())) {
                totalQuestionsForSelectedTopics++;
                selectedQuestions.add(question);
            }
        }
        progressBar.setMax(totalQuestionsForSelectedTopics);
        progressBar.setProgress(currentQuestionIndex);
        startGame();
    }
    private void initializeQuestionList() {
        questionList = new ArrayList<>();
        // Aggiungi le domande alla lista
        questionList.add(new Activity_Question("Storia", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2", "Risposta sbagliata 3"));
        questionList.add(new Activity_Question("Storia", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2", "Risposta sbagliata 3"));
        questionList.add(new Activity_Question("Attualità", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2", "Risposta sbagliata 3"));
        questionList.add(new Activity_Question("Attualità", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2", "Risposta sbagliata 3"));
        questionList.add(new Activity_Question("Geografia", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2", "Risposta sbagliata 3"));
        questionList.add(new Activity_Question("Geografia", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2", "Risposta sbagliata 3"));
        questionList.add(new Activity_Question("Sport", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2", "Risposta sbagliata 3"));
        questionList.add(new Activity_Question("Sport", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2", "Risposta sbagliata 3"));
        questionList.add(new Activity_Question("Scienza", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2", "Risposta sbagliata 3"));
        questionList.add(new Activity_Question("Scienza", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2", "Risposta sbagliata 3"));
        questionList.add(new Activity_Question("Informatica", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2", "Risposta sbagliata 3"));
        questionList.add(new Activity_Question("Informatica", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2", "Risposta sbagliata 3"));
        questionList.add(new Activity_Question("Letteratura", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2", "Risposta sbagliata 3"));
        questionList.add(new Activity_Question("Letteratura", "Domanda", "Risposta giusta", "Risposta sbagliata 1", "Risposta sbagliata 2", "Risposta sbagliata 3"));
    }
    private void startGame() {
        resetRadioButtonTextColors(); // Imposta tutti i testi a nero
        if (currentQuestionIndex < selectedQuestions.size()) {
            confirmButton.setEnabled(true);
            Activity_Question currentQuestion = selectedQuestions.get(currentQuestionIndex);
            textDomanda.setText(currentQuestion.getQuestion());
            List<String> answerOptions = new ArrayList<>();
            answerOptions.add(currentQuestion.getCorrectAnswer());
            answerOptions.add(currentQuestion.getWrongAnswer1());
            answerOptions.add(currentQuestion.getWrongAnswer2());
            answerOptions.add(currentQuestion.getWrongAnswer3());
            Collections.shuffle(answerOptions);

            ((RadioButton) answerRadioGroup.getChildAt(0)).setText("A) " + answerOptions.get(0));
            ((RadioButton) answerRadioGroup.getChildAt(1)).setText("B) " + answerOptions.get(1));
            ((RadioButton) answerRadioGroup.getChildAt(2)).setText("C) " + answerOptions.get(2));
            ((RadioButton) answerRadioGroup.getChildAt(3)).setText("D) " + answerOptions.get(3));

            answerRadioGroup.clearCheck();
            answerRadioGroup.setEnabled(true); // Abilita il gruppo di radiobutton
            progressBar.setProgress(currentQuestionIndex + 1); // Aggiorna la ProgressBar

            // Avvia il conteggio di tempo per la prossima domanda

        } else {
            scoreTextView.setText("Hai finito il quiz. Punteggio: " + score);
            //navigateToParam(Activity7_Farewelling.class, sessionID, user, selectedDrink);
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
    private void checkAnswer() {
        int darkGreenColor = DARK_GREEN_COLOR;
        Activity_Question currentQuestion = questionList.get(currentQuestionIndex);
        int selectedRadioButtonId = answerRadioGroup.getCheckedRadioButtonId();

        if (selectedRadioButtonId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
            String selectedAnswer = selectedRadioButton.getText().toString().substring(3);
            if (selectedAnswer.equals(currentQuestion.getCorrectAnswer())) {
                score++;
                scoreTextView.setText("Score: " + score);
                selectedRadioButton.setTextColor(darkGreenColor);
            } else {
                selectedRadioButton.setTextColor(Color.RED);
                // Colora in verde la risposta corretta
                for (int i = 0; i < answerRadioGroup.getChildCount(); i++) {
                    RadioButton radioButton = (RadioButton) answerRadioGroup.getChildAt(i);
                    String answerText = radioButton.getText().toString().substring(3);
                    if (answerText.equals(currentQuestion.getCorrectAnswer())) {
                        radioButton.setTextColor(darkGreenColor);
                        break;  // Esci dal loop una volta trovata la risposta corretta
                    }
                }
            }

            currentQuestionIndex++;
            confirmButton.setEnabled(false); // Disabilita il pulsante di conferma
            delayedStartNextQuestion();
        } else {
            Toast.makeText(Activity6_Chat.this, "Seleziona una risposta prima di confermare.", Toast.LENGTH_SHORT).show();
        }
    }
    private void delayedStartNextQuestion() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentQuestionIndex >= selectedQuestions.size()) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scoreTextView.setText("Hai finito il quiz. Punteggio: " + score);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    navigateToParam(Activity7_Farewelling.class, sessionID, user, selectedDrink);
                                }
                            }, 2000); // Ritardo di 2 secondi prima della navigazione
                        }
                    }, 1000);

                } else {
                    startGame();
                }
            }
        }, DELAY_BEFORE_NEXT_QUESTION);
    }

    private void resetRadioButtonTextColors() {
        for (int i = 0; i < answerRadioGroup.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) answerRadioGroup.getChildAt(i);
            radioButton.setTextColor(Color.BLACK);
        }
    }

}
