package com.my.conferences.dao.implementation.mysql;

import com.my.conferences.dao.UserDao;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Mysql implementation of UserDao interface
 */
public class MysqlUserDaoImpl implements UserDao {

    private static final String GET_BY_EMAIL = "SELECT * FROM users WHERE email = ?";
    private static final String INSERT_ONE = "INSERT INTO users values (DEFAULT, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_ONE = "UPDATE users SET email = ?, first_name = ?, last_name = ?, password = ?, role = ?, language = ? WHERE id = ?";
    private static final String GET_ALL_PARTICIPANTS = "SELECT * FROM users WHERE id IN (SELECT user_id FROM participants WHERE event_id = ?) ORDER BY first_name, last_name, id";
    private static final String GET_ONE = "SELECT * FROM users WHERE id = ?";
    private static final String GET_ALL_AVAILABLE_SPEAKERS_BY_EMAIL = "SELECT * FROM users WHERE role = 'SPEAKER' AND id NOT IN (SELECT user_id FROM participants WHERE event_id = ?) AND email LIKE ?";
    private static final String INSERT_PARTICIPANT = "INSERT INTO participants VALUES (?, ?)";
    private static final String DELETE_PARTICIPANT = "DELETE FROM participants WHERE user_id = ? AND event_id = ?";

    /**
     * fills user fields from database
     *
     * @param user user with id
     */
    @Override
    public void findOne(Connection connection, User user) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(GET_ONE)) {
            stmt.setInt(1, user.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                extractUser(rs, user);
            }
        }
    }

    /**
     * returns user with that email
     *
     * @param email email of user
     * @return user
     */
    @Override
    public User findByEmail(Connection connection, String email) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(GET_BY_EMAIL)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return extractUser(rs);
            }
        }
    }

    /**
     * inserts user into 'users' table
     *
     * @param user user which should be inserted
     */
    @Override
    public void insert(Connection connection, User user) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_ONE, Statement.RETURN_GENERATED_KEYS)) {
            prepareStatementForUser(stmt, user);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                user.setId(rs.getInt(1));
            }
        }
    }

    /**
     * updates user
     *
     * @param user user which should be updated
     */
    @Override
    public void update(Connection connection, User user) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_ONE, Statement.RETURN_GENERATED_KEYS)) {
            int k = prepareStatementForUser(stmt, user);
            stmt.setInt(++k, user.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * returns list of speakers which are not participants by query
     *
     * @param eventId     id of event
     * @param searchQuery query
     * @return list of speaker
     */
    @Override
    public List<User> findAllAvailableSpeakersByEmail(Connection connection, int eventId, String searchQuery) throws SQLException {
        List<User> speakers = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(GET_ALL_AVAILABLE_SPEAKERS_BY_EMAIL)) {
            stmt.setInt(1, eventId);
            stmt.setString(2, "%" + searchQuery + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    speakers.add(extractUser(rs));
                }
            }
        }

        return speakers;
    }

    /**
     * saves all participants to event.participants field
     *
     * @param event event with id
     */
    @Override
    public void findAllParticipants(Connection connection, Event event) throws SQLException {
        List<User> participants = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(GET_ALL_PARTICIPANTS)) {
            stmt.setInt(1, event.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    participants.add(extractUser(rs));
                }
            }
        }

        event.setParticipants(participants);
    }

    /**
     * inserts user to 'participants' table
     *
     * @param event event
     * @param user  user
     */
    @Override
    public void insertParticipant(Connection connection, Event event, User user) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_PARTICIPANT)) {
            stmt.setInt(1, user.getId());
            stmt.setInt(2, event.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * delete user from 'participants' table
     * @param event event
     * @param user  user
     */
    @Override
    public void deleteParticipant(Connection connection, Event event, User user) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_PARTICIPANT)) {
            stmt.setInt(1, user.getId());
            stmt.setInt(2, event.getId());
            stmt.executeUpdate();
        }
    }

    private int prepareStatementForUser(PreparedStatement stmt, User user) throws SQLException {
        int k = 0;
        stmt.setString(++k, user.getEmail());
        stmt.setString(++k, user.getFirstName());
        stmt.setString(++k, user.getLastName());
        stmt.setString(++k, user.getPassword());
        stmt.setString(++k, user.getRole().toString());
        stmt.setString(++k, user.getLanguage());
        return k;
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
        user.setPassword(rs.getString("password"));
        user.setRole(User.Role.valueOf(rs.getString("role").toUpperCase()));
        user.setLanguage(rs.getString("language"));
    }
}
