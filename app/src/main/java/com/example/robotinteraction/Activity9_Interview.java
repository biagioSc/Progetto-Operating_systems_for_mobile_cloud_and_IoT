package com.example.robotinteraction;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

public class Activity9_Interview extends AppCompatActivity {

    // [SERVER] DECIDERE SE I CHECHBOX VOGLIAMO RIEMPIRLI DINAMICAMENTE

    private CheckBox[] drinkCheckboxes;
    private CheckBox[] argCheckboxes;
    private Button buttonSubmit;
    private String param1;
    private String param2;
    private String param3;
    private String param4;
    private List<String> drinkSelections = new ArrayList<>();
    private List<String> argSelections = new ArrayList<>();
    private Animation buttonAnimation;
    private static final long TIME_THRESHOLD = 20000; // 20 secondi
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_9interview);

        Intent intent = getIntent();
        param1 = intent.getStringExtra("nome");
        param2 = intent.getStringExtra("cognome");
        param3 = intent.getStringExtra("email");
        param4 = intent.getStringExtra("password");

        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);

        drinkCheckboxes = new CheckBox[]{
                findViewById(R.id.checkBoxDrink1),
                findViewById(R.id.checkBoxDrink2),
                findViewById(R.id.checkBoxDrink3),
                findViewById(R.id.checkBoxDrink4),
                findViewById(R.id.checkBoxDrink5),
                findViewById(R.id.checkBoxDrink6),
                findViewById(R.id.checkBoxDrink7),
                findViewById(R.id.checkBoxDrink8)
        };

        argCheckboxes = new CheckBox[]{
                findViewById(R.id.checkBoxArg1),
                findViewById(R.id.checkBoxArg2),
                findViewById(R.id.checkBoxArg3),
                findViewById(R.id.checkBoxArg4),
                findViewById(R.id.checkBoxArg5),
                findViewById(R.id.checkBoxArg6),
                findViewById(R.id.checkBoxArg7),
                findViewById(R.id.checkBoxArg8)
        };

        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonSubmit.setEnabled(false);

        for (CheckBox checkBox : drinkCheckboxes) {
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateDrinkSelections();
                    updateSubmitButtonState();
                }
            });
        }

        for (CheckBox checkBox : argCheckboxes) {
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateArgSelections();
                    updateSubmitButtonState();

                    if (checkBox.getId() == R.id.checkBoxArg8 && checkBox.isChecked()) {
                        for (CheckBox argCheckBox : argCheckboxes) {
                            if (argCheckBox != checkBox) {
                                argCheckBox.setChecked(false);
                            }
                        }
                        updateArgSelections();
                        updateSubmitButtonState();
                    }
                }
            });
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Activity9_Interview.this, Activity0_OutOfSight.class);
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

    public void onClickRegister(View v) {
        //[SERVER] mandare dati al server
        buttonSubmit.startAnimation(buttonAnimation);
        Intent newIntent = new Intent(Activity9_Interview.this, Activity1_New.class);
        startActivity(newIntent);
    }

    private void updateDrinkSelections() {
        drinkSelections.clear();
        for (CheckBox checkBox : drinkCheckboxes) {
            if (checkBox.isChecked()) {
                drinkSelections.add(checkBox.getText().toString());
            }
        }
    }

    private void updateArgSelections() {
        argSelections.clear();
        for (CheckBox checkBox : argCheckboxes) {
            if (checkBox.isChecked()) {
                argSelections.add(checkBox.getText().toString());
            }
        }
    }

    private void updateSubmitButtonState() {
        boolean isDrinkSelected = !drinkSelections.isEmpty();
        boolean isArgSelected = !argSelections.isEmpty() || argCheckboxes[7].isChecked();

        buttonSubmit.setEnabled(isDrinkSelected && isArgSelected);

        if (argCheckboxes[7].isChecked()) {
            for (CheckBox argCheckBox : argCheckboxes) {
                if (argCheckBox != argCheckboxes[7]) {
                    argCheckBox.setChecked(false);
                }
            }
            updateArgSelections();
        }
    }
}
