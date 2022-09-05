package com.my.conferences.db;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    private static final String GET_BY_EMAIL =  "SELECT * FROM users WHERE email = ?";
    private static final String INSERT_INTO_USERS = "INSERT INTO users values (DEFAULT, ?, ?, ?, ?, ?)";
    private static final String GET_ALL_PARTICIPANTS = "SELECT * FROM users WHERE id IN (SELECT user_id FROM participants WHERE event_id = ?)";
    private static final String GET_ONE = "SELECT * FROM users WHERE id = ?";
    private static UserRepository instance;

    public static synchronized UserRepository getInstance() {
        if (instance == null) {
            instance = new UserRepository();
        }

        return instance;
    }

    private UserRepository() {

    }

    public void findOne(Connection connection, User user) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(GET_ONE)) {
            stmt.setInt(1, user.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                extractUser(rs, user);
            }
        }
    }

    public User findByEmail(Connection connection, String email) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(GET_BY_EMAIL)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return extractUser(rs);
            }
        }
    }

    public void insert(Connection connection, User user) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_INTO_USERS, Statement.RETURN_GENERATED_KEYS)) {
            int k = 0;
            stmt.setString(++k, user.getEmail());
            stmt.setString(++k, user.getFirstName());
            stmt.setString(++k, user.getLastName());
            stmt.setString(++k, user.getPassHash());
            stmt.setString(++k, user.getRole().toString());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                user.setId(rs.getInt(1));
            }
        }
    }
    public void findAllParticipants(Connection connection, Event event) throws SQLException {
        List<User> participants = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(GET_ALL_PARTICIPANTS)) {
            stmt.setInt(1, event.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    participants.add(extractUser(rs));

                event.setParticipants(participants);
            }
        }
    }

    private User extractUser(ResultSet rs) throws SQLException {
        User user = new User();
        extractUser(rs, user);
        return user;
    }
    private void extractUser(ResultSet rs, User user) throws SQLException {
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPassHash(rs.getString("passhash"));
        user.setRole(User.Role.valueOf(rs.getString("role").toUpperCase()));
    }
}
