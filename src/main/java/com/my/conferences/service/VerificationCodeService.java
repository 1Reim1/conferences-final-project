package com.my.conferences.service;

import com.my.conferences.dao.UserDao;
import com.my.conferences.dao.VerificationCodeDao;
import com.my.conferences.email.EmailManager;
import com.my.conferences.entity.User;
import com.my.conferences.entity.VerificationCode;
import com.my.conferences.exception.DBException;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.util.ConnectionUtil;
import com.my.conferences.validation.UserValidation;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

public class VerificationCodeService {

    private final static Logger logger = Logger.getLogger(VerificationCode.class);
    private final EmailManager emailManager;
    private final VerificationCodeDao verificationCodeDao;
    private final UserDao userDao;

    public VerificationCodeService(EmailManager emailManager, VerificationCodeDao verificationCodeDao, UserDao userDao) {
        this.emailManager = emailManager;
        this.verificationCodeDao = verificationCodeDao;
        this.userDao = userDao;
    }

    public void sendCode(String userEmail, String language) throws DBException, ValidationException {
        UserValidation.validateEmail(userEmail);
        Connection connection = ConnectionUtil.getConnection();
        User user;
        try {
            user = userDao.findByEmail(connection, userEmail);
        } catch (SQLException e) {
            logger.error("SQLException in sendCode", e);
            ConnectionUtil.closeConnection(connection);
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
            ConnectionUtil.closeConnection(connection);
        }

        user.setLanguage(language);
        emailManager.sendVerificationCode(verificationCode);
    }

    public boolean verifyCode(String userEmail, String code) throws DBException, ValidationException {
        UserValidation.validateEmail(userEmail);
        Connection connection = ConnectionUtil.getConnection();
        User user;
        try {
            user = userDao.findByEmail(connection, userEmail);
        } catch (SQLException e) {
            logger.error("SQLException in verifyCode", e);
            throw new DBException("User with that email not found", e);
        }

        try {
            VerificationCode verificationCode = verificationCodeDao.findOne(connection, user);
            return verificationCode.getCode().equals(code);
        } catch (SQLException e) {
            logger.error("SQLException in verifyCode", e);
            throw new DBException("Verification code was not found");
        }
    }

    private String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
