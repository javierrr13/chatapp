package com.chat.servidor.dao;

import com.chat.servidor.model.User;
import com.chat.servidor.util.DatabaseConfig;
import com.chat.shared.UserProfileModel;

import java.sql.*;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    @Override
    public boolean registerUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<User> getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("password")
                );
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return storedPassword.equals(password); // Mejor usar hashing en producción
            }
        }
        return false;
    }

    /**
     * Obtener el perfil del usuario por ID.
     * @param userId ID del usuario.
     * @return Perfil del usuario.
     */
    public UserProfileModel getUserProfile(int userId) {
        String query = "SELECT * FROM user_profiles WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new UserProfileModel(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("full_name"),
                    rs.getString("bio"),
                    rs.getString("profile_picture"),
                    rs.getString("created_at")
                );
            }
        } catch (SQLException e) { 
            e.printStackTrace();
        }
        return null; // Si no existe el perfil
    }

    /**
     * Actualizar el perfil del usuario.
     * @param userId ID del usuario.
     * @param profile Perfil actualizado.
     * @return `true` si se actualizó correctamente, `false` en caso contrario.
     */
    public boolean updateUserProfile(int userId, UserProfileModel profile) {
        String query = "UPDATE user_profiles SET full_name = ?, bio = ?, profile_picture = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, profile.getFullname());
            stmt.setString(2, profile.getBio());
            stmt.setString(3, profile.getProfilePicture());
            stmt.setInt(4, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Crear un nuevo perfil para un usuario.
     * @param userId ID del usuario.
     * @param profile Datos del nuevo perfil.
     * @return `true` si se creó correctamente, `false` en caso contrario.
     */
    public boolean insertUserProfile(int userId, UserProfileModel profile) {
        String query = "INSERT INTO user_profiles (user_id, full_name, bio, profile_picture, created_at) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, profile.getFullname());
            stmt.setString(3, profile.getBio());
            stmt.setString(4, profile.getProfilePicture());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Inserta o actualiza un perfil de usuario.
     * @param userId ID del usuario.
     * @param profile Perfil a insertar o actualizar.
     * @return `true` si la operación fue exitosa, `false` en caso contrario.
     */
    public boolean insertOrUpdateUserProfile(int userId, UserProfileModel profile) {
        String checkQuery = "SELECT COUNT(*) FROM user_profiles WHERE user_id = ?";
        String insertQuery = "INSERT INTO user_profiles (user_id, full_name, bio, profile_picture, created_at) VALUES (?, ?, ?, ?, NOW())";
        String updateQuery = "UPDATE user_profiles SET full_name = ?, bio = ?, profile_picture = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConfig.getConnection()) {
            // Verificar si ya existe un perfil
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, userId);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    // Si existe, actualizar
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, profile.getFullname());
                        updateStmt.setString(2, profile.getBio());
                        updateStmt.setString(3, profile.getProfilePicture());
                        updateStmt.setInt(4, userId);
                        return updateStmt.executeUpdate() > 0;
                    }
                } else {
                    // Si no existe, insertar
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, userId);
                        insertStmt.setString(2, profile.getFullname());
                        insertStmt.setString(3, profile.getBio());
                        insertStmt.setString(4, profile.getProfilePicture());
                        return insertStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
