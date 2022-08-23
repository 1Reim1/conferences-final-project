package com.my.conferences.logic;

import com.my.conferences.db.ConnectionManager;
import com.my.conferences.db.DBException;
import com.my.conferences.db.UserRepository;
import com.my.conferences.entity.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class UserManager {

    private static UserManager instance;
    private static final ConnectionManager connectionManager = ConnectionManager.getInstance();
    private static final UserRepository userRepository = UserRepository.getInstance();
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALID_NAME_REGEX = Pattern.compile("^[a-zA-Z]{3,}$");

    public static synchronized UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }

        return instance;
    }

    private UserManager() {

    }

    public User login(String email, String password) throws DBException {
        validate(email, password);
        Connection connection = connectionManager.getConnection();
        User user;
        try {
            user = userRepository.findUserByEmail(connection, email);
        } catch (SQLException e) {
            throw new DBException("User with this email not found", e);
        }

        if (!encryptPassword(password).equals(user.getPassHash()))
            throw new DBException("Password is incorrect");

        connectionManager.closeConnection(connection);
        return user;
    }

    public void register(User user) throws DBException {
        validate(user);
        user.setPassHash(encryptPassword(user.getPassHash()));
        Connection connection = connectionManager.getConnection();
        try {
            userRepository.insertUser(connection, user);
        } catch (SQLException e) {
            throw new DBException("The user is not inserted", e);
        }

        connectionManager.closeConnection(connection);
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

    private void validate(String email, String password) throws DBException {
        if (!VALID_EMAIL_ADDRESS_REGEX.matcher(email).find())
            throw new DBException("Email is bad");
        if (password.length() < 6) {
            throw new DBException("Password is bad");
        }
    }

    private void validate(User user) throws DBException {
        validate(user.getEmail(), user.getPassHash());
        if (!VALID_NAME_REGEX.matcher(user.getFirstName()).find())
            throw new DBException("First name is bad");
        if (!VALID_NAME_REGEX.matcher(user.getLastName()).find())
            throw new DBException("Last name is bad");
    }
}
