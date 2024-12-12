package com.chat.cliente.view;


import com.chat.cliente.model.UserModel;
import com.chat.cliente.toaster.Toaster;
import com.chat.cliente.utils.TextFieldUsername;
import com.chat.cliente.utils.UIUtils;
import com.chat.shared.UserProfileModel;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.function.Consumer;

public class UserProfileView extends JPanel {

    private final TextFieldUsername fullNameField = new TextFieldUsername();
    private final JTextArea bioField = new JTextArea();
    private final JLabel createdAtLabel = new JLabel();
    private final UserModel userModel;
    private final Toaster toaster;

    private final Consumer<Void> onBackAction;

    public UserProfileView(UserModel userModel, Consumer<Void> onBackAction) {
        this.userModel = userModel;
        this.toaster = new Toaster(this);
        this.onBackAction = onBackAction;

        setLayout(new GridBagLayout());
        setBackground(UIUtils.COLOR_BACKGROUND);
        initComponents();
    }

    private void initComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Header
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel header = new JLabel("User Profile", SwingConstants.CENTER);
        header.setFont(UIUtils.FONT_GENERAL_UI.deriveFont(Font.BOLD, 36f));
        header.setForeground(UIUtils.COLOR_INTERACTIVE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIUtils.COLOR_OUTLINE));
        add(header, gbc);

        // Full Name Field
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        add(decorateLabel("Full Name:"), gbc);

        gbc.gridx = 1;
        fullNameField.setPreferredSize(new Dimension(300, 40));
        add(fullNameField, gbc);

        // Bio Field
        gbc.gridy++;
        gbc.gridx = 0;
        add(decorateLabel("Bio:"), gbc);

        gbc.gridx = 1;
        bioField.setLineWrap(true);
        bioField.setWrapStyleWord(true);
        bioField.setPreferredSize(new Dimension(300, 80));
        add(new JScrollPane(bioField), gbc);


        // Created At Label
        gbc.gridy++;
        gbc.gridx = 0;
        add(decorateLabel("Created At:"), gbc);

        gbc.gridx = 1;
        createdAtLabel.setForeground(UIUtils.COLOR_OUTLINE);
        add(createdAtLabel, gbc);

        // Buttons
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(UIUtils.COLOR_BACKGROUND);

        JButton saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(120, 40));
        saveButton.setBackground(UIUtils.COLOR_INTERACTIVE);
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> saveUserProfile().accept(null));
        buttonPanel.add(saveButton);

        JButton backButton = new JButton("Back");
        backButton.setPreferredSize(new Dimension(120, 40));
        backButton.setBackground(UIUtils.COLOR_INTERACTIVE_DARKER);
        backButton.setForeground(Color.WHITE);
        backButton.addActionListener(e -> onBackAction.accept(null));
        buttonPanel.add(backButton);

        add(buttonPanel, gbc);
    }

    private JLabel decorateLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UIUtils.FONT_GENERAL_UI.deriveFont(Font.BOLD, 16f));
        label.setForeground(UIUtils.COLOR_OUTLINE);
        return label;
    }

    private Consumer<Void> saveUserProfile() {
        return (v) -> {
            try {
                String fullName = fullNameField.getText();
                String bio = bioField.getText();

                if (fullName.isEmpty() || bio.isEmpty()) {
                    toaster.error("All fields are required.");
                    return;
                }

                UserProfileModel profile = new UserProfileModel(
                    0, 
                    0, 
                    fullName,
                    bio,
                    "",
                    ""
                );

                boolean success = false;
				try {
					success = userModel.saveUserProfile(profile);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                if (success) {
                    toaster.success("Perfil guardado.");
                } else {
                    toaster.error("Failed to save profile.");
                }
            } catch (IOException e) {
                toaster.error("Error saving profile.");
            }
        };
    }

    public void loadUserProfile() {
        try {
            UserProfileModel profile = userModel.getUserProfile();
            if (profile != null) {
                fullNameField.setText(profile.getFullname());
                bioField.setText(profile.getBio());
                createdAtLabel.setText("Created At: " + profile.getCreatedAt());
            } else {
                toaster.info("No profile found. Please create one.");
            }
        } catch (IOException | ClassNotFoundException e) {
            toaster.error("Error loading profile.");
        }
    }
}

