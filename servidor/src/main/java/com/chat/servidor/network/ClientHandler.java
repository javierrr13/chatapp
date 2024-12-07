package com.chat.servidor.network;

import com.chat.servidor.dao.ConversationDAO;
import com.chat.servidor.dao.MessageDAO;
import com.chat.servidor.dao.UserDAO;
import com.chat.servidor.dao.UserDAOImpl;
import com.chat.servidor.util.ServerState;
import com.chat.shared.User;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
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
        	output = new ObjectOutputStream(clientSocket.getOutputStream());
            input = new ObjectInputStream(clientSocket.getInputStream());
            
            
            boolean authenticated = false;

            // Manejo de autenticaci�n o registro
            while (!authenticated) {
                String command = (String) input.readObject();
                if ("LOGIN".equalsIgnoreCase(command)) {
                    authenticated = authenticate();
                } else if ("REGISTER".equalsIgnoreCase(command)) {
                    registerUser();
                } else {
                    sendMessage("Error: Debes iniciar sesi�n o registrarte primero.");
                }
            }

            // Procesar comandos una vez autenticado
            
            processClientRequests();

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error en la conexi�n con el cliente: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void processClientRequests() {
        try {
            String command;
            while ((command = (String) input.readObject()) != null) {
                if (!isAuthenticated()) {
                    sendMessage("Error: No est�s autenticado.");
                    continue;
                }

                if (command.startsWith("LOGOUT")) {
                    handleLogout();
                    break;
                }

                System.out.println("Comando recibido de " + loggedInUser.getUsername() + ": " + command);

                try {
                    // Procesar comandos usando el CommandProcessor
                	
                	Map<Socket,User> connectedClients = ServerState.getConnectedClients();
                	System.out.println("Clientes conectados: ");
                	connectedClients.values().stream()
                    .map(user -> String.format("ID=%d, Username=%s", user.getId(), user.getUsername()))
                    .forEach(System.out::println);

                    commandProcessor.processCommand(command, loggedInUser, input, output);
                } catch (Exception e) {
                    sendMessage("Error: No se pudo procesar el comando.");
                    e.printStackTrace();
                }
            }
        } catch (EOFException e) {
            System.out.println("Cliente desconectado: " + loggedInUser.getUsername());
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

                    // Registrar al usuario en el estado del servidor
                    ServerState.addClient(clientSocket, loggedInUser);

                    sendMessage("LOG_SUCCESS");
                    System.out.println("Usuario autenticado: " + loggedInUser.getUsername());
                    return true;
                }
            }
            sendMessage("LOG_FAILS");
        } catch (Exception e) {
            sendMessage("Error: Ocurri� un problema durante la autenticaci�n.");
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
                sendMessage("Registro exitoso. Ahora puedes iniciar sesi�n.");
            } else {
                sendMessage("Error: No se pudo completar el registro. El usuario ya existe.");
            }
        } catch (Exception e) {
            sendMessage("Error: Ocurri� un problema durante el registro.");
            e.printStackTrace();
        }
    }

    private void handleLogout() throws IOException {
        System.out.println("El usuario " + loggedInUser.getUsername() + " cerr� sesi�n.");
        sendMessage("LOGOUT_SUCCESS");
        System.out.println(ServerState.getConnectedClients());
        ServerState.removeClient(clientSocket);
        System.out.println(ServerState.getConnectedClients());
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