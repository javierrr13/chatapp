package com.chat.cliente.controller;
import java.io.IOException;

import com.chat.cliente.model.UserModel;



public class UserController {
    private static UserModel userModel;

    public static void initialize(String serverHost, int serverPort) {
        try {
            userModel = new UserModel(serverHost, serverPort);
        } catch (IOException e) {
            System.err.println("No se pudo conectar al servidor: " + e.getMessage());
            System.exit(1);
        }
    }

    public static boolean handleLogin(String username, String password) {
        return userModel.login(username, password);
    }

    public static boolean handleRegister(String username, String email, String password) {
        return userModel.register(username, email, password);
    }

    public static void closeConnection() {
        userModel.close();
    }
}
