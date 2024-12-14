package com.chat.cliente.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
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

    private SwingWorker<Void, Message> messageListenerWorker;
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
            public void windowClosing(WindowEvent e) {
                stopMessageListener();
            }
        });
    }

    private void initComponents() {
        chatPanel = new JPanel(new BorderLayout());
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField();
        messageField.setPreferredSize(new Dimension(200, 30));

        backButton = new JButton("Volver");
        bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(backButton, BorderLayout.EAST);

        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    public JPanel getChatPanel() {
        return chatPanel;
    }

    public void openChat(Conversation conversation, Toaster toaster) {
        chatPanel.removeAll();

        // Recuperar el área de texto asociada a la conversación
        chatArea = chatAreas.computeIfAbsent(conversation.getId(), k -> new JTextArea());
        chatArea.setText(""); // Limpia antes de cargar mensajes

        loadMessages(conversation, toaster);

        backButton.addActionListener(e -> {
            stopMessageListener();
            cardLayout.show(cardPanel, "Dashboard");
        });

        messageField.addActionListener(e -> {
            String content = messageField.getText();
            if (!content.isEmpty()) {
                userModel.sendMessage(conversation.getId(), content);
                messageField.setText("");
            }
        });

        startMessageListener(conversation.getId());

        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(bottomPanel, BorderLayout.SOUTH);
        cardLayout.show(cardPanel, "ChatView");
    }

    private void loadMessages(Conversation conversation, Toaster toaster) {
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
    }

    private void startMessageListener(int conversationId) {
        stopMessageListener(); // Garantiza que no haya otro worker activo

        messageListenerWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (!isCancelled()) {
                    try {
                        Object response = userModel.getInput().readObject();
                        if (response instanceof Message message) {
                            publish(message); // Envía el mensaje al método process
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        System.err.println("Error en el hilo de escucha: " + e.getMessage());
                        cancel(true); // Cancela el worker si ocurre un error
                    }
                }
                return null;
            }

            @Override
            protected void process(List<Message> chunks) {
                for (Message message : chunks) {
                    // Asegurarse de obtener o crear el JTextArea correcto
                    JTextArea currentChatArea = chatAreas.computeIfAbsent(message.getConversationId(), id -> {
                        JTextArea newChatArea = new JTextArea();
                        newChatArea.setEditable(false);
                        newChatArea.setLineWrap(true);
                        newChatArea.setWrapStyleWord(true);
                        return newChatArea;
                    });

                    // Actualizar el JTextArea con el mensaje recibido
                    currentChatArea.append(String.format(
                            "[%s] Usuario %d: %s\n",
                            message.getSentAt().toLocalTime(),
                            message.getUserId(),
                            message.getContent()));

                    // Desplazar automáticamente el scroll hacia abajo
                    currentChatArea.setCaretPosition(currentChatArea.getDocument().getLength());

                    // Actualizar el JScrollPane y el panel si no coincide el área actual
                    if (scrollPane.getViewport().getView() != currentChatArea) {
                        scrollPane.setViewportView(currentChatArea); // Asociar el JTextArea actual al scrollPane
                        chatPanel.revalidate();
                        chatPanel.repaint();
                    }
                }
            }

           
            @Override
            protected void done() {
                System.out.println("Listener detenido.");
            }
        };

        messageListenerWorker.execute();
    }

    private void stopMessageListener() {
        if (messageListenerWorker != null && !messageListenerWorker.isDone()) {
            messageListenerWorker.cancel(true);
        }
    }
}
