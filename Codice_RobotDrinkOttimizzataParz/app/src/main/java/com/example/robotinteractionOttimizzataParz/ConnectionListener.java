package com.example.robotinteractionOttimizzataParz;

public interface ConnectionListener {
    void onConnected();
    void onConnectionFailed(String errore);
}