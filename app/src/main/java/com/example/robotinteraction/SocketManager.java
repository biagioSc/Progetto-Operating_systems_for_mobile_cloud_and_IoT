package com.example.robotinteraction;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import android.util.Log;

public class SocketManager {
    private String serverIp;
    private int serverPort;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private static SocketManager instance = null;

    private MessageListener messageListener;

    private static final String SERVER_IP = "00.00.00";
    private static final int SERVER_PORT = 0000;

    private SocketManager() {
        // Nessun costruttore esterno necessario
    }

    public interface MessageListener {
        void onNewMessage(String message);
    }

    public void setMessageListener(MessageListener listener){
        this.messageListener = listener;
    }

    public void connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int attempts = 0;
                final int MAX_ATTEMPTS = 3;

                while (attempts < MAX_ATTEMPTS) {
                    try {
                        socket = new Socket(SERVER_IP, SERVER_PORT);
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                        // Se la connessione è riuscita, esce dal ciclo
                        break;
                    } catch (IOException e) {
                        attempts++;
                        e.printStackTrace();
                        Log.e("SocketManager", "Failed to connect. Attempt " + attempts + " of " + MAX_ATTEMPTS);

                        // Se non è riuscito a connettersi, aspetta 5 secondi prima del prossimo tentativo
                        try {
                            Thread.sleep(5000); // 5000 milliseconds = 5 seconds
                        } catch (InterruptedException ie) {
                            // Thread was interrupted during sleep, this is usually a sign to stop
                            Log.e("SocketManager", "Connection retry interrupted: ", ie);
                            return;
                        }

                        // Se ha raggiunto il numero massimo di tentativi, lancia un'eccezione
                        if (attempts == MAX_ATTEMPTS) {
                            throw new RuntimeException("Failed to connect to the server after " + MAX_ATTEMPTS + " attempts");
                        }
                    }
                }
            }
        }).start();
    }




    public static synchronized SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
            instance.connect();
        }
        return instance;
    }


    public String receiveMessage() throws IOException {

        if(in == null){
            throw new IOException("Ho ricevuto un input null dal Server!");
        }
        String line = in.readLine();
        if (messageListener != null) {
            messageListener.onNewMessage(line);
        }
        return line;
    }

    public void sendMessage(String message) throws IOException {
        if (out == null) {
            throw new IOException("Il messaggio da inviare è null!");
        }

        out.println(message);

        // Se ci sono stati errori nella scrittura, PrintWriter non lancerà un'eccezione,
        // ma potrai comunque controllarlo con checkError().
        if (out.checkError()) {
            throw new IOException("Si è verificato un errore durante l'invio del messaggio!");
        }
    }


    public void close() {
        try {
            socket.close();
            instance = null;
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}
