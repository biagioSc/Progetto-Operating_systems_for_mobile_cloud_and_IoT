package com.example.robotinteraction;

// Import delle librerie necessarie
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputLayout;
import android.app.AlertDialog;

//todo tutti i bottoni non cliccabili dopo il primo tocco e i send alla schermata precedente
public class Activity1_New extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private TextInputLayout emailTextInputLayout, passwordTextInputLayout;
    private TextView buttonLogin, textViewError, textViewSignUp, textViewGuest;
    private Animation buttonAnimation;
    private Activity_SocketManager socket;
    private boolean isPasswordVisible = false;
    private ImageButton passwordToggle;
    private String sessionID = "-1", user = "Guest", email, password, LOG_IN_RESPONSE = "LOG_IN_ERROR";
    private static final long TIME_THRESHOLD = 60000; // Tempo di attesa prima dell'inattività
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_1new);

        connection();
        initUIComponents();
        setupListeners();

        receiveParam();
    }

    private void connection() {
        socket = Activity_SocketManager.getInstance(); // Ottieni l'istanza del gestore del socket
        runnable = () -> navigateTo(Activity0_OutOfSight.class, sessionID, user);
    }
    private void initUIComponents() {
        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        emailTextInputLayout = findViewById(R.id.emailTextInputLayout);
        passwordTextInputLayout = findViewById(R.id.passwordTextInputLayout);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewError = findViewById(R.id.textViewError);
        textViewSignUp = findViewById(R.id.buttonSignUp);
        textViewGuest = findViewById(R.id.buttonGuest);
        passwordToggle = findViewById(R.id.passwordToggle);
    }
    private void setupListeners() {
        setFocusChangeListener(editTextEmail);
        setFocusChangeListener(editTextPassword);
        setTouchListenerForAnimation(buttonLogin);
        setTouchListenerForAnimation(textViewSignUp);
        setTouchListenerForAnimation(textViewGuest);
    }
    private void receiveParam() {
        Intent intent = getIntent();
        if (intent != null) {
            user = intent.getStringExtra("param1");
            email = intent.getStringExtra("param2");
            password = intent.getStringExtra("param3");

            if("ERROR".equals(user)) {
                showPopupMessage();
            }else{
                editTextEmail.setText(email);
                editTextPassword.setText(password);
            }
        }
    }
    private void setFocusChangeListener(View view) {
        view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                resetInactivityTimer();
                textViewError.setVisibility(View.INVISIBLE);
            }
        });
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
    private void applyButtonAnimation(View v) {
        v.startAnimation(buttonAnimation);
        new Handler().postDelayed(v::clearAnimation, 200);
    }
    private void navigateTo(Class<?> targetActivity, String param1, String param2) {
        Intent intent = new Intent(Activity1_New.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        startActivity(intent);
    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2) {
        Intent intent = new Intent(Activity1_New.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
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
    public void onClickSignUp(View v) {
        v.setClickable(false);
        resetInactivityTimer();
        Intent intent = new Intent(Activity1_New.this, Activity9_Signup.class);
        startActivity(intent);
    }
    public void onClickGuest(View v) {
        v.setClickable(false);
        resetInactivityTimer();
        sessionID = "-1";
        user="Guest";
        navigateToParam(Activity2_Welcome.class, sessionID, user);

    }
    public void onClickAccedi(View v) {
        v.setClickable(false);
        resetInactivityTimer();
        email = editTextEmail.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            textViewError.setText("Inserisci email e password!");
            textViewError.setVisibility(View.VISIBLE);
        } else {
             new Thread(() -> {
                 runOnUiThread(() -> textViewError.setVisibility(View.INVISIBLE));
                 try {
                     socket.send("LOG_IN" + " " + email + " " + password);
                     String response = socket.receive();
                     String[] parts = response.split(" ");
                     if (parts.length >= 3) {
                         LOG_IN_RESPONSE = parts[0];
                         sessionID = parts[1];
                         user = parts[2];
                     }
                     if ("LOG_IN_SUCCESS".equals(LOG_IN_RESPONSE)) {
                         navigateToParam(Activity2_Welcome.class, sessionID, user);
                         runOnUiThread(() -> textViewError.setVisibility(View.INVISIBLE));
                     } else if ("LOG_IN_ERROR".equals(LOG_IN_RESPONSE)) {
                         runOnUiThread(() -> textViewError.setVisibility(View.VISIBLE));
                     }
                 }catch (Exception e){
                     showPopupMessage();
                 }
             }).start();
        }
    }
    public void showPopupMessage() {
        runOnUiThread(() -> {
            LayoutInflater inflater = LayoutInflater.from(Activity1_New.this);
            View customView = inflater.inflate(R.layout.activity_00popupguest, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(Activity1_New.this);
            builder.setCustomTitle(customView)
                    .setPositiveButton("Accedi come Ospite!", (dialog, id) -> {
                        sessionID = "-1";
                        user="Guest";
                        navigateToParam(Activity2_Welcome.class, sessionID, user);
                        dialog.dismiss();
                        finish();
                    });

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialogInterface -> {
                final int DARK_GREEN_COLOR = Color.parseColor("#00A859"); // Colore verde scuro
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(DARK_GREEN_COLOR); // Sostituisci con il colore desiderato
            });

            dialog.show();
        });
    }
    public void togglePasswordVisibility(View view) {
        resetInactivityTimer();
        if (isPasswordVisible) {
            editTextPassword.setTransformationMethod(new PasswordTransformationMethod());
            passwordToggle.setImageResource(R.drawable.hide);
        } else {
            editTextPassword.setTransformationMethod(null);
            passwordToggle.setImageResource(R.drawable.visible);
        }
        isPasswordVisible = !isPasswordVisible;
        editTextPassword.setSelection(editTextPassword.getText().length());
    }

}