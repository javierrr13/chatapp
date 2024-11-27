package com.chat.cliente.model;

import com.chat.shared.Conversation;
import com.chat.shared.Message;
import com.chat.shared.UserProfileModel;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class UserModel {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    public UserModel(String serverHost, int serverPort) throws IOException {
        this.socket = new Socket(serverHost, serverPort);
        this.output = new ObjectOutputStream(socket.getOutputStream());
        this.input = new ObjectInputStream(socket.getInputStream());
    }

    public boolean login(String username, String password) {
        try {
            output.writeObject("LOGIN");
            output.writeObject(username);
            output.writeObject(password);

            String response = (String) input.readObject();
            return response.startsWith("LOG_SUCCESS");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean logout() {
        try {
            output.writeObject("LOGOUT");
            output.flush();
            String response = (String) input.readObject();
            return "LOGOUT_SUCCESS".equals(response);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public UserProfileModel getUserProfile() throws IOException, ClassNotFoundException {
        System.out.println("Sending profile request...");
        output.writeObject("GET_PROFILE");
        Object response = input.readObject();
        System.out.println("Response received.");

        if (response instanceof UserProfileModel) {
            return (UserProfileModel) response; // Retornar el perfil si existe
        } else if (response instanceof String) {
            String serverMessage = (String) response;

            // Manejo de mensajes específicos
            if ("NO_PROFILE_FOUND".equals(serverMessage)) {
                System.out.println("No profile found for user.");
                return null; // Indicar al cliente que no hay perfil
            } else {
                System.err.println("Error from server: " + serverMessage);
                throw new IOException("Error from server: " + serverMessage);
            }
        } else {
            throw new ClassCastException("Unexpected response from server: " + response.getClass().getName());
        }
    }

    public boolean saveUserProfile(UserProfileModel profile) throws IOException, ClassNotFoundException {
        output.writeObject("SAVE_PROFILE");
        output.writeObject(profile);
        String response = (String) input.readObject();
        return "PROFILE_SAVED".equals(response);
    }

    public boolean register(String username, String email, String password) {
        try {
            output.writeObject("REGISTER");
            output.writeObject(username);
            output.writeObject(email);
            output.writeObject(password);

            String response = (String) input.readObject();
            return response.startsWith("Registro exitoso");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtener las conversaciones del usuario actual.
     */
    public List<Conversation> getConversations() throws IOException, ClassNotFoundException {
        output.writeObject("LIST_CONVERSATIONS");
        Object response = input.readObject();
        System.out.println(response);
        if (response instanceof List<?>) {
            List<?> rawList = (List<?>) response;
            List<Conversation> conversations = new ArrayList<>();
            for (Object obj : rawList) {
                if (obj instanceof Conversation) {
                    conversations.add((Conversation) obj);
                    System.out.println(rawList);
                }
            }
            return conversations;
        } else {
            throw new IOException("Unexpected response from server while fetching conversations.");
        }
    }

    /**
     * Obtener mensajes de una conversación.
     */
    public List<Message> getMessages(int conversationId) throws IOException, ClassNotFoundException {
        output.writeObject("GET_MESSAGES " + conversationId);
        Object response = input.readObject(); // Leer respuesta del servidor

        if (response instanceof List<?>) {
            List<?> rawList = (List<?>) response;
            List<Message> messages = new ArrayList<>();

            for (Object obj : rawList) {
                if (obj instanceof Message) {
                    messages.add((Message) obj); // Deserializar objetos Message
                }
            }
            System.out.println("Mensajes recibidos: " + messages);
            return messages;
        } else {
            throw new IOException("Unexpected response from server while fetching messages.");
        }
    }

    /**
     * Enviar un mensaje a una conversación.
     * @throws ClassNotFoundException 
     */
    public void sendMessage(int conversationId, String content) throws IOException, ClassNotFoundException {
        output.writeObject("SEND_MESSAGE " + conversationId + "," + content);
        String response = (String) input.readObject();
        if (!"Mensaje enviado con éxito.".equals(response)) {
            throw new IOException("Error while sending message: " + response);
        }
    }

    public void close() {
        try {
            if (output != null) output.close();
            if (input != null) input.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
