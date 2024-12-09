package com.chat.cliente.controller;
import java.io.IOException;

import javax.swing.JFrame;

import com.chat.cliente.model.UserModel;
import com.chat.cliente.view.DashboardView;
import com.chat.cliente.view.LoginView;



public class LoginController {
    private static LoginController instance;
    private JFrame currentView;

    public static LoginController getInstance() {
        if (instance == null) {
            instance = new LoginController();
        }
        return instance;
    }

    private LoginController() {
        showLoginView();
    }

    public void showLoginView() {
        if (currentView != null) {
            System.out.println("Cerrando ventana actual: " + currentView.getTitle());
            currentView.dispose(); // Cerrar la ventana actual
            currentView = null;
        }
        System.out.println("Abriendo nueva ventana: LoginView");
        currentView = new LoginView(this);
        currentView.setVisible(true);
    }

    public void showDashboardView(UserModel userModel) throws IOException {
        if (currentView != null) {
            System.out.println("Cerrando ventana actual: " + currentView.getTitle());
            currentView.dispose(); // Cerrar la ventana actual
            currentView = null;
        }
        System.out.println("Abriendo nueva ventana: DashboardView");
        currentView = new DashboardView(userModel);
        currentView.setVisible(true);
    }

    public void logout() {
        if (currentView != null) {
            System.out.println("Cerrando ventana actual: " + currentView.getTitle());
            currentView.dispose(); // Aseg�rate de cerrar la vista actual
            currentView = null;
        }
        System.out.println("Abriendo loginView");
        showLoginView(); // Abrir LoginView despu�s de cerrar DashboardView
    }


    public static void main(String[] args) {
        UserController.initialize("localhost", 12345); // Configurar servidor
        LoginController.getInstance();
    }
}