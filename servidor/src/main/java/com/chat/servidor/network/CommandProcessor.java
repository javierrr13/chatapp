package com.chat.servidor.network;

import com.chat.servidor.dao.ConversationDAO;
import com.chat.servidor.dao.MessageDAO;
import com.chat.servidor.dao.UserDAOImpl;
import com.chat.servidor.model.Conversation;
import com.chat.servidor.model.User;
import com.chat.shared.UserProfileModel;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommandProcessor {
    private final ConversationDAO conversationService;
    private final MessageDAO messageService;

    public CommandProcessor(ConversationDAO conversationService, MessageDAO messageService) {
        this.conversationService = conversationService;
        this.messageService = messageService;
    }

    /**
     * Procesa el comando enviado por el cliente.
     *
     * @param command El comando a procesar.
     * @param user    El usuario que envió el comando.
     * @param input   El flujo de entrada del cliente.
     * @param output  El flujo de salida hacia el cliente.
     * @throws Exception Si ocurre algún error durante el procesamiento.
     */
    public void processCommand(String command, User user, ObjectInputStream input, ObjectOutputStream output) throws Exception {
        String[] parts = command.split(" ", 2);
        String action = parts[0].toUpperCase();

        switch (action) {
            case "CREATE_CONVERSATION":
                if (parts.length > 1) {
                    String[] createParams = parts[1].split(",", 2);
                    if (createParams.length == 2) {
                        String conversationName = createParams[0].trim();
                        int statusId = Integer.parseInt(createParams[1].trim());
                        conversationService.createConversation(conversationName, statusId);
                    } else {
                        output.writeObject("Error: Faltan parámetros para CREATE_CONVERSATION. Formato esperado: nombre,statusId.");
                    }
                } else {
                    output.writeObject("Error: Faltan parámetros para CREATE_CONVERSATION.");
                }
                break;

            case "SEND_MESSAGE":
                if (parts.length > 1) {
                    // Espera un formato: conversationID,userID,content
                    String[] messageParams = parts[1].split(",", 3);
                    if (messageParams.length == 3) {
                        int conversationID = Integer.parseInt(messageParams[0].trim());
                        int userID = Integer.parseInt(messageParams[1].trim());
                        String content = messageParams[2].trim();
                        messageService.addMessage(conversationID, userID, content);
                        output.writeObject("Mensaje enviado con éxito.");
                    } else {
                        output.writeObject("Error: Faltan parámetros para SEND_MESSAGE. Formato esperado: conversationID,userID,content.");
                    }
                } else {
                    output.writeObject("Error: Faltan parámetros para SEND_MESSAGE.");
                }
                break;

            case "LIST_CONVERSATIONS":
                try {
                    List<Conversation> conversations = conversationService.getAllConversations(); // Obtén las conversaciones
                    output.writeObject(conversations); // Envíalas al cliente
                    for (Conversation conversation : conversations) {
                        System.out.println(conversation); // Llama al método toString() automáticamente
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Envía un mensaje de error al cliente, si es necesario
                    output.writeObject(new ArrayList<>()); // Envía una lista vacía en caso de error
                }
                break;
                
            case "GET_PROFILE":
                try {
                    if (user != null) {
                        UserDAOImpl userDAO = new UserDAOImpl();
                        UserProfileModel profile = userDAO.getUserProfile(user.getId());
                        
                        if (profile != null) {
                        	
                            output.writeObject(profile);
                            System.out.println(profile.toString());// Enviar objeto UserProfile
                        } else {
                            output.writeObject("ERROR: No se encontró el perfil."); // Enviar mensaje de error
                        }
                    } else {
                        output.writeObject("ERROR: Usuario no autenticado."); // Enviar mensaje de error
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    output.writeObject("ERROR: No se pudo obtener el perfil del usuario."); // Enviar mensaje de error
                }
                break;
           

            default:
                output.writeObject("Comando no reconocido: " + action);
        }
    }
 
}
