// Classe SocketManager
package com.example.robotinteraction;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketManager {
    private String serverIp;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int serverPort;
    private static SocketManager instance = null;

    private MessageListener messageListener;

    private static final String SERVER_IP = "195.231.38.118";
    private static final int SERVER_PORT = 8080;

    private SocketManager() {
        // Nessun costruttore esterno necessario
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(SERVER_IP, SERVER_PORT);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("SocketManager", "Failed to connect");
                }
            }
        }).start();
    }

    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
            instance.connect();
        }
        else
        {
            Log.d("SocketManager", "[Server] Connessione recuperata correttamente  ");
        }
        return instance;
    }

    public String receiveMessage() {
        try {
            String receivedData = in.readLine();
            if (receivedData != null) {
                receivedData = receivedData.trim().replaceAll("\\n$", "");
                notifyMessageReceived(receivedData);
            }
            Log.d("SocketManager", "[Server] Ho ricevuto questo messaggio: " + receivedData);
            return receivedData;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void sendMessage(String message) throws IOException {
        if (out == null) {
            throw new IOException("Il messaggio da inviare è nullo!");
        }
        out.flush();
        try {
            Thread.sleep(300); // Aggiungi un ritardo di 300 millisecondi tra ogni invio
            out.println(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d("SocketManager", "[CLIENT] Messaggio inviato: " + message);

        if (out.checkError()) {
            throw new IOException("Errore durante l'invio del messaggio.");
        }
    }

    public void close() {
        try {
            socket.close();
            instance = null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void attemptConnection() {
        if (!isConnected()) {
            connect();
        }
    }

    private void notifyMessageReceived(String message) {
        if (messageListener != null) {
            messageListener.onMessageReceived(message);
        }
    }

    public interface MessageListener {
        void onMessageReceived(String message);
    }
}
