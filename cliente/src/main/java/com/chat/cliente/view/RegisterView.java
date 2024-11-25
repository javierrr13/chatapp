package com.chat.cliente.view;


import javax.swing.*;

import com.chat.cliente.controller.LoginController;
import com.chat.cliente.model.UserModel;
import com.chat.cliente.toaster.Toaster;
import com.chat.cliente.utils.StyledButton;
import com.chat.cliente.utils.TextFieldPassword;
import com.chat.cliente.utils.TextFieldUsername;
import com.chat.cliente.utils.UIUtils;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class RegisterView extends JFrame {

    private final Toaster toaster;

    private final TextFieldUsername usernameField = new TextFieldUsername();
    private final TextFieldUsername emailField = new TextFieldUsername();
    private final TextFieldPassword passwordField = new TextFieldPassword();

    private final LoginController controller;

    public RegisterView(LoginController controller) {
        super("Register");
        this.controller = controller;

        // Configuración de la ventana
        this.setUndecorated(true);

        JPanel mainPanel = getMainPanel();
        addLogo(mainPanel);
        addFields(mainPanel);
        addRegisterButton(mainPanel);
        addCancelButton(mainPanel);

        this.add(mainPanel);
        this.pack();
        this.setResizable(false);
        centerWindow();

        toaster = new Toaster(mainPanel);
        addCloseOperation();
    }

    private JPanel getMainPanel() {
        Dimension size = new Dimension(500, 450);

        JPanel panel = new JPanel();
        panel.setSize(size);
        panel.setPreferredSize(size);
        panel.setBackground(UIUtils.COLOR_BACKGROUND);
        panel.setLayout(null);

        enableDrag(panel);

        return panel;
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
    }

    private void addCloseOperation() {
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Solo cerrar esta ventana
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

    private void addLogo(JPanel panel) {
        JLabel logo = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("lumo_placeholder.png")));
        logo.setBounds(175, 20, 150, 50); // Centrar horizontalmente
        panel.add(logo);
    }

    private void addFields(JPanel panel) {
        int fieldWidth = 300;
        int fieldHeight = 40;
        int xCenter = (500 - fieldWidth) / 2;

        // Username field
        usernameField.setBounds(xCenter, 100, fieldWidth, fieldHeight);
       
        panel.add(usernameField);

        // Email field
        emailField.setBounds(xCenter, 160, fieldWidth, fieldHeight);
     
        panel.add(emailField);

        // Password field
        passwordField.setBounds(xCenter, 220, fieldWidth, fieldHeight);
 
        panel.add(passwordField);
    }

    private void addRegisterButton(JPanel panel) {
        int buttonWidth = 140;
        int buttonHeight = 40;
        int xLeft = (500 - 2 * buttonWidth - 20) / 2;

        JLabel registerButton = new StyledButton("Register", UIUtils.COLOR_INTERACTIVE, UIUtils.COLOR_INTERACTIVE_DARKER, this::registerEventHandler);
        registerButton.setBounds(xLeft, 300, buttonWidth, buttonHeight);
        panel.add(registerButton);
    }

    private void addCancelButton(JPanel panel) {
        int buttonWidth = 140;
        int buttonHeight = 40;
        int xRight = (500 + 20) / 2;

        JLabel cancelButton = new StyledButton("Cancel", UIUtils.COLOR_BACKGROUND, UIUtils.COLOR_OUTLINE, this::dispose);
        cancelButton.setBounds(xRight, 300, buttonWidth, buttonHeight);
        panel.add(cancelButton);
    }

    private void registerEventHandler() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            toaster.warn("Todos los campos son obligatorios.");
            return;
        }

        try {
            UserModel userModel = new UserModel("localhost", 12345);
            boolean success = userModel.register(username, email, password);

            if (success) {
                toaster.success("Registro exitoso. Ahora puedes iniciar sesión.");
                this.dispose(); // Cerrar ventana de registro
            } else {
                toaster.warn("Error en el registro. Intenta nuevamente.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            toaster.error("Error al conectar al servidor.");
        }
    }
}
