package com.example.robotinteraction;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Activity6_Chat extends AppCompatActivity {

    private TextView textDomanda, textViewLoggedIn, scoreTextView, scoreText;
    private RadioGroup answerRadioGroup;
    private Button confirmButton;
    private List<Activity_Question> questionList;
    private int currentQuestionIndex = 0, totalQuestionsForSelectedTopics = 0;
    private int score = 0;
    private Animation buttonAnimation;
    private static final long TIME_THRESHOLD = 60000; // 20 secondi
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private String[] selectedTopics; // Aggiungi gli argomenti desiderati
    private String sessionID = "-1", user = "Guest", selectedDrink, innerResponseDescription;
    private Socket_Manager socket;  // Manager del socket per la comunicazione con il server
    private ProgressBar progressBar;
    private List<Activity_Question> selectedQuestions = new ArrayList<>();
    private static final long DELAY_BEFORE_NEXT_QUESTION = 1000; // Ritardo di 10 secondi
    private static final int DARK_GREEN_COLOR = Color.parseColor("#00A859"); // Colore verde scuro
    private static final int ORANGE = Color.parseColor("#FFA500"); // Colore verde scuro

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_6chat);
        getWindow().setWindowAnimations(0);

        connection();
        initUIComponents();
        setupListeners();

        receiveParam();
        setUpComponent();
    }
    private void connection() {
        socket = Socket_Manager.getInstance(); // Ottieni l'istanza del gestore del socket
        runnable = () -> navigateTo(Activity0_OutOfSight.class, sessionID, user);
    }
    private void initUIComponents() {
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        textDomanda = findViewById(R.id.textDomanda);
        answerRadioGroup = findViewById(R.id.answerRadioGroup);
        confirmButton = findViewById(R.id.confirmButton);
        scoreTextView = findViewById(R.id.scoreTextView);
        textViewLoggedIn = findViewById(R.id.textViewLoggedIn);
        progressBar = findViewById(R.id.timerProgressBar);
        scoreText = findViewById(R.id.score);
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

        view.setOnClickListener(v -> checkAnswer());
    }
    private void applyButtonAnimation(View v) {
        v.startAnimation(buttonAnimation);
        new Handler().postDelayed(v::clearAnimation, 200);
    }
    private void navigateTo(Class<?> targetActivity, String param1, String param2) {
        Intent intent = new Intent(Activity6_Chat.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        startActivity(intent);
    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2, String param3, String param4) {
        Intent intent = new Intent(Activity6_Chat.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        intent.putExtra("param3", param3);
        intent.putExtra("param4", param4);

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
            selectedTopics = intent.getStringArrayExtra("param4");

            int atIndex = user.indexOf("@");

            // Verificare se è presente il simbolo "@"
            if (atIndex != -1) {
                String username = user.substring(0, atIndex);
                runOnUiThread(() -> textViewLoggedIn.setText(username));
            } else {
                runOnUiThread(() -> textViewLoggedIn.setText(user));
            }
        }
    }
    private void setUpComponent() {
        initializeQuestionList();

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

        questionList.add(new Activity_Question("Storia", "Qual è l'evento più importante della storia?", "La Rivoluzione Industriale", "La scoperta dell'America", "La caduta dell'Impero Romano", "La Guerra Fredda"));
        questionList.add(new Activity_Question("Storia", "Chi era il primo presidente degli Stati Uniti?", "George Washington", "Thomas Jefferson", "Benjamin Franklin", "Abraham Lincoln"));
        questionList.add(new Activity_Question("Storia", "Dove è iniziata la prima guerra mondiale?", "Europa", "Asia", "Africa", "Americhe"));
        questionList.add(new Activity_Question("Attualità", "Quale è il problema globale più urgente?", "Cambiamenti climatici", "Terrorismo", "Povertà", "Guerra nucleare"));
        questionList.add(new Activity_Question("Attualità", "Chi è il presidente attuale degli Stati Uniti?", "Joe Biden", "Donald Trump", "Barack Obama", "George Bush"));
        questionList.add(new Activity_Question("Attualità", "Qual è la capitale del Brasile?", "Brasilia", "Rio de Janeiro", "Sao Paulo", "Salvador"));
        questionList.add(new Activity_Question("Geografia", "Qual è il fiume più lungo del mondo?", "Il Nilo", "Il Rio delle Amazzoni", "Il Mississippi", "Il Danubio"));
        questionList.add(new Activity_Question("Geografia", "In quale paese si trova la Torre Eiffel?", "Francia", "Italia", "Spagna", "Germania"));
        questionList.add(new Activity_Question("Geografia", "Qual è il deserto più vasto del pianeta?", "Il deserto dell'Antartide", "Il deserto del Sahara", "Il deserto di Gobi", "Il deserto di Atacama"));
        questionList.add(new Activity_Question("Sport", "In quale sport si vince la Coppa Davis?", "Tennis", "Calcio", "Golf", "Basket"));
        questionList.add(new Activity_Question("Sport", "Quale squadra ha vinto di più nella storia dei Campionati mondiali di calcio?", "Brasile", "Italia", "Germania", "Argentina"));
        questionList.add(new Activity_Question("Sport", "Chi è considerato il più grande pugile di tutti i tempi?", "Muhammad Ali", "Mike Tyson", "Floyd Mayweather Jr.", "Rocky Marciano"));
        questionList.add(new Activity_Question("Scienza", "Qual è la legge di gravità formulata da Isaac Newton?", "La legge di gravitazione universale", "La legge dei grandi numeri", "La legge dell'attrito", "La legge della conservazione dell'energia"));
        questionList.add(new Activity_Question("Scienza", "Quale è l'elemento chimico più abbondante nell'universo?", "Idrogeno", "Ossigeno", "Elio", "Carbonio"));
        questionList.add(new Activity_Question("Scienza", "Qual è l'unità di misura della corrente elettrica?", "Ampere", "Watt", "Volt", "Ohm"));
        questionList.add(new Activity_Question("Informatica", "Cos'è un algoritmo?", "Una sequenza di istruzioni per risolvere un problema", "Un dispositivo hardware", "Un linguaggio di programmazione", "Un tipo di virus informatico"));
        questionList.add(new Activity_Question("Informatica", "Chi è considerato il padre dell'informatica?", "Alan Turing", "Bill Gates", "Steve Jobs", "Tim Berners-Lee"));
        questionList.add(new Activity_Question("Informatica", "Cosa significa HTML?", "HyperText Markup Language", "High Tech Modern Language", "Hyperlink and Text Manipulation Language", "Home Tool Markup Language"));
        questionList.add(new Activity_Question("Musica", "Qual è il compositore di \"La Nona Sinfonia\"?", "Ludwig van Beethoven", "Wolfgang Amadeus Mozart", "Johann Sebastian Bach", "Antonio Vivaldi"));
        questionList.add(new Activity_Question("Musica", "Quale strumento è detto il \"Re degli strumenti\"?", "L'organo", "Il violino", "Il pianoforte", "La chitarra"));
        questionList.add(new Activity_Question("Musica", "Qual è l'album più venduto di tutti i tempi?", "Thriller di Michael Jackson", "Back in Black degli AC/DC", "The Dark Side of the Moon dei Pink Floyd", "Their Greatest Hits (1971-1975) degli Eagles"));
        questionList.add(new Activity_Question("Letteratura", "Chi è l'autore di \"1984\"?", "George Orwell", "Aldous Huxley", "Ray Bradbury", "J.D. Salinger"));
        questionList.add(new Activity_Question("Letteratura", "Qual è l'opera più famosa di William Shakespeare?", "Romeo e Giulietta", "Il mercante di Venezia", "Macbeth", "Amleto"));
        questionList.add(new Activity_Question("Letteratura", "Chi ha scritto \"Il Gattopardo\"?", "Giuseppe Tomasi di Lampedusa", "Italo Calvino", "Umberto Eco", "Luigi Pirandello"));
    }
    private void startGame() {
        resetRadioButtonTextColors(); // Imposta tutti i testi a nero

        if (currentQuestionIndex < selectedQuestions.size()) {
            confirmButton.setEnabled(true);
            Activity_Question currentQuestion = selectedQuestions.get(currentQuestionIndex);
            runOnUiThread(() -> {
                textDomanda.setText(currentQuestion.getTopic() + ": " + currentQuestion.getQuestion());
            });
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
        Activity_Question currentQuestion = selectedQuestions.get(currentQuestionIndex);
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
            confirmButton.setEnabled(false);
            delayedStartNextQuestion();
        } else {
            Toast.makeText(Activity6_Chat.this, "Seleziona una risposta prima di confermare.", Toast.LENGTH_SHORT).show();
        }
    }
    private void delayedStartNextQuestion() {
        new Handler().postDelayed(() -> {
            if (currentQuestionIndex >= selectedQuestions.size()) {
                new Handler().postDelayed(this::showPopupMessage, 200);
            } else {
                startGame();
            }
        }, DELAY_BEFORE_NEXT_QUESTION);
    }
    private void resetRadioButtonTextColors() {
        for (int i = 0; i < answerRadioGroup.getChildCount(); i++) {
            RadioButton radioButton = (RadioButton) answerRadioGroup.getChildAt(i);
            radioButton.setTextColor(Color.BLACK);
        }
    }
    public void showPopupMessage() {
        runOnUiThread(() -> {
            LayoutInflater inflater = LayoutInflater.from(Activity6_Chat.this);
            View customView = inflater.inflate(R.layout.activity_00popupchat, null);
            int darkGreenColor = DARK_GREEN_COLOR;
            int orange = ORANGE;

            TextView scoreTextView = customView.findViewById(R.id.scoreTextView);
            if(score<3){
                scoreTextView.setTextColor(Color.RED);
            }else if(score==3){
                scoreTextView.setTextColor(orange);
            }else{
                scoreTextView.setTextColor(darkGreenColor);
            }

            scoreTextView.setText(score + "/" + totalQuestionsForSelectedTopics); // Imposta il punteggio

            AlertDialog.Builder builder = new AlertDialog.Builder(Activity6_Chat.this);
            builder.setCustomTitle(customView)
                    .setCancelable(false)  // Evita la chiusura cliccando all'esterno
                    .setPositiveButton("Ok", (dialog, id) -> {
                        if(!("Guest".equals(user))) {
                            new Thread(() -> {
                                try {
                                    socket.send("DRINK_DESCRIPTION");
                                    Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio
                                    socket.send(selectedDrink);
                                    Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio
                                    innerResponseDescription = socket.receive();
                                    Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio

                                    if(innerResponseDescription == null && innerResponseDescription.equalsIgnoreCase("DRINK_DESCRIPTION_NOT_FOUND")) {
                                        innerResponseDescription = "Descrizione non disponibile!";
                                    }
                                } catch (Exception e) {
                                    innerResponseDescription = "Descrizione non disponibile!";
                                }
                                navigateToParam(Activity7_Farewelling.class, sessionID, user, selectedDrink, innerResponseDescription);
                                dialog.dismiss();
                                finish();
                            }).start();
                        }else{
                            innerResponseDescription = "Descrizione non disponibile!";
                            navigateToParam(Activity7_Farewelling.class, sessionID, user, selectedDrink, innerResponseDescription);
                            dialog.dismiss();
                            finish();
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialogInterface -> {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(getResources().getColor(R.color.black)); // Sostituisci con il colore desiderato
            });

            dialog.show();
        });
    }

}
