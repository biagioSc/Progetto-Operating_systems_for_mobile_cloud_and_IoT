package com.example.robotinteractionOttimizzata;

public interface ConnectionListener {
    void onConnected();
    void onConnectionFailed(String errore);
}