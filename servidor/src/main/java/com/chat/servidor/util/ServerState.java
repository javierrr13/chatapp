package com.chat.servidor.util;

import com.chat.servidor.model.User;

import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServerState {
    private static final Map<Socket, User> connectedClients = Collections.synchronizedMap(new HashMap<>());

    public static void addClient(Socket client, User user) {
        connectedClients.put(client, user);
    }

    public static void removeClient(Socket client) {
        connectedClients.remove(client);
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
}
