package com.chat.servidor.network;

import com.chat.servidor.dao.ConversationDAO;
import com.chat.servidor.dao.MessageDAO;
import com.chat.servidor.dao.UserDAO;
import com.chat.servidor.dao.UserDAOImpl;
import com.chat.servidor.model.User;
import com.chat.servidor.util.ServerState;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private User loggedInUser;
    private final UserDAO userDAO;
    private final ConversationDAO conversationDAO;
    private final MessageDAO messageDAO;
    private final CommandProcessor commandProcessor;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.userDAO = new UserDAOImpl();
        this.conversationDAO = new ConversationDAO();
        this.messageDAO = new MessageDAO();
        this.commandProcessor = new CommandProcessor(conversationDAO, messageDAO);
    }

    @Override
    public void run() {
        try {
            input = new ObjectInputStream(clientSocket.getInputStream());
            output = new ObjectOutputStream(clientSocket.getOutputStream());

            boolean authenticated = false;

            // Manejo de autenticación o registro
            while (!authenticated) {
                String command = (String) input.readObject();
                if ("LOGIN".equalsIgnoreCase(command)) {
                    authenticated = authenticate();
                } else if ("REGISTER".equalsIgnoreCase(command)) {
                    registerUser();
                } else {
                    sendMessage("Error: Debes iniciar sesión o registrarte primero.");
                }
            }

            // Procesar comandos una vez autenticado
            processClientRequests();

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error en la conexión con el cliente: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void processClientRequests() {
        try {
            String command;
            while ((command = (String) input.readObject()) != null) {
                if (!isAuthenticated()) {
                    sendMessage("Error: No estás autenticado.");
                    continue;
                }

                if (command.startsWith("LOGOUT")) {
                    handleLogout();
                    break;
                }

                System.out.println("Comando recibido de " + loggedInUser.getUsername() + ": " + command);

                try {
                    // Procesar comandos usando el CommandProcessor
                    commandProcessor.processCommand(command, loggedInUser, input, output);
                } catch (Exception e) {
                    sendMessage("Error: No se pudo procesar el comando.");
                    e.printStackTrace();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al procesar solicitudes del cliente: " + e.getMessage());
        }
    }

    private boolean authenticate() throws IOException, ClassNotFoundException {
        String username = (String) input.readObject();
        String password = (String) input.readObject();

        try {
            if (userDAO.authenticateUser(username, password)) {
                Optional<User> userOpt = userDAO.getUserByUsername(username);
                if (userOpt.isPresent()) {
                    this.loggedInUser = userOpt.get();
                    sendMessage("LOG_SUCCESS");
                    ServerState.addClient(clientSocket, loggedInUser);
                    System.out.println("Usuario autenticado: " + loggedInUser.getUsername());
                    return true;
                }
            }
            sendMessage("LOG_FAILS");
        } catch (Exception e) {
            sendMessage("Error: Ocurrió un problema durante la autenticación.");
            e.printStackTrace();
        }
        return false;
    }

    private void registerUser() throws IOException, ClassNotFoundException {
        String username = (String) input.readObject();
        String email = (String) input.readObject();
        String password = (String) input.readObject();

        try {
            User newUser = new User(username, email, password);
            if (userDAO.registerUser(newUser)) {
                sendMessage("Registro exitoso. Ahora puedes iniciar sesión.");
            } else {
                sendMessage("Error: No se pudo completar el registro. El usuario ya existe.");
            }
        } catch (Exception e) {
            sendMessage("Error: Ocurrió un problema durante el registro.");
            e.printStackTrace();
        }
    }

    private void handleLogout() throws IOException {
        System.out.println("El usuario " + loggedInUser.getUsername() + " cerró sesión.");
        sendMessage("LOGOUT_SUCCESS");
        ServerState.removeClient(clientSocket);
        loggedInUser = null;
    }

    private boolean isAuthenticated() {
        return loggedInUser != null;
    }

    private void sendMessage(String message) throws IOException {
        output.writeObject(message);
    }

    private void closeConnection() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
