package com.my.conferences.dao;

import com.my.conferences.entity.User;
import com.my.conferences.entity.VerificationCode;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Dao interface for interaction with verification codes in storage
 */
public interface VerificationCodeDao {

    VerificationCode findOne(Connection connection, User user) throws SQLException;
    void insert(Connection connection, VerificationCode verificationCode) throws SQLException;
    void delete(Connection connection, User user) throws SQLException;
}
