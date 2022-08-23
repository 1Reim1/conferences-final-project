package com.my.conferences.db;

import com.my.conferences.entity.User;

import java.sql.*;

public class UserRepository {

    private static final String GET_USER_BY_EMAIL =  "SELECT * FROM users WHERE email = ?";
    public static final String INSERT_INTO_USERS = "INSERT INTO users values (DEFAULT, ?, ?, ?, ?)";

    private static UserRepository instance;

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }

        return instance;
    }

    private UserRepository() {

    }

    public User findUserByEmail(Connection connection, String email) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(GET_USER_BY_EMAIL)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return extractUser(rs);
            }
        }
    }

    public void insertUser(Connection connection, User user) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_INTO_USERS, Statement.RETURN_GENERATED_KEYS)) {
            int k = 0;
            stmt.setString(++k, user.getEmail());
            stmt.setString(++k, user.getFirstName());
            stmt.setString(++k, user.getLastName());
            stmt.setString(++k, user.getPassHash());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                extractUser(rs, user);
            }
        }
    }

    private User extractUser(ResultSet rs) throws SQLException {
        User user = new User();
        return extractUser(rs, user);
    }
    private User extractUser(ResultSet rs, User user) throws SQLException {
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPassHash(rs.getString("passhash"));
        return user;
    }
}
