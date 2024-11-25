package com.chat.servidor.dao;

import com.chat.servidor.model.User;

import java.sql.SQLException;
import java.util.Optional;

public interface UserDAO {
    boolean registerUser(User user) throws SQLException;

    Optional<User> getUserByUsername(String username) throws SQLException;

    boolean authenticateUser(String username, String password) throws SQLException;
}
