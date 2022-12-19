package com.my.conferences.service;

import com.my.conferences.dao.EventDao;
import com.my.conferences.dao.ReportDao;
import com.my.conferences.dao.UserDao;
import com.my.conferences.dao.VerificationCodeDao;
import com.my.conferences.email.EmailManager;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;
import com.my.conferences.entity.VerificationCode;
import com.my.conferences.exception.DBException;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.util.ConnectionUtil;
import com.my.conferences.validation.UserValidation;
import org.apache.log4j.Logger;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Class with logic for interaction with users
 */
public class UserService {

    private static final Logger logger = Logger.getLogger(UserService.class);
    private final EmailManager emailManager;
    private final UserDao userDao;
    private final ReportDao reportDao;
    private final EventDao eventDao;
    private final VerificationCodeDao verificationCodeDao;
    private final int pageSize;

    public UserService(EmailManager emailManager, UserDao userDao, ReportDao reportDao, EventDao eventDao, VerificationCodeDao verificationCodeDao, int pageSize) {
        this.emailManager = emailManager;
        this.userDao = userDao;
        this.reportDao = reportDao;
        this.eventDao = eventDao;
        this.verificationCodeDao = verificationCodeDao;
        this.pageSize = pageSize;
    }

    /**
     * @param emailQuery query for searching
     * @param page page
     * @param user user that performs action
     * @return list of users
     */
    public List<User> findAll(String emailQuery, int page, User user) throws DBException, ValidationException {
        if (user.getRole() != User.Role.MODERATOR) {
            throw new ValidationException("You have not permissions");
        }

        Connection connection = ConnectionUtil.getConnection();
        try {
            return userDao.findAllExceptItself(connection, emailQuery, page, pageSize, user);
        } catch (SQLException e) {
            logger.error("SQLException in findAll", e);
            throw new DBException("not found", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    /**
     * represents login operation
     *
     * @param email    email of user
     * @param password password of user
     * @param language language to save its in database
     * @return user with these email and password
     */
    public User login(String email, String password, String language) throws DBException, ValidationException {
        UserValidation.validateEmailAndPassword(email, password);
        Connection connection = ConnectionUtil.getConnection();
        User user;
        try {
            user = userDao.findByEmail(connection, email);
            if (!user.getLanguage().equals(language)) {
                logger.debug("Updating user’s language");
                user.setLanguage(language);
                userDao.update(connection, user);
            }
        } catch (SQLException e) {
            logger.error("SQLException in login", e);
            throw new DBException("User with that email not found", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }

        if (!encryptPassword(password).equals(user.getPassword())) {
            throw new ValidationException("Password is wrong");
        }

        return user;
    }

    /**
     * represents register operation
     *
     * @param user user which should be registered
     */
    public void register(User user) throws DBException, ValidationException {
        if (user.getRole() == User.Role.MODERATOR) {
            throw new ValidationException("Moderator can not be register");
        }

        UserValidation.validateNames(user.getFirstName(), user.getLastName());
        UserValidation.validateEmailAndPassword(user.getEmail(), user.getPassword());
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
            logger.error("SQLException in register", e);
            throw new DBException("The user was not inserted", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    /**
     * Changes a role of user
     *
     * @param userId  id of the user whose role should be changed
     * @param newRole new role for user
     * @param me      user who performs action
     */
    public void modifyRole(int userId, User.Role newRole, User me) throws DBException, ValidationException {
        if (me.getRole() != User.Role.MODERATOR) {
            throw new ValidationException("You have not permission");
        }
        Connection connection = ConnectionUtil.getConnectionForTransaction();
        try {
            User user = new User();
            user.setId(userId);
            userDao.findOne(connection, user);
            if (user.getRole() == newRole) {
                throw new ValidationException("This role is already set");
            }

            if (user.getRole() == User.Role.SPEAKER) {
                for (Report report : reportDao.findAllBySpeaker(connection, user, true)) {
                    reportDao.delete(connection, report);
                    Event event = eventDao.findOne(connection, report.getEventId(), true);
                    // send email notification
                    if (report.isConfirmed()) {
                        for (Report r : event.getReports()) {
                            userDao.findOne(connection, r.getSpeaker());
                        }
                        userDao.findAllParticipants(connection, event);
                        userDao.findOne(connection, event.getModerator());
                        emailManager.sendConfirmedReportCancelled(report, event);
                    }
                }
            } else if (user.getRole() == User.Role.MODERATOR) {
                for (Event event : eventDao.findAllByModerator(connection, user)) {
                    event.setModerator(me);
                    eventDao.update(connection, event);
                }
            }

            user.setRole(newRole);
            userDao.update(connection, user);
            connection.commit();
        } catch (SQLException e) {
            logger.error("SQLException in modifyRole", e);
            ConnectionUtil.rollbackConnection(connection);
            throw new DBException("Role was not modified");
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    /**
     * changes password of user, if verification code is correct
     * if verification code is wrong, that will be deleted and ValidationException will be thrown
     *
     * @param email email of user
     * @param code verification code
     * @param newPassword new password
     * @return user with new password
     */
    public User modifyPassword(String email, String code, String newPassword) throws DBException, ValidationException {
        UserValidation.validateEmailAndPassword(email, newPassword);
        Connection connection = ConnectionUtil.getConnectionForTransaction();
        User user;
        try {
            user = userDao.findByEmail(connection, email);
        } catch (SQLException e) {
            logger.error("SQLException in modifyPassword", e);
            ConnectionUtil.closeConnection(connection);
            throw new DBException("User with that email not found", e);
        }
        try {
            VerificationCode verificationCode = verificationCodeDao.findOne(connection, user);
            if (!verificationCode.getCode().equals(code)) {
                // if code is wrong, delete that verification code
                verificationCodeDao.delete(connection, user);
                connection.commit();
                ConnectionUtil.closeConnection(connection);
                throw new ValidationException("Verification code is wrong");
            }
        } catch (SQLException e) {
            logger.error("SQLException in modifyPassword", e);
            ConnectionUtil.closeConnection(connection);
            throw new DBException("Verification code was not found");
        }
        try {
            verificationCodeDao.delete(connection, user);
            user.setPassword(encryptPassword(newPassword));
            userDao.update(connection, user);
            connection.commit();
            return user;
        } catch (SQLException e) {
            logger.error("SQLException in modifyPassword", e);
            ConnectionUtil.rollbackConnection(connection);
            throw new DBException("Unable to modify a password", e);
        }   finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    /**
     * Changes a language for user
     *
     * @param user user whose language should be changed
     * @param language new language
     */
    public void setLanguage(User user, String language) throws DBException {
        user.setLanguage(language);
        Connection connection = ConnectionUtil.getConnection();
        try {
            userDao.update(connection, user);
        } catch (SQLException e) {
            logger.error("SQLException in setLanguage", e);
            throw new DBException("Unable to change a language");
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    /**
     * Returns the list of users with 'speaker' role,
     * which are not participants of the event
     *
     * @param eventId     id of the event
     * @param searchQuery part of name of speaker
     * @param user        user that performs search
     * @return list of speakers
     */
    public List<User> searchAvailableSpeakers(int eventId, String searchQuery, User user) throws DBException, ValidationException {
        if (user.getRole() != User.Role.MODERATOR) {
            throw new ValidationException("You have not permissions");
        }

        Connection connection = ConnectionUtil.getConnection();
        try {
            return userDao.findAllAvailableSpeakersByEmail(connection, eventId, searchQuery);
        } catch (SQLException e) {
            logger.error("SQLException in searchAvailableSpeakers", e);
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
            for (byte aByte : bytes) {
                s.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            encrypted = s.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return encrypted;
    }
}
