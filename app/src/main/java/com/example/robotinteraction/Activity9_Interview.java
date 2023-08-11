package com.example.robotinteraction;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

public class Activity9_Interview extends AppCompatActivity {

    private CheckBox[] drinkCheckboxes;
    private CheckBox[] argCheckboxes;
    private Button buttonSubmit;
    private String param1;
    private String param2;
    private String param3;
    private String param4;
    private List<String> drinkSelections = new ArrayList<>();
    private List<String> argSelections = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_9interview);

        Intent intent = getIntent();
        param1 = intent.getStringExtra("param1");
        param2 = intent.getStringExtra("param2");
        param3 = intent.getStringExtra("param3");
        param4 = intent.getStringExtra("param4");

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

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //[SERVER] mandare dati al server
                Intent newIntent = new Intent(Activity9_Interview.this, Activity1_New.class);
                startActivity(newIntent);
            }
        });
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
