package com.chat.servidor;

import com.chat.servidor.network.Server;

public class ServerMain {
    public static void main(String[] args) {
        int port = 12345; // Puerto para el servidor

        System.out.println("Iniciando el servidor en el puerto " + port + "...");

        try {
            Server server = new Server(port);
            server.start(); // Inicia el servidor y espera conexiones de clientes
        } catch (Exception e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
