package com.chat.cliente.view;

import com.chat.cliente.controller.LoginController;
import com.chat.cliente.model.UserModel;
import com.chat.cliente.toaster.Toaster;
import com.chat.cliente.utils.StyledButton;
import com.chat.cliente.utils.TextFieldUsername;
import com.chat.cliente.utils.UIUtils;
import com.chat.shared.Conversation; // Modelo compartido para las conversaciones
import com.chat.shared.Message;
import com.chat.shared.UserProfileModel;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.time.LocalDateTime;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DashboardView extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Toaster toaster;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final UserModel userModel;

    private final JPanel conversationListPanel = new JPanel(new GridLayout(0, 1, 10, 10)); // Panel din�mico para las conversaciones
    private final JPanel chatPanel = new JPanel(new BorderLayout()); // Panel para la vista de chat
    
    private final TextFieldUsername fullNameField = new TextFieldUsername();
    private final JTextArea bioField = new JTextArea();
    private final TextFieldUsername profilePictureField = new TextFieldUsername();
    private final JLabel createdAtLabel = new JLabel();
    private final JLabel profilePicturePreview = new JLabel();
    private final Map<Integer, JTextArea> chatAreas = new HashMap<>();
    private boolean isListening = false;

    public DashboardView(UserModel userModel) {
        super("Dashboard");
        System.out.println("Abriendo dashboard");
        this.userModel = userModel;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Configurar pantalla completa
        setUndecorated(true);
        setSize(1200, 800); // Tama�o fijo, ancho x alto
        setLocationRelativeTo(null); 

        // Configuraci�n del CardLayout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Agregar vistas al CardLayout
        cardPanel.add(createDashboardPanel(), "Dashboard");
        cardPanel.add(chatPanel, "ChatView");// Vista de chat
        cardPanel.add(createUserProfilePanel(), "UserProfile");

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
                System.out.println("A�adiendo bot�n para: " + conversation.getName());
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

        // Recuperar o crear el �rea de texto asociada a la conversaci�n
        JTextArea chatArea = chatAreas.computeIfAbsent(conversation.getId(), k -> new JTextArea());
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        // Cargar mensajes hist�ricos
        try {
            List<Message> messages = userModel.getMessages(conversation.getId());
            chatArea.setText(""); // Limpia el �rea antes de cargar mensajes hist�ricos
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

        // Asegurar que estamos escuchando los mensajes en tiempo real
        if (!isListening) { // Variable para evitar m�ltiples hilos de escucha
            userModel.listenForMessages(chatAreas);
            isListening = true; // Iniciar solo una vez
        }

        JScrollPane scrollPane = new JScrollPane(chatArea);

        // Campo para enviar mensajes
        JTextField messageField = new JTextField();
        messageField.addActionListener(e -> {
            String content = messageField.getText();
            if (!content.isEmpty()) {
                try {
                    userModel.sendMessage(conversation.getId(), content);
                    messageField.setText("");
                } catch (IOException ex) {
                    toaster.error("Error enviando mensaje.");
                    ex.printStackTrace();
                }
            }
        });

        // Bot�n para volver al main
        JButton backButton = new JButton("Volver al Main");
        backButton.setBackground(UIUtils.COLOR_INTERACTIVE);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "Dashboard"));

        // Panel inferior con campo de mensaje y bot�n
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(backButton, BorderLayout.EAST);

        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(bottomPanel, BorderLayout.SOUTH);

        cardLayout.show(cardPanel, "ChatView");
    }


    private JPanel createUserProfilePanel() {
        JPanel userProfilePanel = new JPanel(new GridBagLayout());
        userProfilePanel.setBackground(UIUtils.COLOR_BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15); // Espaciado entre componentes
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        // Header
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Header ocupa dos columnas
        JLabel header = new JLabel("User Profile");
        header.setFont(UIUtils.FONT_GENERAL_UI.deriveFont(Font.BOLD, 36f));
        header.setForeground(UIUtils.COLOR_INTERACTIVE);
        header.setHorizontalAlignment(SwingConstants.CENTER);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIUtils.COLOR_OUTLINE));
        userProfilePanel.add(header, gbc);

        // Full Name Field
        gbc.gridy++;
        gbc.gridwidth = 1; // Restablece a 1 columna
        gbc.weighty = 0.1;
        gbc.gridx = 0;
        JLabel fullNameLabel = decorateLabel("Full Name:");
        userProfilePanel.add(fullNameLabel, gbc);

        gbc.gridx = 1;
        fullNameField.setPreferredSize(new Dimension(300, 40));
        fullNameField.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_OUTLINE, 2));
        userProfilePanel.add(fullNameField, gbc);

        // Bio Field
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel bioLabel = decorateLabel("Bio:");
        userProfilePanel.add(bioLabel, gbc);

        gbc.gridx = 1;
        bioField.setLineWrap(true);
        bioField.setWrapStyleWord(true);
        bioField.setPreferredSize(new Dimension(300, 80));
        bioField.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_OUTLINE, 2));
        userProfilePanel.add(bioField, gbc);

        // Profile Picture Field
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel profilePictureLabel = decorateLabel("Profile Picture:");
        userProfilePanel.add(profilePictureLabel, gbc);

        gbc.gridx = 1;
        JPanel profilePicturePanel = new JPanel(new BorderLayout());
        profilePicturePanel.setBackground(UIUtils.COLOR_BACKGROUND);

        // Bot�n para subir imagen
        JButton uploadButton = new JButton("Upload Image");
        uploadButton.setBackground(UIUtils.COLOR_INTERACTIVE);
        uploadButton.setForeground(Color.WHITE);
        uploadButton.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_OUTLINE, 2));
        uploadButton.addActionListener(e -> uploadProfilePicture());
        profilePicturePanel.add(uploadButton, BorderLayout.NORTH);

        // Imagen redonda para la previsualizaci�n
        profilePicturePreview.setPreferredSize(new Dimension(150, 150));
        profilePicturePreview.setOpaque(true);
        profilePicturePreview.setBackground(UIUtils.COLOR_OUTLINE);
        profilePicturePreview.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        profilePicturePreview.setHorizontalAlignment(SwingConstants.CENTER);
        profilePicturePanel.add(profilePicturePreview, BorderLayout.CENTER);

        userProfilePanel.add(profilePicturePanel, gbc);

        // Created At Label
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel createdAtTitle = decorateLabel("Created At:");
        userProfilePanel.add(createdAtTitle, gbc);

        gbc.gridx = 1;
        createdAtLabel.setForeground(UIUtils.COLOR_OUTLINE);
        createdAtLabel.setFont(UIUtils.FONT_GENERAL_UI.deriveFont(Font.PLAIN, 16f));
        userProfilePanel.add(createdAtLabel, gbc);

        // Buttons
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2; // Botones ocupan dos columnas
        gbc.weighty = 0.2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(UIUtils.COLOR_BACKGROUND);

        JLabel saveButton = new StyledButton("Save", UIUtils.COLOR_INTERACTIVE, UIUtils.COLOR_INTERACTIVE_DARKER, this::saveUserProfile);
        saveButton.setPreferredSize(new Dimension(140, 40));
        buttonPanel.add(saveButton);
        JButton backButton = new JButton("Volver");
        backButton.addActionListener(e -> {
            userModel.stopListening(); // Detener la escucha
            isListening = false; // Marcar que no estamos escuchando
            cardLayout.show(cardPanel, "Dashboard");
        });
        backButton.setBackground(UIUtils.COLOR_INTERACTIVE);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        buttonPanel.add(backButton);

        userProfilePanel.add(buttonPanel, gbc);

        return userProfilePanel;
    }

    private void uploadProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            updateProfilePicturePreview(selectedFile.getAbsolutePath());
        }
    }

    private void updateProfilePicturePreview(String imagePath) {
        ImageIcon originalIcon = new ImageIcon(imagePath);
        Image scaledImage = originalIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);

        // Hacer la imagen circular
        BufferedImage circularImage = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = circularImage.createGraphics();
        g2.setClip(new Ellipse2D.Float(0, 0, 150, 150));
        g2.drawImage(scaledImage, 0, 0, null);
        g2.dispose();

        profilePicturePreview.setIcon(new ImageIcon(circularImage));
    }

    // M�todo auxiliar para adornar etiquetas
    private JLabel decorateLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(UIUtils.COLOR_INTERACTIVE);
        label.setFont(UIUtils.FONT_GENERAL_UI.deriveFont(Font.BOLD, 16f));
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return label;
    }



    private void showUserProfile() {
        try {
            System.out.println("Fetching user profile...");
            UserProfileModel profile = userModel.getUserProfile();

            if (profile != null) {
                // Mostrar datos del perfil
                fullNameField.setText(profile.getFullname());
                bioField.setText(profile.getBio());
                profilePictureField.setText(profile.getProfilePicture());
                createdAtLabel.setText(profile.getCreatedAt() + " \n User id " + profile.getUserId());
                cardLayout.show(cardPanel, "UserProfile");
                System.out.println("User profile loaded successfully.");
            } else {
                // Si no hay perfil, permitir al usuario crear uno
                toaster.info("No profile found. Please create one.");
                fullNameField.setText("");
                bioField.setText("");
                profilePictureField.setText("");
                createdAtLabel.setText("");
                cardLayout.show(cardPanel, "UserProfile");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            toaster.error("Error loading user profile.");
        }
    }


    private void saveUserProfile() {
        try {
            String fullName = fullNameField.getText();
            String bio = bioField.getText();
            String profilePicture = profilePictureField.getText();

            if (fullName.isEmpty() || bio.isEmpty()) {
                toaster.error("All fields are required.");
                return;
            }

            UserProfileModel profile = new UserProfileModel(
                0, // ID is irrelevant here, handled on the server
                0, // User ID is assigned server-side
                fullName,
                bio,
                profilePicture,
                ""
            );

            boolean success = userModel.saveUserProfile(profile);
            System.out.println(profile);
            if (success) {
                toaster.success("Profile saved successfully.");
            } else {
                toaster.error("Failed to save profile.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            toaster.error("Error saving profile.");
        }
    }



    private void logout() {
        try {
            boolean success = userModel.logout();
            if (success) {
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
