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
     * @param user user
     * @return verifivation code
     */
    VerificationCode findOne(Connection connection, User user) throws SQLException;

    /**
     * saves verification code to storage
     *
     * @param verificationCode verification code
     */
    void insert(Connection connection, VerificationCode verificationCode) throws SQLException;

    /**
     * deletes verification code from storage
     *
     * @param user user
     */
    void delete(Connection connection, User user) throws SQLException;
}
