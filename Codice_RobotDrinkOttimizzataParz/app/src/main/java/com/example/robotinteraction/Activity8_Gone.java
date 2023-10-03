package com.example.robotinteraction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class Activity8_Gone extends AppCompatActivity {

    private Button buttonExit;
    private Animation buttonAnimation;
    private Socket_Manager socket;  // Manager del socket per la comunicazione con il server
    private String sessionID = "-1", user = "Guest", activity = "";
    private TextView textViewGoodbye;
    private TextView messageRating;
    private TextView messageValutazione;
    private RatingBar ratingbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_8gone);
        getWindow().setWindowAnimations(0);

        connection();
        initUIComponents();
        setupListeners();
        receiveParam();
        ShowRatingBarIfVisitedOrdering(ratingbar,messageRating,messageValutazione,buttonExit);
    }
    private void connection() {
        socket = Socket_Manager.getInstance(); // Ottieni l'istanza del gestore del socket
    }
    private void initUIComponents() {
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        buttonExit = findViewById(R.id.buttonExit);
        textViewGoodbye = findViewById(R.id.textViewGoodbye);
        ratingbar = findViewById(R.id.ratingBar);
        messageRating = findViewById(R.id.textViewMessageRating);
        messageValutazione = findViewById(R.id.textViewMessageValutazione);
    }

    public void ShowRatingBarIfVisitedOrdering(final RatingBar ratingbar,
                                                     final TextView messageRating,
                                                     final TextView messageValutazione,
                                                     final TextView buttonExit) {

        if ("FAREWELLING".equals(activity)) {
            // Imposta l'ascoltatore per la RatingBar
            ratingbar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> updateRatingString((int) rating));
        } else {
            // Nasconde la RatingBar e i messaggi associati
            ratingbar.setVisibility(View.INVISIBLE);
            messageRating.setVisibility(View.INVISIBLE);
            messageValutazione.setVisibility(View.INVISIBLE);

            // Aggiorna la posizione del pulsante di uscita
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) buttonExit.getLayoutParams();
            params.addRule(RelativeLayout.BELOW, R.id.textViewMessage);
            buttonExit.setLayoutParams(params);
        }
    }


    private void updateRatingString(int rating) {
        TextView ratingString = findViewById(R.id.textViewMessageRating);
        ratingString.setTextColor(Color.BLACK);
        switch (rating) {
            case 1:
                ratingString.setText("Scarsa!");
                break;
            case 2:
                ratingString.setText("Abbastanza Bene!");
                break;
            case 3:
                ratingString.setText("Bene!");
                break;
            case 4:
                ratingString.setText("Molto Bene!");
                break;
            case 5:
                ratingString.setText("Eccellente!");
                break;
            default:
                ratingString.setText("");
                break;
        }
    }

    private void setupListeners() {
        setTouchListenerForAnimation(buttonExit);
    }
    private void setTouchListenerForAnimation(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    applyButtonAnimation(v);
                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    break;
            }
            return false;
        });
    }
    private void applyButtonAnimation(View v) {
        v.startAnimation(buttonAnimation);
        new Handler().postDelayed(v::clearAnimation, 100);
    }
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            sessionID = intent.getStringExtra("param1");
            user = intent.getStringExtra("param2");
            activity = intent.getStringExtra("param3");

            int atIndex;
            if (user != null && !user.equals("ERROR")) {  // Verifica che user non sia nullo e non sia "ERROR"
                atIndex = user.indexOf("@");
                // Verifica se è presente il simbolo "@"
                if (atIndex != -1) {
                    String username = user.substring(0, atIndex);
                    textViewGoodbye.setText(textViewGoodbye.getText() + "\n" + username);
                } else {
                     textViewGoodbye.setText(textViewGoodbye.getText() + "\n" + user);
                }
            }
        }
    }

    public void onClickExit(View v) {

        v.setClickable(false);
        if(!("Guest".equals(user)) && socket != null) {
            try {
                socket.send("USER_GONE");
                Thread.sleep(500);
                if(Integer.parseInt(sessionID) != 0) {
                    socket.send(sessionID);
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "Errore nella connessione. Continuerai come Ospite...", Toast.LENGTH_SHORT).show());
                sessionID = "-1";
                user = "Guest";
            }
        }else if(socket != null){
            socket.close();
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishAffinity();
        finish();

    }
}
