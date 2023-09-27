package com.example.robotinteraction;

// Import delle librerie necessarie

import android.app.AlertDialog;
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;


public class Activity1_New extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private TextInputLayout emailTextInputLayout, passwordTextInputLayout;
    private TextView textViewError, textViewSignUp, textViewGuest;
    private Button buttonLogin;
    private Animation buttonAnimation;
    private Socket_Manager socket;
    private boolean isPasswordVisible = false;
    private ImageButton passwordToggle, exitButton;
    private String sessionID = "-1", user = "Guest", email, password, LOG_IN_RESPONSE = "LOG_IN_ERROR";
    private static final long TIME_THRESHOLD = 60000; // Tempo di attesa prima dell'inattività
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private boolean isServerConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_1new);

        getWindow().setWindowAnimations(0);

        connection();
        initUIComponents();
        setupListeners();

        receiveParam();
    }

    private void connection() {
        initializeSocketAndSetCallback();
        runnable = () -> navigateTo(Activity0_OutOfSight.class, sessionID, user);
    }
    private void initializeSocketAndSetCallback() {
        socket = Socket_Manager.getInstance();  // Aggiunto
        if (socket != null) {
            socket.setConnectionListener(new ConnectionListener() {
                @Override
                public void onConnected() {
                    isServerConnected = true;
                    // Aggiorna l'UI o fai altre operazioni qui se necessario
                }

                @Override
                public void onConnectionFailed(String errore) {
                    isServerConnected = false;
                    // Mostra un popup o altre notifiche qui
                    runOnUiThread(() -> showPopupMessage());
                }
            });
        } else {
            // Se l'istanza è null, mostra un popup
            runOnUiThread(() -> showPopupMessage());
        }
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
        exitButton = findViewById(R.id.exitToggle);
    }
    private void setupListeners() {
        setFocusChangeListener(editTextEmail);
        setFocusChangeListener(editTextPassword);
        setTouchListenerForAnimation(buttonLogin);
        setTouchListenerForAnimation(exitButton);
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
        new Handler().postDelayed(v::clearAnimation, 100);
    }
    private void navigateTo(Class<?> targetActivity, String param1, String param2) {
        Intent intent = new Intent(Activity1_New.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        startActivity(intent);
    }
    private void navigateToParam(Class<?> targetActivity, String param1, String param2, String param3) {
        Intent intent = new Intent(Activity1_New.this, targetActivity);
        intent.putExtra("param1", param1);
        intent.putExtra("param2", param2);
        if("GONE".equals(param3)) intent.putExtra("param3", param3);
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
        finish();
    }
    public void onClickGuest(View v) {
        v.setClickable(false);
        resetInactivityTimer();
        sessionID = "-1";
        user="Guest";
        navigateToParam(Activity2_Welcome.class, sessionID, user,"");
        try {
            socket.close();
        } catch (Exception e) {
            sessionID = "-1";
            user = "Guest";
        }

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
                    Thread.sleep(250);
                    String response = socket.receive();
                    Thread.sleep(250);
                    if(response=="[ERROR]"){
                        throw new Exception();
                    }
                    String[] parts = response.split(" ");
                    if (parts.length >= 3) {
                        LOG_IN_RESPONSE = parts[0];
                        sessionID = parts[1];
                        user = parts[2];
                    }
                    if ("LOG_IN_SUCCESS".equals(LOG_IN_RESPONSE)) {
                        navigateToParam(Activity2_Welcome.class, sessionID, user,"");
                        runOnUiThread(() -> textViewError.setVisibility(View.INVISIBLE));
                    } else if ("LOG_IN_ERROR".equals(LOG_IN_RESPONSE)) {
                        v.setClickable(true);
                        runOnUiThread(() -> textViewError.setVisibility(View.VISIBLE));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    // Se c'è un'eccezione, mostra il popup
                    showPopupMessage();
                }
            }).start();
        }
    }
    public void showPopupMessage() {
        runOnUiThread(() -> {
            View customView = LayoutInflater.from(Activity1_New.this).inflate(R.layout.activity_00popupguest, null);

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Activity1_New.this);
            builder.setView(customView)
                    .setCancelable(false)  // Evita la chiusura cliccando all'esterno
                    .setPositiveButton("Accedi come Ospite", (dialog, id) -> {
                        sessionID = "-1";
                        user = "Guest";
                        navigateToParam(Activity2_Welcome.class, sessionID, user,"");
                        dialog.dismiss();
                        finish();
                    })
                    .setCancelable(false)  // Evita la chiusura cliccando all'esterno
                    .setNegativeButton("Esci", (dialog, id) -> {
                        sessionID = "-1";
                        user = "Guest";
                        navigateToParam(Activity8_Gone.class, sessionID, user, "NEW");
                        dialog.dismiss();
                        finish();
                    });

            androidx.appcompat.app.AlertDialog dialog = builder.create();
            dialog.setOnShowListener(dialogInterface -> {
                final int DARK_GREEN_COLOR = Color.parseColor("#00A859");
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setTextColor(DARK_GREEN_COLOR);

                Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                negativeButton.setTextColor(Color.RED);

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

    public void ExitNew(View v) {

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