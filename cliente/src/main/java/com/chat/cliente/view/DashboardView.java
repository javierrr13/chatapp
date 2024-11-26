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

        // Configurar pantalla completa
        setUndecorated(true); // Sin bordes
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximizar ventana
        setSize(Toolkit.getDefaultToolkit().getScreenSize()); // Tamaño de pantalla completa

        // Configuración del CardLayout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Agregar vistas al CardLayout
        cardPanel.add(createDashboardPanel(), "Dashboard");
        cardPanel.add(createUserProfilePanel(), "UserProfile");

        this.add(cardPanel);

        toaster = new Toaster(cardPanel);
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
            this::showUserProfile, // Show User Profile
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
        contentArea.setBounds(250, 0, getWidth() - 250, getHeight());
        contentArea.setBackground(UIUtils.COLOR_BACKGROUND);
        contentArea.setLayout(new BorderLayout());

        JLabel title = new JLabel("StudentValue", SwingConstants.CENTER);
        title.setFont(UIUtils.FONT_GENERAL_UI.deriveFont(24f));
        title.setForeground(UIUtils.COLOR_OUTLINE);
        contentArea.add(title, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane();
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        contentPanel.setLayout(new GridLayout(5, 2, 10, 10));

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

        // Profile Picture Field
        JLabel profilePictureLabel = new JLabel("Profile Picture:");
        profilePictureLabel.setForeground(Color.WHITE);
        profilePictureLabel.setBounds(300, 280, 200, 30);
        userProfilePanel.add(profilePictureLabel);

        profilePictureField.setBounds(500, 280, 300, 40);
        userProfilePanel.add(profilePictureField);

        // Created At Label
        JLabel createdAtTitle = new JLabel("Created At:");
        createdAtTitle.setForeground(Color.WHITE);
        createdAtTitle.setBounds(300, 340, 200, 30);
        userProfilePanel.add(createdAtTitle);

        createdAtLabel.setBounds(500, 340, 300, 40);
        createdAtLabel.setForeground(UIUtils.COLOR_OUTLINE);
        userProfilePanel.add(createdAtLabel);

        // Save Button
        JLabel saveButton = new StyledButton("Save", UIUtils.COLOR_INTERACTIVE, UIUtils.COLOR_INTERACTIVE_DARKER, this::saveUserProfile);
        saveButton.setBounds(500, 400, 140, 40);
        userProfilePanel.add(saveButton);

        // Back Button
    
     // Back Button
        JLabel backButton = new StyledButton(
            "Back",
            UIUtils.COLOR_BACKGROUND,
            UIUtils.COLOR_OUTLINE,
            this::showDashboard // Ensure this method is visible and matches `Runnable`
        );
        backButton.setBounds(660, 400, 140, 40);
        userProfilePanel.add(backButton);

        return userProfilePanel;
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
    private void showDashboard() {
        cardLayout.show(cardPanel, "Dashboard");
    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserModel userModel = null;
            try {
                userModel = new UserModel("localhost", 12345);
            } catch (IOException e) {
                e.printStackTrace();
            }
            new DashboardView(userModel).setVisible(true);
        });
    }
}
