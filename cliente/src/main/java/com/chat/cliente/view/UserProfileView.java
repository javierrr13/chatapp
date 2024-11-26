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

public class UserProfileView extends JFrame {

    private final Toaster toaster;
    private final LoginController controller;
    private final UserModel userModel;

    private final TextFieldUsername fullNameField = new TextFieldUsername();
    private final JTextArea bioField = new JTextArea();
    private final TextFieldUsername profilePictureField = new TextFieldUsername();
    private final JLabel createdAtLabel = new JLabel();

    public UserProfileView(LoginController controller, UserModel userModel) {
        super("User Profile");
        System.out.println("Abriendo UserProfileView...");
        this.controller = controller;
        this.userModel = userModel;

        // Configuración de la ventana
        this.setUndecorated(true);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH); // Pantalla completa
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setLayout(new BorderLayout());

        // Panel principal
        JPanel mainPanel = createMainPanel();
        this.add(mainPanel, BorderLayout.CENTER);

        toaster = new Toaster(mainPanel);
        loadUserProfile(); // Cargar datos del perfil del usuario
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        mainPanel.setLayout(new BorderLayout());

        mainPanel.add(createHeader(), BorderLayout.NORTH);
        mainPanel.add(createContent(), BorderLayout.CENTER);
        mainPanel.add(createFooter(), BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setBackground(UIUtils.COLOR_INTERACTIVE_DARKER);
        header.setPreferredSize(new Dimension(0, 80)); // Altura fija
        header.setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel title = new JLabel("User Profile");
        title.setFont(UIUtils.FONT_GENERAL_UI.deriveFont(28f));
        title.setForeground(Color.WHITE);
        header.add(title);

        return header;
    }

    private JPanel createContent() {
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        contentPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Foto de perfil
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel profilePictureLabel = new JLabel("Profile Picture:");
        profilePictureLabel.setForeground(Color.WHITE);
        contentPanel.add(profilePictureLabel, gbc);

        gbc.gridx = 1;
        profilePictureField.setPreferredSize(new Dimension(400, 40));
        contentPanel.add(profilePictureField, gbc);

        gbc.gridx = 2;
        JLabel profilePicturePreview = new JLabel();
        profilePicturePreview.setOpaque(true);
        profilePicturePreview.setPreferredSize(new Dimension(100, 100));
        profilePicturePreview.setBackground(UIUtils.COLOR_OUTLINE);
        contentPanel.add(profilePicturePreview, gbc);

        // Nombre completo
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel fullNameLabel = new JLabel("Full Name:");
        fullNameLabel.setForeground(Color.WHITE);
        contentPanel.add(fullNameLabel, gbc);

        gbc.gridx = 1;
        fullNameField.setPreferredSize(new Dimension(400, 40));
        contentPanel.add(fullNameField, gbc);

        // Biografía
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel bioLabel = new JLabel("Bio:");
        bioLabel.setForeground(Color.WHITE);
        contentPanel.add(bioLabel, gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        bioField.setLineWrap(true);
        bioField.setWrapStyleWord(true);
        bioField.setPreferredSize(new Dimension(600, 150));
        bioField.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_OUTLINE));
        contentPanel.add(bioField, gbc);
        gbc.gridwidth = 1;

        // Fecha de creación
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel createdAtTitle = new JLabel("Created At:");
        createdAtTitle.setForeground(Color.WHITE);
        contentPanel.add(createdAtTitle, gbc);

        gbc.gridx = 1;
        contentPanel.add(createdAtLabel, gbc);

        return contentPanel;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel();
        footer.setBackground(UIUtils.COLOR_BACKGROUND);
        footer.setPreferredSize(new Dimension(0, 80));
        footer.setLayout(new FlowLayout(FlowLayout.RIGHT));

        // Botón de guardar
        JLabel saveButton = new StyledButton("Save", UIUtils.COLOR_INTERACTIVE, UIUtils.COLOR_INTERACTIVE_DARKER, this::saveUserProfile);
        saveButton.setPreferredSize(new Dimension(140, 40));
        footer.add(saveButton);

        // Botón de cancelar
        JLabel cancelButton = new StyledButton("Cancel", UIUtils.COLOR_BACKGROUND, UIUtils.COLOR_OUTLINE, this::dispose);
        cancelButton.setPreferredSize(new Dimension(140, 40));
        footer.add(cancelButton);

        return footer;
    }

    private void loadUserProfile() {
        try {
            UserProfileModel profile = userModel.getUserProfile();
            if (profile != null) {
                fullNameField.setText(profile.getFullname());
                bioField.setText(profile.getBio());
                profilePictureField.setText(profile.getProfilePicture());
                createdAtLabel.setText(profile.getCreatedAt());
            } else {
                toaster.warn("No profile data found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            toaster.error("Error loading profile.");
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

            UserProfileModel newProfile = new UserProfileModel(0, 0, fullName, bio, profilePicture, null);
            if (userModel.insertUserProfile(newProfile)) {
                toaster.success("Profile saved successfully!");
            } else {
                toaster.error("Failed to save profile.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            toaster.error("Error saving profile.");
        }
    }
}
