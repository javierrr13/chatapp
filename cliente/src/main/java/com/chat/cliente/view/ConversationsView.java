
package com.chat.cliente.view;


import com.chat.cliente.model.UserModel;
import com.chat.cliente.toaster.Toaster;
import com.chat.cliente.utils.StyledButton;
import com.chat.cliente.utils.TextFieldUsername;
import com.chat.cliente.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ConversationsView extends JFrame {
    private static final long serialVersionUID = 1L;

    private final UserModel userModel;


    private final TextFieldUsername conversationNameField = new TextFieldUsername();
    private final JTextArea usersArea = new JTextArea();

    public ConversationsView(UserModel userModel) {
        super("Crear Conversación");
        this.userModel = userModel;

        // Configuración del frame
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setBackground(UIUtils.COLOR_BACKGROUND);

        // Panel principal
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(UIUtils.COLOR_BACKGROUND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel titleLabel = new JLabel("Nueva Conversación", SwingConstants.CENTER);
        titleLabel.setFont(UIUtils.FONT_GENERAL_UI.deriveFont(24f));
        titleLabel.setForeground(UIUtils.COLOR_INTERACTIVE);
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(titleLabel, gbc);

        // Nombre de la conversación
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel nameLabel = new JLabel("Nombre de la Conversación:");
        nameLabel.setForeground(UIUtils.COLOR_OUTLINE);
        mainPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        conversationNameField.setPreferredSize(new Dimension(300, 30));
        mainPanel.add(conversationNameField, gbc);

        // Área para agregar usuarios
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel usersLabel = new JLabel("IDs de Usuarios (separados por coma):");
        usersLabel.setForeground(UIUtils.COLOR_OUTLINE);
        mainPanel.add(usersLabel, gbc);

        gbc.gridx = 1;
        JScrollPane scrollPane = new JScrollPane(usersArea);
        usersArea.setLineWrap(true);
        usersArea.setWrapStyleWord(true);
        scrollPane.setPreferredSize(new Dimension(300, 100));
        mainPanel.add(scrollPane, gbc);

        // Botones
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(UIUtils.COLOR_BACKGROUND);

        JLabel createButton = new StyledButton(
                "Crear Conversación",
                UIUtils.COLOR_INTERACTIVE,
                UIUtils.COLOR_INTERACTIVE_DARKER,
                this::createConversation
        );
        createButton.setPreferredSize(new Dimension(200, 40));
        buttonPanel.add(createButton);

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.setBackground(UIUtils.COLOR_OUTLINE);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.addActionListener(e -> this.dispose());
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void createConversation() {
    	
        String conversationName = conversationNameField.getText().trim();
        String usersInput = usersArea.getText().trim();

 
        // Parsear IDs de usuarios
        String[] userIdsArray = usersInput.split(",");
        List<Integer> userIds = new ArrayList<>();
        try {
            for (String id : userIdsArray) {
                userIds.add(Integer.parseInt(id.trim()));
            }
        } catch (NumberFormatException e) {
            System.out.println("Los IDs de usuario deben ser números válidos.");
            return;
        }
        System.out.println(conversationName+ " "+ userIds);
        

//        try {
//            boolean success = userModel.createConversation(conversationName, userIds);
//            if (success) {
//            	System.out.println("Conversación creada exitosamente.");
//                this.dispose();
//            } else {
//            	System.out.println("Error al crear la conversación. Intente nuevamente.");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            System.out.println("Ocurrió un error inesperado.");
//        }
    }
}