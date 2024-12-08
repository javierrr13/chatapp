package com.chat.servidor.util;

import com.chat.shared.Message;
import com.chat.shared.User;
import com.chat.servidor.dao.ConversationDAO;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ServerState {
    private static final Map<Socket, User> connectedClients = Collections.synchronizedMap(new HashMap<>());
    private static final Map<Integer, Set<Socket>> conversationSockets = Collections.synchronizedMap(new HashMap<>());
    private static final Map<Socket, ObjectOutputStream> outputStreamMap = new HashMap<>();
    public static void addClient(Socket client, User user) {
        connectedClients.put(client, user);
    }


 

    public static Set<Socket> getConnectedSockets() {
        return Collections.unmodifiableSet(connectedClients.keySet());
    }

    public static Map<Socket, User> getConnectedClients() {
        return Collections.unmodifiableMap(connectedClients);
    }

    public static User getUserBySocket(Socket client) {
        return connectedClients.get(client);
    }

    public static void addSocketToConversation(int conversationId, Socket client) {
        conversationSockets.computeIfAbsent(conversationId, k -> Collections.synchronizedSet(new java.util.HashSet<>())).add(client);
    }

    public static void removeSocketFromConversation(int conversationId, Socket client) {
        if (conversationSockets.containsKey(conversationId)) {
            conversationSockets.get(conversationId).remove(client);
        }
    }
    public static Set<Socket> getClientsInConversation(int conversationId) {
        Set<Socket> clients = new HashSet<>();
        ConversationDAO conversationDAO = new ConversationDAO();

        try {
            // Obtener IDs de usuarios en la conversaci�n
            List<Integer> userIds = conversationDAO.getUserIdsInConversation(conversationId);

            // Mapear usuarios a sockets
            for (Map.Entry<Socket, User> entry : connectedClients.entrySet()) {
                User user = entry.getValue();
                if (user != null && userIds.contains(user.getId())) {
                    clients.add(entry.getKey());
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener clientes en la conversaci�n: " + e.getMessage());
        }
        System.out.println(clients);
        return clients;
    }
    public static void broadcastToConversation(int conversationId, Message message, Socket senderSocket) {
        Set<Socket> conversationClients = getClientsInConversation(conversationId);

        for (Socket client : conversationClients) {
            // Evitar enviar el mensaje de vuelta al emisor
            if (client.equals(senderSocket)) {
                continue;
            }

            try {
                // Obtener el flujo de salida correspondiente al cliente
                ObjectOutputStream clientOutput = getOutputStream(client);

                // Sincronizar el flujo de salida para evitar conflictos en la escritura simultánea
                synchronized (clientOutput) {
                    // Escribir el mensaje en el flujo de salida del cliente
                    clientOutput.writeObject(message);
                    clientOutput.reset();
                    clientOutput.flush();
                }
            } catch (IOException e) {
                System.err.println("Error al enviar mensaje a cliente: " + e.getMessage());
                removeClient(client); // Remueve cliente desconectado
            }
        }
    }


    public static Socket getSocketByUser(User user) {
        synchronized (connectedClients) {
            for (Map.Entry<Socket, User> entry : connectedClients.entrySet()) {
                if (entry.getValue().equals(user)) {
                    return entry.getKey();
                }
            }
        }
        return null; // Devuelve null si no se encuentra el socket
    }

    private static ObjectOutputStream getOutputStream(Socket client) throws IOException {
        if (!outputStreamMap.containsKey(client)) {
            // Usar MyObjectOutputStream para evitar encabezados duplicados
            ObjectOutputStream outputStream = new MyObjectOutputStream(client.getOutputStream());
            outputStreamMap.put(client, outputStream);
        }
        return outputStreamMap.get(client);
    }

    public static void removeClient(Socket client) {
        // Remover del mapa de connectedClients
        synchronized (connectedClients) {
            connectedClients.remove(client);
        }

        // Remover del mapa de outputStreamMap
        synchronized (outputStreamMap) {
            outputStreamMap.remove(client);
        }

        // Remover de todas las conversaciones
        synchronized (conversationSockets) {
            for (Set<Socket> sockets : conversationSockets.values()) {
                sockets.remove(client);
            }
        }

        // Cerrar el socket despu�s de remover todas las referencias
        try {
            client.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar socket: " + e.getMessage());
        }

        System.out.println("Cliente removido completamente: " + client);
    }



}
