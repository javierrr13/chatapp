package com.chat.cliente.model;

import com.chat.servidor.util.MyObjectOutputStream;
import com.chat.shared.Conversation;
import com.chat.shared.Message;
import com.chat.shared.UserProfileModel;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class UserModel {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private volatile boolean listening = true;
    
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
            // Enviar solicitud de logout al servidor
            if (socket != null && output != null) {
                output.writeObject("LOGOUT");
                output.flush();
            }

            // Detener la escucha
            stopListening();

            // Cerrar streams y socket
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            System.out.println("Cliente cerrado correctamente.");
            return true;
        } catch (IOException e) {
            System.err.println("Error al cerrar cliente: " + e.getMessage());
            return false;
        }
    }
    public void stopListening() {
        listening = false; // Detener el bucle en el hilo
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close(); // Cerrar el socket para detener la lectura
            }
        } catch (IOException e) {
            System.err.println("Error al cerrar el socket: " + e.getMessage());
        }
        System.out.println("Escucha detenida.");
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

            // Manejo de mensajes espec�ficos
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
        // Verificar si ya existe un perfil para el usuario
        UserProfileModel existingProfile = getUserProfile();

        if (existingProfile != null) {
            // Si existe, enviar el comando para actualizar el perfil
            output.writeObject("UPDATE_PROFILE");
            output.writeObject(profile);
            String response = (String) input.readObject();
            return "PROFILE_UPDATED_SUCCESSFULLY".equals(response);
        } else {
            // Si no existe, enviar el comando para insertar el perfil
            output.writeObject("INSERT_PROFILE");
            output.writeObject(profile);
            String response = (String) input.readObject();
            return "PROFILE_CREATED_SUCCESSFULLY".equals(response);
        }
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
     * Obtener mensajes de una conversaci�n.
     */
    public List<Message> getMessages(int conversationId) throws IOException, ClassNotFoundException {
        synchronized (output) {
            output.writeObject("GET_MESSAGES " + conversationId);
            output.reset(); // Limpia referencias persistentes

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
    }

    public void sendMessage(int conversationId, String content) throws IOException {
        synchronized (output) {
            output.writeObject("SEND_MESSAGE " + conversationId + "," + content);
            output.reset(); // Limpia referencias persistentes
        }
    }


    public void listenForMessages(Map<Integer, JTextArea> chatAreas) {
        new Thread(() -> {
            System.out.println("Hilo escucha invocado");
            try {
                while (listening && !socket.isClosed() && socket.isConnected()) {
                    Object response;
                    synchronized (input) {
                        response = input.readObject(); // Leer objeto del flujo
                    }

                    if (response instanceof Message message) {
                        int conversationId = message.getConversationId();
                        String formattedMessage = String.format(
                            "[%s] Usuario %d: %s",
                            message.getSentAt().toLocalTime(),
                            message.getUserId(),
                            message.getContent()
                        );

                        SwingUtilities.invokeLater(() -> {
                            JTextArea chatArea = chatAreas.get(conversationId);
                            if (chatArea != null) {
                                chatArea.append(formattedMessage + "\n");
                            } else {
                                System.err.println("No chat area found for conversation ID: " + conversationId);
                            }
                        });
                    } else if (response instanceof String) {
                        System.out.println("Mensaje del servidor: " + response);
                    } else {
                        System.err.println("Tipo inesperado recibido: " + response.getClass().getName());
                    }
                }
            } catch (StreamCorruptedException e) {
                System.err.println(e.getMessage());
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error en el hilo de escucha: " + e.getMessage());
            } finally {
                System.out.println("Hilo de escucha terminado.");
                listening = false; // Asegurarse de que la variable est� desactivada
            }
        }).start();
    }

}
