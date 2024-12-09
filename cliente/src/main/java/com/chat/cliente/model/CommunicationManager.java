package com.chat.cliente.model;

import com.chat.shared.Message;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class CommunicationManager {
    private static CommunicationManager instance;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private volatile boolean listening = false;
    private Thread listeningThread;

    private final BlockingQueue<Object> responseQueue = new LinkedBlockingQueue<>();

    // Listener para mensajes en tiempo real
    public interface MessageListener {
        void onMessageReceived(Message message);
    }

    private CommunicationManager(String serverHost, int serverPort) throws IOException {
        this.socket = new Socket(serverHost, serverPort);
        this.output = new ObjectOutputStream(socket.getOutputStream());
        this.input = new ObjectInputStream(socket.getInputStream());
    }

    public static CommunicationManager getInstance(String serverHost, int serverPort) throws IOException {
        if (instance == null) {
            synchronized (CommunicationManager.class) {
                if (instance == null) {
                    instance = new CommunicationManager(serverHost, serverPort);
                }
            }
        }
        return instance;
    }

    public void sendMessage(String command, Object data) {
        try {
            output.writeObject(command);
            if (data != null) {
                output.writeObject(data);
            }
            output.flush();
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje: " + e.getMessage());
        }
    }

    public Object waitForResponse() throws IOException, ClassNotFoundException {
        try {
            return input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw e;
        }
    }

    public void startListening(MessageListener listener) {
        if (!listening) {
            listening = true;
            listeningThread = new Thread(() -> {
                while (listening) {
                    try {
                        Object response = input.readObject();
                        if (response instanceof Message) {
                            listener.onMessageReceived((Message) response);
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        System.err.println("Error en hilo de escucha: " + e.getMessage());
                        stopListening();
                    }
                }
            }, "ListenerThread");
            listeningThread.start();
        }
    }

    public void stopListening() {
        listening = false;
        if (listeningThread != null) {
            listeningThread.interrupt();
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar el socket: " + e.getMessage());
            }
        }
    }
}
