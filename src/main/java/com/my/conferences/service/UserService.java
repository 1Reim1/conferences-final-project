package com.my.conferences.service;

import com.my.conferences.dao.UserDao;
import com.my.conferences.entity.User;
import com.my.conferences.util.ConnectionUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Class with logic for interaction with users
 */
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * represents login operation
     *
     * @param email email of user
     * @param password password of user
     * @param language language to save its in database
     * @return User with these email and password
     */
    public User login(String email, String password, String language) throws DBException, ValidationException {
        User.validateEmailAndPassword(email, password);
        Connection connection = ConnectionUtil.getConnection();
        User user;
        try {
            user = userDao.findByEmail(connection, email);
            if (!user.getLanguage().equals(language)) {
                user.setLanguage(language);
                userDao.update(connection, user);
            }
        } catch (SQLException e) {
            throw new DBException("User with this email not found", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }

        if (!encryptPassword(password).equals(user.getPassword()))
            throw new DBException("Password is incorrect");

        return user;
    }

    /**
     * represents register operation
     *
     * @param user user which should be registered
     */
    public void register(User user) throws DBException, ValidationException {
        user.validateNames();
        user.validateEmailAndPassword();
        user.setPassword(encryptPassword(user.getPassword()));
        Connection connection = ConnectionUtil.getConnection();
        boolean userExists = true;
        try {
            userDao.findByEmail(connection, user.getEmail());
        } catch (SQLException e) {
            userExists = false;
        }

        if (userExists) {
            ConnectionUtil.closeConnection(connection);
            throw new DBException("The user with this email already exists");
        }

        try {
            userDao.insert(connection, user);
        } catch (SQLException e) {
            throw new DBException("The user is not inserted", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    /**
     * Changes a language for user
     */
    public void setLanguage(User user, String language) throws DBException {
        user.setLanguage(language);
        Connection connection = ConnectionUtil.getConnection();
        try {
            userDao.update(connection, user);
        } catch (SQLException e) {
            throw new DBException("Unable to change a language");
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    /**
     * Returns the list of users with 'speaker' role,
     * which are not participants of the event
     *
     * @param eventId id of the event
     * @param searchQuery part of name of speaker
     * @param user user that performs search
     * @return list of speakers
     */
    public List<User> searchAvailableSpeakers(int eventId, String searchQuery, User user) throws DBException, ValidationException {
        if (user.getRole() != User.Role.MODERATOR)
            throw new ValidationException("You have not permissions");

        Connection connection = ConnectionUtil.getConnection();
        try {
            return userDao.findAllAvailableSpeakersByEmail(connection, eventId, searchQuery);
        } catch (SQLException e) {
            throw new DBException("not found", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    private String encryptPassword(String password) {
        String encrypted = null;
        try {
            MessageDigest m = MessageDigest.getInstance("SHA-512");
            m.update(password.getBytes());
            byte[] bytes = m.digest();
            StringBuilder s = new StringBuilder();
            for (byte aByte : bytes)  {
                s.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            encrypted = s.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return encrypted;
    }
}
