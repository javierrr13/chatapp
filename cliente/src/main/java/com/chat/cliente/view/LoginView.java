package com.chat.cliente.view;


import javax.swing.*;

import com.chat.cliente.controller.LoginController;
import com.chat.cliente.model.UserModel;
import com.chat.cliente.toaster.Toaster;
import com.chat.cliente.utils.HyperlinkText;
import com.chat.cliente.utils.StyledButton;
import com.chat.cliente.utils.TextFieldPassword;
import com.chat.cliente.utils.TextFieldUsername;
import com.chat.cliente.utils.UIUtils;

import java.awt.*;
import java.io.IOException;

public class LoginView extends JFrame {

 
	private static final long serialVersionUID = 1L;
	private final Toaster toaster;
    private final LoginController controller;

    private final JPanel cardPanel; // Panel principal con CardLayout
    private final CardLayout cardLayout; // CardLayout para las transiciones

    private final TextFieldUsername loginUsernameField = new TextFieldUsername();
    private final TextFieldPassword loginPasswordField = new TextFieldPassword();

    private final TextFieldUsername registerUsernameField = new TextFieldUsername();
    private final TextFieldUsername registerEmailField = new TextFieldUsername();
    private final TextFieldPassword registerPasswordField = new TextFieldPassword();

    public LoginView(LoginController controller) {
        super("Login/Register");
        this.controller = controller;

        // Configuración de la ventana
        this.setUndecorated(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setPreferredSize(new Dimension(800, 500));

        // Configuración del CardLayout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Agregar los paneles de login y registro
        cardPanel.add(createLoginPanel(), "Login");
        cardPanel.add(createRegisterPanel(), "Register");

        this.add(cardPanel);
        this.pack();
        this.setResizable(false);
        centerWindow();

        toaster = new Toaster(cardPanel);
    }

    private void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
    }

    private JPanel createLoginPanel() {
        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(null);
        loginPanel.setPreferredSize(new Dimension(800, 500));
        loginPanel.setBackground(UIUtils.COLOR_BACKGROUND);

        // Logo
        JLabel logo = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("lumo_placeholder.png")));
        logo.setBounds(50, 50, 200, 100);
        loginPanel.add(logo);

        // Separador vertical
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setForeground(UIUtils.COLOR_OUTLINE);
        separator.setBounds(300, 50, 1, 400);
        loginPanel.add(separator);

        // Username field
        loginUsernameField.setBounds(350, 100, 400, 50);
        loginPanel.add(loginUsernameField);

        // Password field
        loginPasswordField.setBounds(350, 180, 400, 50);
        loginPanel.add(loginPasswordField);

        // Login button
        JLabel loginButton = new StyledButton("Login", UIUtils.COLOR_INTERACTIVE, UIUtils.COLOR_INTERACTIVE_DARKER, this::loginEventHandler);
        loginButton.setBounds(350, 260, 400, 50);
        loginPanel.add(loginButton);

        // Switch to register
        JLabel switchToRegister = new HyperlinkText("Don't have an account? Register", 350, 330, this::showRegisterPanel);
        loginPanel.add(switchToRegister);

        return loginPanel;
    }

    private JPanel createRegisterPanel() {
        JPanel registerPanel = new JPanel();
        registerPanel.setLayout(null);
        registerPanel.setPreferredSize(new Dimension(800, 500));
        registerPanel.setBackground(UIUtils.COLOR_BACKGROUND);

        // Logo
        JLabel logo = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("lumo_placeholder.png")));
        logo.setBounds(50, 50, 200, 100);
        registerPanel.add(logo);

        // Separador vertical
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setForeground(UIUtils.COLOR_OUTLINE);
        separator.setBounds(300, 50, 1, 400);
        registerPanel.add(separator);

        // Username field
        registerUsernameField.setBounds(350, 100, 400, 50);
        registerPanel.add(registerUsernameField);

        // Email field
        registerEmailField.setBounds(350, 180, 400, 50);
        registerPanel.add(registerEmailField);

        // Password field
        registerPasswordField.setBounds(350, 260, 400, 50);
        registerPanel.add(registerPasswordField);

        // Register button
        JLabel registerButton = new StyledButton("Register", UIUtils.COLOR_INTERACTIVE, UIUtils.COLOR_INTERACTIVE_DARKER, this::registerEventHandler);
        registerButton.setBounds(350, 340, 400, 50);
        registerPanel.add(registerButton);

        // Switch to login
        JLabel switchToLogin = new HyperlinkText("Already have an account? Login", 350, 410, this::showLoginPanel);
        registerPanel.add(switchToLogin);

        return registerPanel;
    }

    private void loginEventHandler() {
        String username = loginUsernameField.getText();
        String password = new String(loginPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            toaster.warn("Both fields are required.");
            return;
        }

        try {
            UserModel userModel = new UserModel("localhost", 12345);
            boolean success = userModel.login(username, password);

            if (success) {
                toaster.success("Login successful.");
                controller.showDashboardView(userModel); // Mostrar el Dashboard
                this.dispose(); // Cerrar esta ventana
            } else {
            	System.out.println("LOGIN fallido");
                toaster.warn("Invalid credentials.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            toaster.error("Connection error.");
        }
    }

    private void registerEventHandler() {
        String username = registerUsernameField.getText();
        String email = registerEmailField.getText();
        String password = new String(registerPasswordField.getPassword());

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            toaster.warn("All fields are required.");
            return;
        }

        try {
            UserModel userModel = new UserModel("localhost", 12345);
            boolean success = userModel.register(username, email, password);

            if (success) {
                toaster.success("Registration successful. Please login.");
                showLoginPanel(); // Cambiar al panel de login
            } else {
                toaster.warn("Registration failed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            toaster.error("Connection error.");
        }
    }

    private void showLoginPanel() {
        cardLayout.show(cardPanel, "Login"); // Cambiar al panel de login
    }

    private void showRegisterPanel() {
        cardLayout.show(cardPanel, "Register"); // Cambiar al panel de registro
    }
}
