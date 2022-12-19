package com.my.conferences.dao;

import com.my.conferences.entity.User;
import com.my.conferences.entity.VerificationCode;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Dao interface for interaction with verification codes in storage
 */
public interface VerificationCodeDao {

    /**
     * returns verification code for user
     *
     * @param connection db connection
     * @param user user
     * @return verifivation code
     */
    VerificationCode findOne(Connection connection, User user) throws SQLException;

    /**
     * saves verification code to database
     *
     * @param connection db connection
     * @param verificationCode verification code
     */
    void insert(Connection connection, VerificationCode verificationCode) throws SQLException;

    /**
     * deletes verification code from database
     *
     * @param connection db connection
     * @param user user
     */
    void delete(Connection connection, User user) throws SQLException;
}
