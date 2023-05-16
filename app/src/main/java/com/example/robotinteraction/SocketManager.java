package com.example.robotinteraction;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketManager {
    private String serverIp;
    private int serverPort;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private static SocketManager instance = null;

    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    private SocketManager() {
        // Nessun costruttore esterno necessario
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


    public String receiveMessage(){

        String line = null;

        try {
            line = in.readLine();
        }catch (IOException e){
            e.printStackTrace();
        }

        return line;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public void close() throws IOException {
        socket.close();
        instance = null;
    }
}
