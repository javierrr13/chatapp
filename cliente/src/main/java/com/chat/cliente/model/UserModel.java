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
    private volatile boolean listening = false;
    private volatile boolean isClosing = false;
    private final BlockingQueue<Object> messageQueue = new LinkedBlockingQueue<>();
    private Thread listeningThread; // Variable para el hilo de escucha

    public UserModel(String serverHost, int serverPort) throws IOException {
        this.socket = new Socket(serverHost, serverPort);
        this.output = new ObjectOutputStream(socket.getOutputStream());
        this.input = new ObjectInputStream(socket.getInputStream());
        startMessageSender();
        
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

    public UserProfileModel getUserProfile() throws IOException, ClassNotFoundException {
        if (socket == null || socket.isClosed()) {
            throw new IOException("Socket cerrado, no se puede obtener el perfil.");
        }
        
        // Enviar solicitud al servidor
        output.writeObject("GET_PROFILE");
        output.flush();

        // Leer la respuesta del servidor
        Object response = input.readObject();
        if (response instanceof UserProfileModel) {
            return (UserProfileModel) response;
        } else {
            throw new IOException("Respuesta inesperada del servidor.");
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
    public synchronized void listenForMessages(Map<Integer, JTextArea> chatAreas) {
        if (listeningThread == null || !listeningThread.isAlive()) {
            // Código del hilo aquí
        	if (listeningThread == null || !listeningThread.isAlive()) {
                listening = true;
                listeningThread = new Thread(() -> {
                    System.out.println("Hilo de escucha iniciado.");
                    try {
                        while (listening && !socket.isClosed()) {
                            try {
                                Object response = input.readObject(); // Bloqueante

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
                            } catch (EOFException e) {
                                System.err.println("Fin del flujo de entrada. Deteniendo escucha...");
                                break; // Salir del bucle si el flujo termina
                            } catch (SocketException e) {
                                System.err.println("El servidor cerró la conexión: " + e.getMessage());
                                break;
                            } catch (IOException | ClassNotFoundException e) {
                                System.err.println("Error en el hilo de escucha: " + e.getMessage());
                                break;
                            }
                        }
                    } finally {
                        System.out.println("Finalizando hilo de escucha...");
                        listening = false;
                    }
                }, "ListenerThread");

                listeningThread.start();
            } else {
                System.out.println("El hilo de escucha ya está activo.");
            }
        }
    }




 public void startMessageSender() {
     new Thread(() -> {
         System.out.println("Hilo de escritura iniciado...");
         while (true) {
             try {
                 Object message = messageQueue.take(); // Toma un mensaje de la cola
                 synchronized (output) {
                     output.writeObject(message);
                     output.reset();
                     output.flush();
             // Limpia referencias persistentes
                 }
                 System.out.println("Mensaje enviado: " + message);
             } catch (IOException | InterruptedException e) {
                 System.err.println("Error en el hilo de escritura: " + e.getMessage());
                 break;
             }
         }
     }, "MessageSenderThread").start();
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

 public void stopListening() {
	    if (listeningThread != null && listeningThread.isAlive()) {
	        System.out.println("Deteniendo el hilo de escucha...");
	        listening = false; // Detiene la bandera

	        try {
	            if (socket != null && !socket.isClosed()) {
	                socket.close(); // Cierra el socket para desbloquear readObject
	            }
	        } catch (IOException e) {
	            System.err.println("Error al cerrar el socket: " + e.getMessage());
	        }

	        try {
	            listeningThread.join(1000); // Espera hasta 1 segundo para que el hilo termine
	        } catch (InterruptedException e) {
	            System.err.println("Error al esperar que el hilo termine: " + e.getMessage());
	            Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
	        }

	        listeningThread = null; // Limpia la referencia del hilo
	        System.out.println("Hilo de escucha detenido correctamente.");
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

	        stopListening(); // Detiene la escucha antes de cerrar recursos
	        closeResources();

	        System.out.println("Cliente cerrado correctamente.");
	        return true;
	    } catch (IOException e) {
	        System.err.println("Error al cerrar el cliente: " + e.getMessage());
	        return false;
	    }
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
