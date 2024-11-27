package com.chat.cliente.view;

import com.chat.cliente.controller.LoginController;
import com.chat.cliente.model.UserModel;
import com.chat.cliente.toaster.Toaster;
import com.chat.cliente.utils.StyledButton;
import com.chat.cliente.utils.UIUtils;
import com.chat.shared.Conversation; // Modelo compartido para las conversaciones
import com.chat.shared.Message;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class DashboardView extends JFrame {

    private final Toaster toaster;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final UserModel userModel;

    private final JPanel conversationListPanel = new JPanel(new GridLayout(0, 1, 10, 10)); // Panel dinámico para las conversaciones
    private final JPanel chatPanel = new JPanel(new BorderLayout()); // Panel para la vista de chat

    public DashboardView(UserModel userModel) {
        super("Dashboard");
        System.out.println("Abriendo dashboard");
        this.userModel = userModel;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Configurar pantalla completa
        setUndecorated(true);
        setSize(1200, 800); // Tamaño fijo, ancho x alto
        setLocationRelativeTo(null); 

        // Configuración del CardLayout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Agregar vistas al CardLayout
        cardPanel.add(createDashboardPanel(), "Dashboard");
        cardPanel.add(chatPanel, "ChatView"); // Vista de chat

        this.add(cardPanel);

        toaster = new Toaster(cardPanel);

        // Cargar conversaciones al abrir el dashboard
        loadConversations();
    }

    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(null);
        dashboardPanel.setBackground(UIUtils.COLOR_BACKGROUND);

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setBounds(0, 0, 250, Toolkit.getDefaultToolkit().getScreenSize().height);
        sidebar.setBackground(UIUtils.COLOR_INTERACTIVE_DARKER);
        sidebar.setLayout(null);

        JLabel logo = new JLabel();
        logo.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("lumo_placeholder.png")).getFile()));
        logo.setBounds(50, 30, 150, 80);
        sidebar.add(logo);

        String[] buttonLabels = {"Home", "Profile", "Settings", "Logout"};
        Runnable[] actions = {
            () -> toaster.info("Home clicked!"),
            this::showUserProfile,
            () -> toaster.info("Settings clicked!"),
            this::logout
        };

        int yPos = 140;
        for (int i = 0; i < buttonLabels.length; i++) {
            String label = buttonLabels[i];
            Runnable action = actions[i];

            JLabel button = new StyledButton(label, UIUtils.COLOR_BACKGROUND, UIUtils.COLOR_INTERACTIVE, action);
            button.setBounds(25, yPos, 200, 50);
            yPos += 70;
            sidebar.add(button);
        }

        dashboardPanel.add(sidebar);

        // Content area
        JPanel contentArea = new JPanel(new BorderLayout());
        contentArea.setBounds(250, 0, getWidth() - 250, getHeight());
        contentArea.setBackground(UIUtils.COLOR_BACKGROUND);

        JLabel title = new JLabel("Mis Conversaciones", SwingConstants.CENTER);
        title.setFont(UIUtils.FONT_GENERAL_UI.deriveFont(24f));
        title.setForeground(UIUtils.COLOR_OUTLINE);
        contentArea.add(title, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(conversationListPanel);
        conversationListPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        contentArea.add(scrollPane, BorderLayout.CENTER);

        dashboardPanel.add(contentArea);

        return dashboardPanel;
    }

    private void loadConversations() {
        try {
            List<Conversation> conversations = userModel.getConversations();
            System.out.println("Conversaciones obtenidas: " + conversations);

            conversationListPanel.removeAll(); // Limpiar panel de conversaciones anteriores
            if (conversations.isEmpty()) {
                System.out.println("No hay conversaciones para mostrar.");
            }

            for (Conversation conversation : conversations) {
                System.out.println("Añadiendo botón para: " + conversation.getName());
                JLabel conversationButton = new StyledButton(
                    conversation.getName(),
                    UIUtils.COLOR_INTERACTIVE,
                    UIUtils.COLOR_INTERACTIVE_DARKER,
                    () -> openChat(conversation)
                );
                conversationButton.setPreferredSize(new Dimension(200, 50));
                conversationListPanel.add(conversationButton);
            }

            conversationListPanel.revalidate();
            conversationListPanel.repaint();
        } catch (IOException | ClassNotFoundException e) {
            toaster.error("Error cargando conversaciones.");
            e.printStackTrace();
        }
    }


    private void openChat(Conversation conversation) {
        chatPanel.removeAll();

        JLabel title = new JLabel("Chat: " + conversation.getName(), SwingConstants.CENTER);
        title.setFont(UIUtils.FONT_GENERAL_UI.deriveFont(24f));
        title.setForeground(UIUtils.COLOR_OUTLINE);

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        // Cargar mensajes de la conversación
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

        JScrollPane scrollPane = new JScrollPane(chatArea);

        JTextField messageField = new JTextField();
        messageField.addActionListener(e -> {
            String content = messageField.getText();
            if (!content.isEmpty()) {
                try {
                    try {
                        userModel.sendMessage(conversation.getId(), content);
                    } catch (ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }
                    String formattedMessage = String.format(
                        "[%s] Yo: %s",
                        LocalDateTime.now().toLocalTime(),
                        content
                    );
                    chatArea.append(formattedMessage + "\n");
                    messageField.setText("");
                } catch (IOException ex) {
                    toaster.error("Error enviando mensaje.");
                    ex.printStackTrace();
                }
            }
        });

        chatPanel.add(title, BorderLayout.NORTH);
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(messageField, BorderLayout.SOUTH);

        cardLayout.show(cardPanel, "ChatView");
    }


    private void showUserProfile() {
        // Implementación para mostrar el perfil de usuario
        toaster.info("Perfil no implementado.");
    }

    private void logout() {
        try {
            toaster.info("Logging out...");
            boolean success = userModel.logout();
            if (success) {
                toaster.info("Logout successful.");
                SwingUtilities.invokeLater(() -> {
                    this.dispose();
                    new LoginView(LoginController.getInstance()).setVisible(true);
                });
            } else {
                toaster.error("Logout failed. Try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            toaster.error("Unexpected error during logout.");
        }
    }
}
