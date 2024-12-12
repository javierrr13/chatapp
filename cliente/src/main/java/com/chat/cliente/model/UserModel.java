package com.chat.cliente.model;


import com.chat.shared.Conversation;
import com.chat.shared.Message;
import com.chat.shared.UserProfileModel;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class UserModel {


	private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private volatile boolean isClosing = false;
    private Thread listenerThread;
    private  Map<String, JTextArea> chatAreas;
    private Thread listeningThread;
    private Boolean listening = false;
    public UserModel(String serverHost, int serverPort) throws IOException {
        this.socket = new Socket(serverHost, serverPort);
        this.output = new ObjectOutputStream(socket.getOutputStream());
        this.input = new ObjectInputStream(socket.getInputStream());

        
    }
    public Socket getSocket() {
		return socket;
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
    public synchronized void listenForMessages(Map<Integer, JTextArea> chatAreas) {
        if (listeningThread == null || !listeningThread.isAlive()) {
            listening = true;
            listeningThread = new Thread(() -> {
                System.out.println("Hilo de escucha iniciado: " + Thread.currentThread().getName());
                try {
                    while (listening) {
                        try {
                            // Verificar si el hilo fue interrumpido antes de bloquear
                            if (Thread.currentThread().isInterrupted()) {
                                throw new InterruptedException();
                            }

                            // Leer un mensaje del servidor
                            Object response = input.readObject(); // Operación bloqueante

                            if (response instanceof Message message) {
                                SwingUtilities.invokeLater(() -> {
                                    JTextArea chatArea = chatAreas.get(message.getConversationId());
                                    if (chatArea != null) {
                                        chatArea.append(String.format("[%s] Usuario %d: %s\n",
                                            message.getSentAt().toLocalTime(),
                                            message.getUserId(),
                                            message.getContent()));
                                    }
                                });
                            }
                        } catch (InterruptedException e) {
                            System.out.println("Hilo de escucha interrumpido: " + Thread.currentThread().getName());
                            break; // Salir del bucle si el hilo fue interrumpido
                        } catch (IOException | ClassNotFoundException e) {
                            if (!listening) {
                                System.out.println("Hilo de escucha detenido manualmente.");
                            } else {
                                System.err.println("Error en el hilo de escucha: " + e.getMessage());
                            }
                            break; // Salir del bucle si hay un error
                        }
                    }
                } finally {
                    System.out.println("Finalizando hilo de escucha: " + Thread.currentThread().getName());
                }
            }, "ListenerThread");

            listeningThread.start();
        } else {
            System.out.println("El hilo de escucha ya está activo.");
        }
    }

    public synchronized void stopListening() {
        if (listeningThread != null && listeningThread.isAlive()) {
            System.out.println("Deteniendo el hilo de escucha: " + listeningThread.getName());
            
            listening = false; // Cambia la bandera de control
            listeningThread.stop();// Interrumpir el hilo
            System.out.println("Hilo de escucha marcado para detenerse.");
        } else {
            System.out.println("No hay hilo de escucha activo para detener.");
        }
    }


    public UserProfileModel getUserProfile() throws IOException, ClassNotFoundException {
        if (socket == null || socket.isClosed()) {
            throw new IOException("Socket cerrado, no se puede obtener el perfil.");
        }
        

        output.writeObject("GET_PROFILE");
        output.reset();
        output.flush();

        Object response = input.readObject();
        if (response instanceof UserProfileModel) {
            return (UserProfileModel) response;
        } else {
            throw new IOException("Respuesta inesperada del servidor.");
        }
    }


    public boolean saveUserProfile(UserProfileModel profile) throws IOException, ClassNotFoundException {

        UserProfileModel existingProfile = getUserProfile();

        if (existingProfile != null) {

            output.writeObject("UPDATE_PROFILE");
            output.writeObject(profile);
            String response = (String) input.readObject();
            return "PROFILE_UPDATED_SUCCESSFULLY".equals(response);
        } else {

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

    public List<Message> getMessages(int conversationId) throws IOException, ClassNotFoundException {
        synchronized (output) {
            output.writeObject("GET_MESSAGES " + conversationId);
            output.reset();

            Object response = input.readObject();
            if (response instanceof List<?>) {
                List<?> rawList = (List<?>) response;
                List<Message> messages = new ArrayList<>();

                for (Object obj : rawList) {
                    if (obj instanceof Message) {
                        messages.add((Message) obj);
                    }
                }
                System.out.println("Mensajes recibidos: " + messages);
                return messages;
            } else {
                throw new IOException("Unexpected response from server while fetching messages.");
            }
        }
    }

 public void sendMessage(int conversationId, String content) {
	    try {
	        synchronized (output) {
	            output.writeObject("SEND_MESSAGE " + conversationId + "," + content);
	            output.reset();
	            output.flush();
	            
	            System.out.println("Mensaje enviado: " + content);
	        }
	    } catch (IOException e) {
	        System.err.println("Error al enviar mensaje: " + e.getMessage());
	    }
	}


 public boolean createConversation(String conversationName, List<Integer> userIds) {
	    try {
	        // Preparar el mensaje que se enviará al servidor
	    	System.out.println("cliente -> CREATE_CONVERSATION");
	        output.writeObject("CREATE_CONVERSATION");
	        output.writeObject(conversationName);
	        output.writeObject(userIds);
	        output.flush();

	        // Leer la respuesta del servidor
	        String response = (String) input.readObject();
	        return "CONVERSATION_CREATED_SUCCESSFULLY".equals(response);
	    } catch (IOException | ClassNotFoundException e) {
	        System.err.println("Error al crear la conversación: " + e.getMessage());
	        return false;
	    }
	}

 public boolean logout() {
	    try {
	        if (!isClosing) {
	            synchronized (output) {
	                output.writeObject("LOGOUT");
	                output.flush();
	            }
	        }
	        closeResources();

	        System.out.println("Cliente cerrado correctamente.");
	        return true;
	    } catch (IOException e) {
	        System.err.println("Error al cerrar el cliente: " + e.getMessage());
	        return false;
	    }
	}

	public ObjectInputStream getInput() {
	return input;
}
public void setInput(ObjectInputStream input) {
	this.input = input;
}
	private void closeResources() {
	    try {
	        if (input != null) input.close();
	        if (output != null) output.close();
	        if (socket != null && !socket.isClosed()) socket.close();
	    } catch (IOException e) {
	        System.err.println("Error al cerrar recursos: " + e.getMessage());
	    }
	
	}


}
