package com.example.robotinteraction;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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

    private Activity_SocketManager() {

        connectToServer();
    }

    private synchronized void connectToServer() {

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
                    try {
                        throw e;
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }).start();
    }

    public static Activity_SocketManager getInstance() {
        if (instance == null) {
            instance = new Activity_SocketManager();
        }
        return instance;
    }

    public void send(final String message){
        new Thread(() -> {
            try {
                if (outputStream != null) {
                    outputStream.write(message.getBytes());
                    outputStream.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    throw e;
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }).start();
    }

    public String receive() {
        try {
            InputStream inputStream = socket.getInputStream(); // Supponiamo che tu abbia un oggetto Socket
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader in = new BufferedReader(inputStreamReader);

            String receivedData = in.readLine();
            if (receivedData != null) {
                receivedData = receivedData.trim().replaceAll("\\n$", "");
            }
            Log.d("ciao","try " + receivedData);
            return receivedData;
        } catch (IOException e) {
            e.printStackTrace();
            try {
                throw e;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public boolean isConnected() {

        return socket != null && socket.isConnected();
    }

    public void close() {
        new Thread(() -> {
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
        }).start();
    }
}