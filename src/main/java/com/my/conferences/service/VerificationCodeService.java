package com.my.conferences.service;

import com.my.conferences.dao.UserDao;
import com.my.conferences.dao.VerificationCodeDao;
import com.my.conferences.email.EmailManager;
import com.my.conferences.entity.User;
import com.my.conferences.entity.VerificationCode;
import com.my.conferences.exception.DBException;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.validation.UserValidation;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

/**
 * Class with logic for interaction with verification codes
 */
public class VerificationCodeService {

    private static final Logger logger = Logger.getLogger(VerificationCode.class);
    private final EmailManager emailManager;
    private final ConnectionManager connectionManager;
    private final VerificationCodeDao verificationCodeDao;
    private final UserDao userDao;

    public VerificationCodeService(EmailManager emailManager, ConnectionManager connectionManager, VerificationCodeDao verificationCodeDao, UserDao userDao) {
        this.emailManager = emailManager;
        this.connectionManager = connectionManager;
        this.verificationCodeDao = verificationCodeDao;
        this.userDao = userDao;
    }

    /**
     * sends code to email
     * if code not exists in database, that will be generated and saved to database
     *
     * @param userEmail email of user
     * @param language language of email
     */
    public void sendCode(String userEmail, String language) throws DBException, ValidationException {
        UserValidation.validateEmail(userEmail);
        Connection connection = this.connectionManager.getConnection();
        User user;
        try {
            user = userDao.findByEmail(connection, userEmail);
        } catch (SQLException e) {
            logger.error("SQLException in sendCode", e);
            this.connectionManager.closeConnection(connection);
            throw new DBException("User with that email not found", e);
        }

        VerificationCode verificationCode;
        try {
            verificationCode = verificationCodeDao.findOne(connection, user);
        } catch (SQLException e) {
            logger.error("SQLException in sendCode", e);
            logger.debug("Verification code was not found. Generating new");
            // verification code was not found
            // generate and save new
            verificationCode = new VerificationCode();
            verificationCode.setCode(generateCode());
            verificationCode.setUser(user);
            try {
                verificationCodeDao.insert(connection, verificationCode);
            } catch (SQLException ex) {
                logger.error("SQLException in sendCode", ex);
                throw new DBException("Unable to save verification code");
            }
        }   finally {
            this.connectionManager.closeConnection(connection);
        }

        user.setLanguage(language);
        emailManager.sendVerificationCode(verificationCode);
    }

    /**
     * @param userEmail email of the user whose code will be verified
     * @param code code
     * @return true if code is correct
     */
    public boolean verifyCode(String userEmail, String code) throws DBException, ValidationException {
        UserValidation.validateEmail(userEmail);
        Connection connection = this.connectionManager.getConnection();
        User user;
        try {
            user = userDao.findByEmail(connection, userEmail);
        } catch (SQLException e) {
            logger.error("SQLException in verifyCode", e);
            this.connectionManager.closeConnection(connection);
            throw new DBException("User with that email not found", e);
        }

        try {
            VerificationCode verificationCode = verificationCodeDao.findOne(connection, user);
            return verificationCode.getCode().equals(code);
        } catch (SQLException e) {
            logger.error("SQLException in verifyCode", e);
            throw new DBException("Verification code was not found");
        }   finally {
            this.connectionManager.closeConnection(connection);
        }
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
