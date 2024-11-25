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
                    List<Conversation> conversations = conversationService.getAllConversations();
                    output.writeObject(conversations);
                } catch (SQLException e) {
                    e.printStackTrace();
                    output.writeObject(new ArrayList<>());
                }
                break;

            case "GET_PROFILE":
                handleGetProfile(user, input, output);
                break;

            case "INSERT_PROFILE":
                handleInsertProfile(user, input, output);
                break;

            case "UPDATE_PROFILE":
                handleUpdateProfile(user, input, output);
                break;

            default:
                output.writeObject("Comando no reconocido: " + action);
        }
    }

    private void handleGetProfile(User user, ObjectInputStream input, ObjectOutputStream output) {
        try {
            UserDAOImpl userDAO = new UserDAOImpl();
            UserProfileModel profile = userDAO.getUserProfile(user.getId());

            if (profile != null) {
                output.writeObject(profile);
                System.out.println("Perfil enviado: " + profile);
            } else {
                System.out.println("El usuario no tiene un perfil creado. Solicitando datos para creación.");
                output.writeObject("NO_PROFILE_FOUND");
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                output.writeObject("ERROR: No se pudo obtener el perfil del usuario.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleInsertProfile(User user, ObjectInputStream input, ObjectOutputStream output) {
        try {
            UserProfileModel newProfile = (UserProfileModel) input.readObject();
            UserDAOImpl userDAO = new UserDAOImpl();

            if (userDAO.insertUserProfile(user.getId(), newProfile)) {
                output.writeObject("PROFILE_CREATED_SUCCESSFULLY");
                System.out.println("Perfil creado exitosamente: " + newProfile);
            } else {
                output.writeObject("ERROR: No se pudo crear el perfil.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                output.writeObject("ERROR: Fallo en la creación del perfil.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleUpdateProfile(User user, ObjectInputStream input, ObjectOutputStream output) {
        try {
            UserProfileModel updatedProfile = (UserProfileModel) input.readObject();
            UserDAOImpl userDAO = new UserDAOImpl();

            if (userDAO.updateUserProfile(user.getId(), updatedProfile)) {
                output.writeObject("PROFILE_UPDATED_SUCCESSFULLY");
                System.out.println("Perfil actualizado: " + updatedProfile);
            } else {
                output.writeObject("ERROR: No se pudo actualizar el perfil.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                output.writeObject("ERROR: No se pudo procesar la actualización del perfil.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
