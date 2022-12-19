package com.my.conferences.dao.implementation.mysql;

import com.my.conferences.dao.VerificationCodeDao;
import com.my.conferences.dao.implementation.JdbcTemplate;
import com.my.conferences.entity.User;
import com.my.conferences.entity.VerificationCode;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MysqlVerificationCodeDaoImpl implements VerificationCodeDao {

    private static final String GET_ONE = "SELECT * FROM verification_codes WHERE user_id = ?";
    private static final String INSERT_ONE = "INSERT INTO verification_codes VALUES (?, ?)";
    private static final String DELETE_ONE = "DELETE FROM verification_codes WHERE user_id = ?";

    /**
     * returns verification code for user
     *
     * @param connection db connection
     * @param user       user
     * @return verifivation code
     */
    @Override
    public VerificationCode findOne(Connection connection, User user) throws SQLException {
//        try (PreparedStatement stmt = connection.prepareStatement(GET_ONE)) {
//            stmt.setInt(1, user.getId());
//            try (ResultSet rs = stmt.executeQuery()) {
//                rs.next();
//                return extractVerificationCode(rs, user);
//            }
//        }
        VerificationCode verificationCode = JdbcTemplate
                .query(connection, GET_ONE, this::extractVerificationCode, user.getId())
                .stream().findAny().orElseThrow(SQLException::new);
        verificationCode.setUser(user);
        return verificationCode;
    }

    /**
     * saves verification code to database
     *
     * @param connection       db connection
     * @param verificationCode verification code
     */
    @Override
    public void insert(Connection connection, VerificationCode verificationCode) throws SQLException {
        JdbcTemplate.update(connection, INSERT_ONE, verificationCode.getCode(), verificationCode.getUser()).close();
    }

    /**
     * deletes verification code from database
     *
     * @param connection db connection
     * @param user       user
     */
    @Override
    public void delete(Connection connection, User user) throws SQLException {
        JdbcTemplate.update(connection, DELETE_ONE, user.getId()).close();
    }

    private VerificationCode extractVerificationCode(ResultSet rs) throws SQLException {
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setCode(rs.getString("code"));
        return verificationCode;
    }
}
