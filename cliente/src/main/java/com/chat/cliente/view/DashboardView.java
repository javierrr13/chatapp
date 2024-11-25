package com.chat.cliente.view;


import javax.swing.*;

import com.chat.cliente.controller.LoginController;
import com.chat.cliente.model.UserModel;
import com.chat.cliente.toaster.Toaster;
import com.chat.cliente.utils.StyledButton;
import com.chat.cliente.utils.TextFieldUsername;
import com.chat.cliente.utils.UIUtils;
import com.chat.shared.UserProfileModel;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class DashboardView extends JFrame {

    private final Toaster toaster;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final UserModel userModel;
    
    private final TextFieldUsername fullNameField = new TextFieldUsername();
    private final JTextArea bioField = new JTextArea();
    private final TextFieldUsername profilePictureField = new TextFieldUsername();
    private final JLabel createdAtLabel = new JLabel();

    public DashboardView(UserModel userModel) {
        super("Dashboard");
        System.out.println("Abriendo dashboard");
        this.userModel = userModel;
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1000, 600);
        this.setUndecorated(true);

        // Configuración del CardLayout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Agregar vistas al CardLayout
        cardPanel.add(createDashboardPanel(), "Dashboard");
        cardPanel.add(createUserProfilePanel(), "UserProfile");

        this.add(cardPanel);
        centerWindow();

        toaster = new Toaster(cardPanel);
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
    }

    private JPanel createDashboardPanel() {
        JPanel dashboardPanel = new JPanel();
        dashboardPanel.setLayout(null);
        dashboardPanel.setBackground(UIUtils.COLOR_BACKGROUND);

        // Sidebar
        JPanel sidebar = new JPanel();
        sidebar.setBounds(0, 0, 250, 600);
        sidebar.setBackground(UIUtils.COLOR_INTERACTIVE_DARKER);
        sidebar.setLayout(null);

        JLabel logo = new JLabel();
        logo.setIcon(new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource("lumo_placeholder.png")).getFile()));
        logo.setBounds(50, 30, 150, 80);
        sidebar.add(logo);

        String[] buttonLabels = {"Home", "Profile", "Settings", "Logout"};
        Runnable[] actions = {
            () -> toaster.info("Home clicked!"),
            this::showUserProfile, // Cambiar a UserProfile
            () -> toaster.info("Settings clicked!"),
            this::logout // Logout
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
        JPanel contentArea = new JPanel();
        contentArea.setBounds(250, 0, 750, 600);
        contentArea.setBackground(UIUtils.COLOR_BACKGROUND);
        contentArea.setLayout(new BorderLayout());

        JLabel title = new JLabel("Dashboard Content", SwingConstants.CENTER);
        title.setFont(UIUtils.FONT_GENERAL_UI.deriveFont(24f));
        title.setForeground(UIUtils.COLOR_OUTLINE);
        contentArea.add(title, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane();
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        contentPanel.setLayout(new GridLayout(5, 2, 10, 10));

        for (int i = 1; i <= 10; i++) {
            JLabel card = new JLabel("Card " + i, SwingConstants.CENTER);
            card.setOpaque(true);
            card.setBackground(UIUtils.COLOR_INTERACTIVE);
            card.setForeground(Color.WHITE);
            card.setFont(UIUtils.FONT_GENERAL_UI);
            card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            contentPanel.add(card);
        }

        scrollPane.setViewportView(contentPanel);
        contentArea.add(scrollPane, BorderLayout.CENTER);

        dashboardPanel.add(contentArea);

        return dashboardPanel;
    }

    private JPanel createUserProfilePanel() {
        JPanel userProfilePanel = new JPanel();
        userProfilePanel.setLayout(null);
        userProfilePanel.setBackground(UIUtils.COLOR_BACKGROUND);

        // Header
        JLabel header = new JLabel("User Profile", SwingConstants.CENTER);
        header.setFont(UIUtils.FONT_GENERAL_UI.deriveFont(24f));
        header.setForeground(UIUtils.COLOR_OUTLINE);
        header.setBounds(0, 20, 1000, 40);
        userProfilePanel.add(header);

        // Full Name Field
        JLabel fullNameLabel = new JLabel("Full Name:");
        fullNameLabel.setForeground(Color.WHITE);
        fullNameLabel.setBounds(300, 100, 200, 30);
        userProfilePanel.add(fullNameLabel);

        fullNameField.setBounds(500, 100, 300, 40);
        userProfilePanel.add(fullNameField);

        // Bio Field
        JLabel bioLabel = new JLabel("Bio:");
        bioLabel.setForeground(Color.WHITE);
        bioLabel.setBounds(300, 160, 200, 30);
        userProfilePanel.add(bioLabel);

        bioField.setBounds(500, 160, 300, 100);
        bioField.setLineWrap(true);
        bioField.setWrapStyleWord(true);
        bioField.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_OUTLINE));
        userProfilePanel.add(bioField);

        // Save Button
        JLabel saveButton = new StyledButton("Save", UIUtils.COLOR_INTERACTIVE, UIUtils.COLOR_INTERACTIVE_DARKER, () -> {
            toaster.success("Profile saved!");
            showDashboard(); // Regresar al Dashboard
        });
        saveButton.setBounds(500, 280, 140, 40);
        userProfilePanel.add(saveButton);

        // Back Button
        JLabel backButton = new StyledButton("Back", UIUtils.COLOR_BACKGROUND, UIUtils.COLOR_OUTLINE, this::showDashboard);
        backButton.setBounds(660, 280, 140, 40);
        userProfilePanel.add(backButton);

        return userProfilePanel;
    }

    private void showDashboard() {
        cardLayout.show(cardPanel, "Dashboard");
    }


    private void logout() {
        try {
            toaster.info("Logging out...");
            boolean success = userModel.logout();

            if (success) {
                toaster.info("Logout exitoso.");
                SwingUtilities.invokeLater(() -> {
                    this.dispose();
                    new LoginView(LoginController.getInstance()).setVisible(true);
                });
            } else {
                toaster.error("Error al cerrar sesión. Intenta nuevamente.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            toaster.error("Error inesperado durante el logout.");
        }
    }
    private void showUserProfile() {
        try {
            System.out.println("Fetching user profile...");
            UserProfileModel profile = userModel.getUserProfile();
            if (profile != null) {
                cardLayout.show(cardPanel, "UserProfile");
                fullNameField.setText(profile.getFullname());
                bioField.setText(profile.getBio());
                createdAtLabel.setText(profile.getCreatedAt());
                System.out.println("User profile loaded successfully.");
            } else {
                toaster.warn("No profile data found.");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            toaster.error("Error loading user profile.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserModel userModel = null;
			try {
				userModel = new UserModel("localhost", 12345);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // Instancia ficticia para probar
            new DashboardView(userModel).setVisible(true);
        });
    }
}
