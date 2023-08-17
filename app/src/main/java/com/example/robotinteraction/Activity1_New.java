package com.example.robotinteraction;

// Import delle librerie necessarie
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
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
import android.content.DialogInterface;

public class Activity1_New extends AppCompatActivity {

    // Dichiarazione delle variabili
    private static final long TIME_THRESHOLD = 60000; // Tempo di attesa prima dell'inattività
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private EditText editTextEmail, editTextPassword;
    private TextInputLayout emailTextInputLayout, passwordTextInputLayout;
    private Button buttonLogin;
    private TextView textViewError, textViewSignUp, textViewGuest;
    private Animation buttonAnimation;
    private Activity_SocketManager socket;
    private boolean isPasswordVisible = false;
    private ImageButton passwordToggle;
    private String sessionID = "Guest", LOG_IN_RESPONSE = "LOG_IN_ERROR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_1new);

        // Inizializzazione delle variabili e dei componenti
        initVariables();
        initUIComponents();
        setupListeners();
    }

    // Metodo per inizializzare le variabili non legate all'interfaccia utente
    private void initVariables() {
        socket = Activity_SocketManager.getInstance(); // Ottieni l'istanza del gestore del socket
        boolean connesso = socket.isConnected();

        /*if(connesso==false){
            showPopupMessage();
        }*/

        buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation); // Carica animazione per i pulsanti
        runnable = new Runnable() { // Azione da eseguire dopo l'inattività
            @Override
            public void run() {

                navigateTo(Activity0_OutOfSight.class);
            }
        };
    }

    // Metodo per inizializzare i componenti dell'interfaccia utente
    private void initUIComponents() {
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

    // Metodo per configurare i listener dei vari componenti
    private void setupListeners() {
        setFocusChangeListener(editTextEmail);
        setFocusChangeListener(editTextPassword);
        setTouchListenerForAnimation(buttonLogin);
        setTouchListenerForAnimation(textViewSignUp);
        setTouchListenerForAnimation(textViewGuest);
    }

    private void setFocusChangeListener(View view) {
        view.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    resetInactivityTimer();
                    textViewError.setVisibility(View.INVISIBLE);
                }
            }
        });
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
        new Handler().postDelayed(() -> v.clearAnimation(), 200);
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(Activity1_New.this, targetActivity);
        startActivity(intent);
    }

    private void navigateToParam(Class<?> targetActivity, String param) {
        Intent intent = new Intent(Activity1_New.this, targetActivity);
        intent.putExtra(param, param);
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

    public void onClickSignUp(View v) {
        resetInactivityTimer();
        Intent intent = new Intent(Activity1_New.this, Activity9_Signup.class);
        startActivity(intent);
    }

    public void onClickGuest(View v) {
        resetInactivityTimer();
        Intent intent = new Intent(Activity1_New.this, Activity2_Welcome.class);
        sessionID = "Guest";
        intent.putExtra("SESSION_ID", sessionID);
        startActivity(intent);
    }

    public void onClickAccedi(View v) {
        resetInactivityTimer();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            textViewError.setText("Inserisci email e password");
            textViewError.setVisibility(View.VISIBLE);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textViewError.setVisibility(View.INVISIBLE);
                        }
                    });

                    socket.send("LOG_IN"+" "+email+" "+password);
                    String response = socket.receive();

                    String[] parts = response.split(" ");

                    if (parts.length >= 2) {
                        LOG_IN_RESPONSE = parts[0];
                        sessionID = parts[1];
                    }

                    if ("LOG_IN_SUCCESS".equals(LOG_IN_RESPONSE)){
                        navigateToParam(Activity2_Welcome.class, sessionID);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewError.setVisibility(View.INVISIBLE);
                            }
                        });                       }
                    else if ("LOG_IN_ERROR".equals(LOG_IN_RESPONSE)){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewError.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            }).start();
        }
    }

    private void startInactivityTimer() {
        handler.postDelayed(runnable, TIME_THRESHOLD);
    }

    private void resetInactivityTimer() {
        handler.removeCallbacks(runnable);
        startInactivityTimer();
    }

    public void showPopupMessage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(Activity1_New.this);
                builder.setTitle("Server offline")
                        .setMessage("Attualmente i server sono offline, fai l'accesso come 'Ospite'!")
                        .setPositiveButton("Accedi come Ospite!", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Activity1_New.this, Activity2_Welcome.class);
                                sessionID = "Guest";
                                intent.putExtra("SESSION_ID", sessionID);
                                startActivity(intent);
                                dialog.dismiss();
                                finish();
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    public void togglePasswordVisibility(View view) {
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