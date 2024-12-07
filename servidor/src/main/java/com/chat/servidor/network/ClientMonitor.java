package com.chat.servidor.network;

import com.chat.servidor.util.ServerState;
import com.chat.shared.User;

import java.net.Socket;
import java.util.Set;

public class ClientMonitor extends Thread {

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(5000); // Espera de 5 segundos

                Set<Socket> connectedSockets = ServerState.getConnectedSockets();
                System.out.println("Clientes conectados: " + connectedSockets.size());

                for (Socket client : connectedSockets) {
                    User user = ServerState.getUserBySocket(client);
                    if (user != null) {
                        System.out.println("Cliente: " + user.getUsername() + " (" + client.getInetAddress() + ":" + client.getPort() + ")");
                    } else {
                        System.out.println("Cliente desconocido: " + client.getInetAddress() + ":" + client.getPort());
                    }
                }

            } catch (InterruptedException e) {
                System.err.println("Error en el monitor de clientes: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }
}
