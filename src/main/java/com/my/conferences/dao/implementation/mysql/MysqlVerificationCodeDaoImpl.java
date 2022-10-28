package com.my.conferences.dao.implementation.mysql;

import com.my.conferences.dao.VerificationCodeDao;
import com.my.conferences.entity.User;
import com.my.conferences.entity.VerificationCode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MysqlVerificationCodeDaoImpl implements VerificationCodeDao {

    private static final String GET_ONE = "SELECT * FROM verification_codes WHERE user_id = ?";
    private static final String INSERT_ONE = "INSERT INTO verification_codes VALUES (?, ?)";
    private static final String DELETE_ONE = "DELETE FROM verification_codes WHERE user_id = ?";

    @Override
    public VerificationCode findOne(Connection connection, User user) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(GET_ONE)) {
            stmt.setInt(1, user.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return extractVerificationCode(rs, user);
            }
        }
    }

    @Override
    public void insert(Connection connection, VerificationCode verificationCode) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_ONE)) {
            stmt.setString(1, verificationCode.getCode());
            stmt.setInt(2, verificationCode.getUser().getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(Connection connection, User user) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_ONE)) {
            stmt.setInt(1, user.getId());
            stmt.executeUpdate();
        }
    }

    private VerificationCode extractVerificationCode(ResultSet rs, User user) throws SQLException {
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setCode(rs.getString("code"));
        verificationCode.setUser(user);
        return verificationCode;
    }
}
