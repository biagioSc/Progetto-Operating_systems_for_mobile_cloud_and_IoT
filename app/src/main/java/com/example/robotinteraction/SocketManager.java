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

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

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
                try {
                    socket = new Socket(SERVER_IP, SERVER_PORT);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                } catch (IOException e) {
                    e.printStackTrace();
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
        String line = in.readLine();
        if (messageListener != null) {
            messageListener.onNewMessage(line);
        }
        return line;
    }

    public void sendMessage(String message) throws IOException{
        final int MAX_RETRIES = 3;  // Numero di tentativi
        int attempt = 0;

        while (attempt < MAX_RETRIES) {
            out.println(message);
            if (!out.checkError()) {
                // Il messaggio è stato inviato con successo, quindi esco dal ciclo
                break;
            } else {
                // C'è stato un errore nell'invio del messaggio, stampo i log di errore
                attempt++;
                Log.e("SocketManager", "Errore durante l'invio del messaggio. Tentativo "
                        + attempt + " di " + MAX_RETRIES);
            }
        }

        // Raggiunti i tentativi massimi
        if (attempt == MAX_RETRIES) {
            Log.e("SocketManager", "Non è stato possibile inviare il messaggio dopo "
                    + MAX_RETRIES + " tentativi.");
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
