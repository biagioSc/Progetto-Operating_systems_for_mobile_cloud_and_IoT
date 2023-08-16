package com.example.robotinteraction;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Activity_SocketManager {
    private static final String SERVER_IP = "195.231.38.118";
    private static final int SERVER_PORT = 8080;
    private Socket socket;
    private BufferedReader reader;
    private OutputStream outputStream;
    private static Activity_SocketManager instance ;
    private boolean isConnected = false; // Aggiunta della variabile di stato
    private boolean isConnecting = false;

    private Activity_SocketManager() {
        connectToServer();
    }

    private synchronized void connectToServer() {
        if (isConnecting) {
            return;
        }
        isConnecting = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(SERVER_IP, SERVER_PORT);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    outputStream = socket.getOutputStream();
                    isConnected = true;

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Activity_SocketManager", "Errore durante la connessione al server: " + e.getMessage());
                    if (e instanceof ConnectException) {
                        Log.e("Activity_SocketManager", "Il server non è attivo o non raggiungibile");
                    } else if (e instanceof SocketTimeoutException) {
                        Log.e("Activity_SocketManager", "Timeout di connessione raggiunto");
                    }
                }
                isConnecting = false;
            }
        }).start();
    }

    public static Activity_SocketManager getInstance() {
        if (instance == null) {
            instance = new Activity_SocketManager();
        }
        return instance;
    }

    public void send(final String message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    outputStream.write(message.getBytes());
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public String receive() {
        final StringBuilder receivedMessage = new StringBuilder();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        receivedMessage.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return receivedMessage.toString();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void close() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket != null) {
                        socket.close();
                    }
                    if (reader != null) {
                        reader.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}