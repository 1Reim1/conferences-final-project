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

    public User login(String email, String password) throws DBException {
        User.validateEmailAndPassword(email, password);
        Connection connection = connectionManager.getConnection();
        User user;
        try {
            user = userRepository.findByEmail(connection, email);
        } catch (SQLException e) {
            throw new DBException("User with this email not found", e);
        } finally {
            connectionManager.closeConnection(connection);
        }

        if (!encryptPassword(password).equals(user.getPassHash()))
            throw new DBException("Password is incorrect");

        return user;
    }

    public void register(User user) throws DBException {
        user.validateNames();
        user.validateEmailAndPassword();
        user.setPassHash(encryptPassword(user.getPassHash()));
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
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    public List<User> searchAvailableSpeakers(int eventId, String searchQuery, User user) throws DBException {
        if (user.getRole() != User.Role.MODERATOR)
            throw new DBException("You have not permissions");
        Connection connection = connectionManager.getConnection();
        try {
            return userRepository.findAllAvailableSpeakersByEmail(connection, eventId, searchQuery);
        } catch (SQLException e) {
            throw new DBException("not found", e);
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    private String encryptPassword(String password) {
        String encrypted = null;
        try
        {
            MessageDigest m = MessageDigest.getInstance("SHA-512");
            m.update(password.getBytes());
            byte[] bytes = m.digest();
            StringBuilder s = new StringBuilder();
            for(int i=0; i< bytes.length ;i++)
                s.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
            encrypted = s.toString();
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

        return encrypted;
    }
}
