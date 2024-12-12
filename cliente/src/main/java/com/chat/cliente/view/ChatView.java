package com.chat.cliente.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import com.chat.cliente.model.UserModel;
import com.chat.cliente.toaster.Toaster;
import com.chat.shared.Conversation;
import com.chat.shared.Message;

public class ChatView {
    private JPanel chatPanel;
    private JTextArea chatArea;
    private JTextField messageField;
    private JScrollPane scrollPane;
    private JButton backButton;
    private JPanel bottomPanel;
    private final UserModel userModel;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final Map<Integer, JTextArea> chatAreas;
    private Thread backgroundThread;
    private boolean running = false;

    private final JFrame parentFrame;

    public ChatView(UserModel userModel, CardLayout cardLayout, JPanel cardPanel, Map<Integer, JTextArea> chatAreas, JFrame parentFrame) {
        this.userModel = userModel;
        this.cardLayout = cardLayout;
        this.cardPanel = cardPanel;
        this.chatAreas = chatAreas;
        this.parentFrame = parentFrame; 
        initComponents();
        setupWindowListener();
    }

    private void setupWindowListener() {
        parentFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                stopBackgroundTask();
                System.out.println("Background task stopped");
            }
        });
    }

    private void initComponents() {
    	System.out.println("ChatView Abierto");
        chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        backButton = new JButton("Volver");

        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(backButton, BorderLayout.EAST);
    }

    public JPanel getChatPanel() {
        return chatPanel;
    }

    public void openChat(Conversation conversation, Toaster toaster) {
        chatPanel.removeAll();
        try {
            startBackgroundTask();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Recuperar o crear el área de texto asociada a la conversación
        chatArea = chatAreas.computeIfAbsent(conversation.getId(), k -> new JTextArea());
        chatArea.setText(""); // Limpia antes de cargar mensajes

        try {
            List<Message> messages = userModel.getMessages(conversation.getId());
            for (Message message : messages) {
                String formattedMessage = String.format(
                        "[%s] Usuario %d: %s",
                        message.getSentAt().toLocalTime(),
                        message.getUserId(),
                        message.getContent()
                );
                chatArea.append(formattedMessage + "\n");
            }
        } catch (IOException | ClassNotFoundException e) {
            toaster.error("Error cargando mensajes.");
            e.printStackTrace();
        }
        // Configurar componentes
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(bottomPanel, BorderLayout.SOUTH);

        messageField.addActionListener(e -> {
            String content = messageField.getText();
            if (!content.isEmpty()) {
                userModel.sendMessage(conversation.getId(), content);
                messageField.setText("");
            }
        });

        backButton.addActionListener(e -> {
            cardLayout.show(cardPanel, "Dashboard");
            stopBackgroundTask();
        });

        cardLayout.show(cardPanel, "ChatView");
    }

    private void startBackgroundTask() throws IOException {
    	ObjectInputStream input = userModel.getInput();
        running = true;
        backgroundThread = new Thread(() -> {
            while (running) {
            	try {
                    // Verificar si el hilo fue interrumpido antes de bloquear
                    if (Thread.currentThread().isInterrupted()) {
                        throw new InterruptedException();
                    }
                    System.out.println("Hilo activo");
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
                    if (!running) {
                        System.out.println("Hilo de escucha detenido manualmente.");
                    } else {
                        System.err.println("Error en el hilo de escucha: " + e.getMessage());
                    }
                    break; // Salir del bucle si hay un error
                }
            }
            System.out.println("Hilo detenido.");
        });
        backgroundThread.start();
    }

    public void stopBackgroundTask() {
    	
        running = false;
        try {
            backgroundThread.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
