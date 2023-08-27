package com.example.robotinteraction;

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

    private Socket_Manager() {
        connectToServer();
    }

    private synchronized void connectToServer() {
        new Thread(() -> {
            try {
                Log.d(TAG, "Connecting to the server...");
                socket = new Socket(SERVER_IP, SERVER_PORT);
                Log.d(TAG, "Connected to the server.");
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outputStream = socket.getOutputStream();
                isConnected = true;
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error during connection: " + e.getMessage());
                isConnected = false;
            }
        }).start();
    }

    public static Socket_Manager getInstance() {
        if (instance == null) {
            instance = new Socket_Manager();
            if (!instance.isConnected) { // se la connessione fallisce
                instance = null;
            }
        }
        return instance;
    }

    public void send(final String message) {
        Log.d(TAG, "Sending message: " + message);
        new Thread(() -> {
            try {
                if (outputStream != null) {
                    outputStream.write(message.getBytes());
                    outputStream.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }).start();
    }
    public String receive() {
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
            throw new RuntimeException(e);
        }
    }

    public boolean isConnected() {
        return isConnected;
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
