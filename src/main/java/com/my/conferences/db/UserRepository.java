package com.my.conferences.db;

import com.my.conferences.entity.User;
import com.my.conferences.logic.UserManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {

    private static final String GET_USER_BY_EMAIL =  "SELECT * FROM users WHERE email = ?";

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

    private User extractUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPassHash(rs.getString("passhash"));
        return user;
    }
}
