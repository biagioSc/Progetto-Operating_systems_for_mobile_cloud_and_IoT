package com.example.robotinteraction;

public interface ConnectionListener {
    void onConnected();
    void onConnectionFailed(String errore);
}