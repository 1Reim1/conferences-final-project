package com.my.conferences.logic;

import com.my.conferences.db.ConnectionManager;
import com.my.conferences.db.DBException;
import com.my.conferences.db.UserRepository;
import com.my.conferences.entity.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class UserManager {

    private static UserManager instance;
    private static final ConnectionManager connectionManager = ConnectionManager.getInstance();
    private static final UserRepository userRepository = UserRepository.getInstance();

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }

        return instance;
    }

    private UserManager() {
    }

    public User login(String email, String password, String language) throws DBException, ValidationException {
        User.validateEmailAndPassword(email, password);
        Connection connection = connectionManager.getConnection();
        User user;
        try {
            user = userRepository.findByEmail(connection, email);
            if (!user.getLanguage().equals(language)) {
                user.setLanguage(language);
                userRepository.update(connection, user);
            }
        } catch (SQLException e) {
            throw new DBException("User with this email not found", e);
        } finally {
            connectionManager.closeConnection(connection);
        }

        if (!encryptPassword(password).equals(user.getPassword()))
            throw new DBException("Password is incorrect");

        return user;
    }

    public void register(User user) throws DBException, ValidationException {
        user.validateNames();
        user.validateEmailAndPassword();
        user.setPassword(encryptPassword(user.getPassword()));
        Connection connection = connectionManager.getConnection();
        boolean userExists = true;
        try {
            userRepository.findByEmail(connection, user.getEmail());
        } catch (SQLException e) {
            userExists = false;
        }

        if (userExists) {
            connectionManager.closeConnection(connection);
            throw new DBException("The user with this email already exists");
        }

        try {
            userRepository.insert(connection, user);
        } catch (SQLException e) {
            throw new DBException("The user is not inserted", e);
        } finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void setLanguage(User user, String language) throws DBException {
        user.setLanguage(language);
        Connection connection = connectionManager.getConnection();
        try {
            userRepository.update(connection, user);
        } catch (SQLException e) {
            throw new DBException("Unable to change a language");
        } finally {
            connectionManager.closeConnection(connection);
        }
    }

    public List<User> searchAvailableSpeakers(int eventId, String searchQuery, User user) throws DBException, ValidationException {
        if (user.getRole() != User.Role.MODERATOR)
            throw new ValidationException("You have not permissions");

        Connection connection = connectionManager.getConnection();
        try {
            return userRepository.findAllAvailableSpeakersByEmail(connection, eventId, searchQuery);
        } catch (SQLException e) {
            throw new DBException("not found", e);
        } finally {
            connectionManager.closeConnection(connection);
        }
    }

    private String encryptPassword(String password) {
        String encrypted = null;
        try {
            MessageDigest m = MessageDigest.getInstance("SHA-512");
            m.update(password.getBytes());
            byte[] bytes = m.digest();
            StringBuilder s = new StringBuilder();
            for (byte aByte : bytes) s.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            encrypted = s.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return encrypted;
    }
}
