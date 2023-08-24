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
    private static final String TAG = "Activity_SocketManager";
    private static final String SERVER_IP = "195.231.38.118";
    private static final int SERVER_PORT = 8080;
    private Socket socket;
    private BufferedReader reader;
    private OutputStream outputStream;
    private static Activity_SocketManager instance;
    private boolean isConnected = false;

    private Activity_SocketManager() {
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
                if (e instanceof ConnectException) {
                    Log.e(TAG, "The server is not active or not reachable.");
                } else if (e instanceof SocketTimeoutException) {
                    Log.e(TAG, "Connection timeout reached.");
                }
                throw new RuntimeException(e);
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
        return socket != null && socket.isConnected();
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
