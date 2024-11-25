package com.chat.cliente.model;

import java.io.*;
import java.net.Socket;

import com.chat.shared.UserProfileModel;

public class UserModel {
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    public UserModel(String serverHost, int serverPort) throws IOException {
        this.socket = new Socket(serverHost, serverPort);
        this.output = new ObjectOutputStream(socket.getOutputStream());
        this.input = new ObjectInputStream(socket.getInputStream());
    }

    public boolean login(String username, String password) {
        try {
            output.writeObject("LOGIN");
            output.writeObject(username);
            output.writeObject(password);

            String response = (String) input.readObject();
            return response.startsWith("LOG_SUCCESS");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean logout() {
        try {
            output.writeObject("LOGOUT");
            output.flush();
            String response = (String) input.readObject();
            return "LOGOUT_SUCCESS".equals(response);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public UserProfileModel getUserProfile() throws IOException, ClassNotFoundException {
        System.out.println("Sending profile request...");
        output.writeObject("GET_PROFILE");
        Object response = input.readObject();
        System.out.println("Response received.");

        if (response instanceof UserProfileModel) {
            return (UserProfileModel) response;
        } else if (response instanceof String) {
            System.err.println("Error: " + response);
            return null;
        } else {
            throw new ClassCastException("Unexpected response from server: " + response.getClass().getName());
        }
    }

    public boolean register(String username, String email, String password) {
        try {
            output.writeObject("REGISTER");
            output.writeObject(username);
            output.writeObject(email);
            output.writeObject(password);

            String response = (String) input.readObject();
            return response.startsWith("Registro exitoso");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        try {
            if (output != null) output.close();
            if (input != null) input.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
