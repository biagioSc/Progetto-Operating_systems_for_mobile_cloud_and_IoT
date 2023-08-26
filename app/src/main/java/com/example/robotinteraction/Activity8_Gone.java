package com.example.robotinteraction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


public class Activity8_Gone extends AppCompatActivity {

    private TextView buttonExit;
    private Animation buttonAnimation;
    private Activity_SocketManager socket;  // Manager del socket per la comunicazione con il server
    private String sessionID = "-1", user = "Guest";
    private TextView textViewGoodbye;
    private TextView messageRating;
    private TextView messageValutazione;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_8gone);
        getWindow().setWindowAnimations(0);

        connection();
        initUIComponents();
        setupListeners();

        receiveParam();
    }
    private void connection() {
        socket = Activity_SocketManager.getInstance(); // Ottieni l'istanza del gestore del socket
    }
    private void initUIComponents() {
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        buttonExit = findViewById(R.id.buttonExit);
        textViewGoodbye = findViewById(R.id.textViewGoodbye);
        RatingBar ratingbar = findViewById(R.id.ratingBar);
        messageRating = findViewById(R.id.textViewMessageRating);
        messageValutazione = findViewById(R.id.textViewMessageValutazione);


        if(Activity4_Ordering.beenInOrdering == true){
            Activity4_Ordering.beenInOrdering = false;
            ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    updateRatingString((int) rating);
                }
            });
        }else{
            ratingbar.setVisibility(View.GONE);
            messageRating.setVisibility(View.GONE);
            messageValutazione.setVisibility(View.GONE);
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
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    applyButtonAnimation(v);
                }
                return false;
            }
        });
    }
    private void applyButtonAnimation(View v) {
        v.startAnimation(buttonAnimation);
        new Handler().postDelayed(v::clearAnimation, 200);
    }
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            sessionID = intent.getStringExtra("param1");
            user = intent.getStringExtra("param2");
            int atIndex = -1;
            if(user!=null){
                atIndex = user.indexOf("@");
                // Verificare se è presente il simbolo "@"
                if (atIndex != -1) {
                    String username = user.substring(0, atIndex);
                    runOnUiThread(() -> textViewGoodbye.setText(textViewGoodbye.getText() + "\n" + username));

                } else {
                    runOnUiThread(() -> textViewGoodbye.setText(textViewGoodbye.getText() + "\n" + user));
                }
            }
        }
    }
    public void onClickExit(View v) {
        v.setClickable(false);
        if(!("Guest".equals(user))) {
            try {
                socket.send("USER_GONE");
                Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio
                if(Integer.parseInt(sessionID) != 0) {
                    socket.send(sessionID);
                    Thread.sleep(1000); // Aggiungi un ritardo di 1000 millisecondi tra ogni invio
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }else{
            socket.close();
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finishAffinity();
        System.exit(0);

    }
}
