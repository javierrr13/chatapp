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
import java.awt.event.*;

public class UserProfileView extends JFrame {

    private final Toaster toaster;
    private final LoginController controller;
    private final UserModel userModel;

    private final TextFieldUsername fullNameField = new TextFieldUsername();
    private final JTextArea bioField = new JTextArea();
    private final TextFieldUsername profilePictureField = new TextFieldUsername();
    private final JLabel profilePicturePreview = new JLabel();
    private final JLabel createdAtLabel = new JLabel();

    public UserProfileView(LoginController controller, UserModel userModel) {
        super("User Profile");
        System.out.println("Abriendo UserProfileView...");
        this.controller = controller;
        this.userModel = userModel;

        // Configuración de la ventana
        this.setUndecorated(true);
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setPreferredSize(new Dimension(800, 600));

        JPanel mainPanel = getMainPanel();
        addFields(mainPanel);
        addButtons(mainPanel);

        this.add(mainPanel);
        this.pack();
        this.setResizable(false);
        centerWindow();

        toaster = new Toaster(mainPanel);
        loadUserProfile(); // Cargar datos del perfil del usuario
    }


    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
    }

    private JPanel getMainPanel() {
        JPanel panel = new JPanel();
        panel.setSize(new Dimension(800, 600));
        panel.setPreferredSize(new Dimension(800, 600));
        panel.setBackground(UIUtils.COLOR_BACKGROUND);
        panel.setLayout(null);

        enableDrag(panel);

        return panel;
    }

    private void enableDrag(JPanel panel) {
        MouseAdapter dragListener = new MouseAdapter() {
            int lastX, lastY;

            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getXOnScreen();
                lastY = e.getYOnScreen();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();
                setLocation(getLocationOnScreen().x + x - lastX, getLocationOnScreen().y + y - lastY);
                lastX = x;
                lastY = y;
            }
        };

        panel.addMouseListener(dragListener);
        panel.addMouseMotionListener(dragListener);
    }

    private void addFields(JPanel panel) {
        // Foto de perfil
        JLabel profilePictureLabel = new JLabel("Profile Picture:");
        profilePictureLabel.setForeground(Color.WHITE);
        profilePictureLabel.setBounds(50, 50, 200, 30);
        panel.add(profilePictureLabel);

        profilePicturePreview.setBounds(250, 50, 100, 100);
        profilePicturePreview.setOpaque(true);
        profilePicturePreview.setBackground(UIUtils.COLOR_OUTLINE);
        panel.add(profilePicturePreview);

        profilePictureField.setBounds(400, 50, 300, 40);
        panel.add(profilePictureField);

        // Nombre completo
        JLabel fullNameLabel = new JLabel("Full Name:");
        fullNameLabel.setForeground(Color.WHITE);
        fullNameLabel.setBounds(50, 180, 200, 30);
        panel.add(fullNameLabel);

        fullNameField.setBounds(250, 180, 450, 40);
        panel.add(fullNameField);

        // Biografía
        JLabel bioLabel = new JLabel("Bio:");
        bioLabel.setForeground(Color.WHITE);
        bioLabel.setBounds(50, 240, 200, 30);
        panel.add(bioLabel);

        bioField.setBounds(250, 240, 450, 100);
        bioField.setLineWrap(true);
        bioField.setWrapStyleWord(true);
        bioField.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_OUTLINE));
        panel.add(bioField);

        // Fecha de creación
        JLabel createdAtTitle = new JLabel("Created At:");
        createdAtTitle.setForeground(Color.WHITE);
        createdAtTitle.setBounds(50, 360, 200, 30);
        panel.add(createdAtTitle);

        createdAtLabel.setBounds(250, 360, 450, 30);
        createdAtLabel.setForeground(UIUtils.COLOR_OUTLINE);
        panel.add(createdAtLabel);
    }

    private void addButtons(JPanel panel) {
        JLabel saveButton = new StyledButton("Save", UIUtils.COLOR_INTERACTIVE, UIUtils.COLOR_INTERACTIVE_DARKER, () -> {
            UserProfileModel newProfile = collectProfileData();
            try {
                if (userModel.insertUserProfile(newProfile)) {
                    toaster.success("Profile saved successfully!");
                } else {
                    toaster.error("Failed to save profile.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                toaster.error("Error saving profile.");
            }
        });
        saveButton.setBounds(250, 500, 140, 50);
        panel.add(saveButton);

        JLabel cancelButton = new StyledButton("Cancel", UIUtils.COLOR_BACKGROUND, UIUtils.COLOR_OUTLINE, this::dispose);
        cancelButton.setBounds(450, 500, 140, 50);
        panel.add(cancelButton);
    }


    private void loadUserProfile() {
        try {
            UserProfileModel profile = userModel.getUserProfile();
            if (profile != null) {
                fullNameField.setText(profile.getFullname());
                bioField.setText(profile.getBio());
                profilePictureField.setText(profile.getProfilePicture());
                createdAtLabel.setText(profile.getCreatedAt());
//                updateProfilePicture(profile.getProfilePicture());
            } else {
                toaster.warn("No profile data found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            toaster.error("Error loading profile.");
        }
    }
    public UserProfileModel collectProfileData() {
        String fullName = fullNameField.getText().trim();
        String bio = bioField.getText().trim();
        String profilePicture = profilePictureField.getText().trim();
        
        return new UserProfileModel(0, 0, fullName, bio, profilePicture, null);
    }


}
