package com.example.robotinteractionOttimizzata;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class Socket_Manager {
    private static final String TAG = "Activity_SocketManager";
    private static final String SERVER_IP = "195.231.38.118";
    private static final int SERVER_PORT = 8080;
    private Socket socket;
    private BufferedReader reader;
    private OutputStream outputStream;
    private static Socket_Manager instance;
    private boolean isConnected = false;

    private ConnectionListener connectionListener;

    private Socket_Manager() {
        connectToServer();
    }

    public void setConnectionListener(ConnectionListener listener){
        this.connectionListener = listener;
    }

    private synchronized void connectToServer() {
        new Thread(() -> {
            try {
                Log.d(TAG, "Connessione al server...");
                socket = new Socket(SERVER_IP, SERVER_PORT);
                Log.d(TAG, "Connesso al server.");
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outputStream = socket.getOutputStream();
                isConnected = true;
                if (connectionListener != null) {
                    connectionListener.onConnected();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Errore durante la connessione: " + e.getMessage());
                isConnected = false;
                if (connectionListener != null) {
                    connectionListener.onConnectionFailed(e.getMessage());
                }
            }
        }).start();
    }


    public static Socket_Manager getInstance() {
        if (instance == null) {
            instance = new Socket_Manager();
        }
        return instance;
    }

    public void send(final String message) throws Exception{
        Log.d(TAG, "Sending message: " + message);
        new Thread(() -> {
            try {
                if (outputStream != null) {
                    outputStream.write(message.getBytes());
                    outputStream.flush();
                }
            } catch (Exception e) {
                // Gestisci altre eccezioni in modo generico
                e.printStackTrace();
                //throw new RuntimeException(e);
            }
        }).start();
    }

    public String receive() throws Exception {
        try {
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader in = new BufferedReader(inputStreamReader);

            String receivedData = in.readLine();

            if (receivedData != null) {
                receivedData = receivedData.trim().replaceAll("\\n$", "");
            }
            if (receivedData != null) {
                Log.d(TAG, "Received message: " + receivedData);
            }
            return receivedData;
        } catch (Exception e) {
            Log.d(TAG, "Received message: CATCH");
            e.printStackTrace();
            String receivedData = "[ERROR]";
            return receivedData;
            //throw new Exception(e);
        }
    }

    public boolean isConnected() {
        return (socket != null && !socket.isClosed());
    }

    public void close() {
        new Thread(() -> {
            try {
                if (socket != null) {
                    socket.close();
                    Log.d(TAG, "Socket closed.");
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