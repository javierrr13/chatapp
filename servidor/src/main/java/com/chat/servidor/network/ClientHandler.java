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
    private Socket clientSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private User loggedInUser;
    private UserDAO userDAO;
    private ConversationDAO conversationDAO;
    private MessageDAO messageDAO;
    private CommandProcessor commandProcessor;
    
    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.userDAO = new UserDAOImpl();
        this.conversationDAO = new ConversationDAO();
        this.messageDAO = new MessageDAO();
        this.commandProcessor=new CommandProcessor(conversationDAO, messageDAO);
    }

    @Override
    public void run() {
        try {
            input = new ObjectInputStream(clientSocket.getInputStream());
            output = new ObjectOutputStream(clientSocket.getOutputStream());

            boolean authenticated = false;

            while (!authenticated) {
                String command = (String) input.readObject();
                if ("LOGIN".equalsIgnoreCase(command)) {
                    authenticated = authenticate();
                    if (authenticated) {
                        sendMessage("LOG_SUCCESS");
                        System.out.println("Conectado " + loggedInUser.getUsername());
                        ServerState.addClient(clientSocket, loggedInUser); // Agregar cliente autenticado a ServerState
                    } else {
                        sendMessage("LOG_FAILS");
                    }
                } else if ("REGISTER".equalsIgnoreCase(command)) {
                    registerUser();
                } else {
                    sendMessage("Comando no válido. Usa LOGIN o REGISTER.");
                }
            }

           

            String message;
            while ((message = (String) input.readObject()) != null) {
                if (message.startsWith("LOGOUT")) {
                    handleLogout();
                    break; // Salir del bucle y cerrar conexión
                }
                System.out.println("Mensaje recibido de " + loggedInUser.getUsername() + ": " + message);
                try {
                    commandProcessor.processCommand(message, loggedInUser, input, output);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error en la conexión con el cliente: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void handleLogout() throws IOException {
        System.out.println("El usuario " + loggedInUser.getUsername() + " cerró sesión.");
        sendMessage("LOGOUT_SUCCESS"); // Notificar al cliente que el logout exitoso ---------------------------------
        ServerState.removeClient(clientSocket); // Eliminar cliente del estado del servidor
        loggedInUser = null; // Limpiar el estado del usuario autenticado
    }


    private boolean authenticate() throws IOException, ClassNotFoundException {
        String username = (String) input.readObject();
        String password = (String) input.readObject();

        try {
            if (userDAO.authenticateUser(username, password)) {
                Optional<User> userOpt = userDAO.getUserByUsername(username);
                if (userOpt.isPresent()) {
                    this.loggedInUser = userOpt.get();
                    return true;
                } else {
                    sendMessage("Error: Usuario no encontrado.");
                }
            } else {
                sendMessage("Error: Credenciales incorrectas.");
            }
        } catch (Exception e) {
            sendMessage("Error: Ocurrió un problema al autenticar.");
            e.printStackTrace();
        }
        return false;
    }


    private void registerUser() throws IOException, ClassNotFoundException {
        sendMessage("Por favor, ingresa un nombre de usuario:");
        String username = (String) input.readObject();
        sendMessage("Por favor, ingresa un email:");
        String email = (String) input.readObject();
        sendMessage("Por favor, ingresa una contraseña:");
        String password = (String) input.readObject();

        try {
            User newUser = new User(username, email, password);
            if (userDAO.registerUser(newUser)) {
                sendMessage("Registro exitoso. Ahora puedes iniciar sesión.");
            } else {
                sendMessage("Error: No se pudo completar el registro. El usuario ya existe.");
            }
        } catch (Exception e) {
            sendMessage("Error: Ocurrió un problema al registrar el usuario.");
            e.printStackTrace();
        }
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
