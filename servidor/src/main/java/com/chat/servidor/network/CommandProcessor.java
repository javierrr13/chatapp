package com.chat.servidor.network;

import com.chat.servidor.dao.ConversationDAO;
import com.chat.servidor.dao.MessageDAO;
import com.chat.servidor.dao.UserDAOImpl;
import com.chat.shared.Message;
import com.chat.shared.User;
import com.chat.servidor.util.ServerState;
import com.chat.shared.UserProfileModel;
import com.chat.shared.Conversation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
                handleCreateConversation(parts, user, output);
                break;

            case "SEND_MESSAGE":
                handleSendMessage(parts, user, output);
                break;

            case "LIST_CONVERSATIONS":
                handleListConversations(user, output);
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
            case "GET_MESSAGES":
                handleGetMessages(parts, user, output);
                break;
            default:
                output.writeObject("Comando no reconocido: " + action);
        }
    }
    private void handleGetMessages(String[] parts, User user, ObjectOutputStream output) {
        try {
            if (parts.length > 1) {
                int conversationId = Integer.parseInt(parts[1].trim());

                // Validar si el usuario pertenece a la conversaci�n
                if (conversationService.canSendMessage(user.getId(), conversationId)) {
                    // Obtener los mensajes como objetos Message
                    List<Message> messages = messageService.getMessagesByConversation(conversationId);
                    ServerState.getClientsInConversation(conversationId);
                    // Enviar directamente los objetos Message
                    output.writeObject(messages);
                    System.out.println("Mensajes enviados: " + messages);
                } else {
                    output.writeObject("ERROR: No est�s autorizado para ver los mensajes de esta conversaci�n.");
                }
            } else {
                output.writeObject("Error: Faltan par�metros para GET_MESSAGES. Formato esperado: GET_MESSAGES conversationId.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                output.writeObject("ERROR: No se pudieron obtener los mensajes.");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }



    private void handleCreateConversation(String[] parts, User user, ObjectOutputStream output) {
        try {
            if (parts.length > 1) {
                String[] createParams = parts[1].split(",", 2);
                if (createParams.length == 2) {
                    String conversationName = createParams[0].trim();
                    int statusId = Integer.parseInt(createParams[1].trim());

                    // Crear la conversaci�n y agregar al usuario como miembro
                    Conversation conversation = conversationService.createConversation(conversationName, statusId, user.getId());
                    output.writeObject("CONVERSATION_CREATED: " + conversation);
                } else {
                    output.writeObject("Error: Faltan par�metros para CREATE_CONVERSATION. Formato esperado: nombre,statusId.");
                }
            } else {
                output.writeObject("Error: Faltan par�metros para CREATE_CONVERSATION.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                output.writeObject("ERROR: No se pudo crear la conversaci�n.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handleSendMessage(String[] parts, User user, ObjectOutputStream output) {
        try {
            if (parts.length > 1) {
                String[] messageParams = parts[1].split(",", 2);
                if (messageParams.length == 2) {
                    int conversationId = Integer.parseInt(messageParams[0].trim());
                    String content = messageParams[1].trim();

                    if (conversationService.canSendMessage(user.getId(), conversationId)) {
                        // Crear un nuevo objeto Message
                        Message newMessage = new Message(0, conversationId, user.getId(), content, LocalDateTime.now());
                        
                        // Registrar el mensaje en el servicio
                        messageService.addMessage(conversationId, user.getId(), content);
                        
                        // Enviar el mensaje a todos los clientes en la conversaci�n
                        ServerState.broadcastToConversation(conversationId, newMessage);
                        
                        System.out.println(newMessage.getClass().getName());

                        // Confirmar al cliente que envi� el mensaje con �xito
                        output.writeObject(newMessage);
                        output.reset();
                        output.flush();
                    } else {
                        output.writeObject("ERROR: No est�s autorizado para enviar mensajes en esta conversaci�n.");
                        output.flush();
                    }
                } else {
                    output.writeObject("Error: Faltan par�metros para SEND_MESSAGE. Formato esperado: conversationId,content.");
                    output.flush();
                }
            } else {
                output.writeObject("Error: Faltan par�metros para SEND_MESSAGE.");
                output.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                output.writeObject("ERROR: No se pudo enviar el mensaje.");
                output.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    private void handleListConversations(User user, ObjectOutputStream output) throws IOException {
        try {
            // Obtener las conversaciones del usuario
            List<Conversation> conversations = conversationService.getConversationsByUserId(user.getId());
            System.out.println(conversations);
            output.writeObject(conversations);
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                output.writeObject(new ArrayList<Conversation>());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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
                System.out.println("El usuario no tiene un perfil creado. Solicitando datos para creaci�n.");
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
                output.writeObject("ERROR: Fallo en la creaci�n del perfil.");
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
                output.writeObject("ERROR: No se pudo procesar la actualizaci�n del perfil.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
