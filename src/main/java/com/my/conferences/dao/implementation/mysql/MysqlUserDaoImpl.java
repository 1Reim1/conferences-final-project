package com.my.conferences.dao.implementation.mysql;

import com.my.conferences.dao.UserDao;
import com.my.conferences.dao.implementation.JdbcTemplate;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Mysql implementation of UserDao interface
 */
public class MysqlUserDaoImpl implements UserDao {

    private static final String GET_ALL = "SELECT * FROM users WHERE email LIKE ? AND id != ? ORDER BY id LIMIT ? OFFSET ?";
    private static final String GET_BY_EMAIL = "SELECT * FROM users WHERE email = ?";
    private static final String INSERT_ONE = "INSERT INTO users values (DEFAULT, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_ONE = "UPDATE users SET email = ?, first_name = ?, last_name = ?, password = ?, role = ?, language = ? WHERE id = ?";
    private static final String GET_ALL_PARTICIPANTS = "SELECT * FROM users WHERE id IN (SELECT user_id FROM participants WHERE event_id = ?) ORDER BY first_name, last_name, id";
    private static final String GET_ONE = "SELECT * FROM users WHERE id = ?";
    private static final String GET_ALL_AVAILABLE_SPEAKERS_BY_EMAIL = "SELECT * FROM users WHERE role = 'SPEAKER' AND id NOT IN (SELECT user_id FROM participants WHERE event_id = ?) AND email LIKE ?";
    private static final String INSERT_PARTICIPANT = "INSERT INTO participants VALUES (?, ?)";
    private static final String DELETE_PARTICIPANT = "DELETE FROM participants WHERE user_id = ? AND event_id = ?";

    /**
     * returns all users from the database except itself
     *
     * @param connection db connection
     * @param page       page
     * @param pageSize   size of page
     * @param user       user that performs action
     * @return list of users sorted by id
     */
    @Override
    public List<User> findAllExceptItself(Connection connection, String emailQuery, int page, int pageSize, User user) throws SQLException {
        return JdbcTemplate.query(
                connection,
                GET_ALL, this::extractUser,
                "%" + emailQuery + "%",
                user.getId(),
                pageSize,
                (page - 1) * pageSize);
    }

    /**
     * fills user fields from database
     *
     * @param connection db connection
     * @param user       user with id
     */
    @Override
    public void findOne(Connection connection, User user) throws SQLException {
        User result = JdbcTemplate
                .query(connection, GET_ONE, this::extractUser, user.getId())
                .stream().findAny().orElseThrow(SQLException::new);
        user.setEmail(result.getEmail());
        user.setFirstName(result.getFirstName());
        user.setLastName(result.getLastName());
        user.setPassword(result.getPassword());
        user.setRole(result.getRole());
        user.setLanguage(result.getLanguage());
    }

    /**
     * returns user with that email
     *
     * @param connection db connection
     * @param email      email of user
     * @return user
     */
    @Override
    public User findByEmail(Connection connection, String email) throws SQLException {
        return JdbcTemplate
                .query(connection, GET_BY_EMAIL, this::extractUser, email)
                .stream().findAny().orElseThrow(SQLException::new);
    }

    /**
     * saves user to database
     *
     * @param connection db connection
     * @param user       user which should be saved
     */
    @Override
    public void insert(Connection connection, User user) throws SQLException {
        PreparedStatement statement = JdbcTemplate.update(
                connection,
                INSERT_ONE,
                user.getEmail(),
                user.getEmail(),
                user.getLastName(),
                user.getPassword(),
                user.getRole().toString(),
                user.getLanguage());
        try (ResultSet rs = statement.getGeneratedKeys()) {
            rs.next();
            user.setId(rs.getInt(1));
        }
        statement.close();
    }

    /**
     * updates user in database
     *
     * @param connection db connection
     * @param user       user which should be updated
     */
    @Override
    public void update(Connection connection, User user) throws SQLException {
        JdbcTemplate.update(
                connection,
                UPDATE_ONE,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPassword(),
                user.getRole().toString(),
                user.getLanguage(),
                user.getId()).close();
    }

    /**
     * returns list of speakers which are not participants by query
     *
     * @param connection  db connection
     * @param eventId     id of event
     * @param searchQuery query
     * @return list of speakers
     */
    @Override
    public List<User> findAllAvailableSpeakersByEmail(Connection connection, int eventId, String searchQuery) throws SQLException {
        return JdbcTemplate.query(connection, GET_ALL_AVAILABLE_SPEAKERS_BY_EMAIL, this::extractUser, eventId, "%" + searchQuery + "%");
    }

    /**
     * saves all participants to event.participants field
     *
     * @param connection db connection
     * @param event      event with id
     */
    @Override
    public void findAllParticipants(Connection connection, Event event) throws SQLException {
        List<User> participants = JdbcTemplate.query(connection, GET_ALL_PARTICIPANTS, this::extractUser, event.getId());
        event.setParticipants(participants);
    }

    /**
     * adds user to participants of event
     *
     * @param connection db connection
     * @param event      event
     * @param user       user
     */
    @Override
    public void insertParticipant(Connection connection, Event event, User user) throws SQLException {
        JdbcTemplate.update(connection, INSERT_PARTICIPANT, user.getId(), event.getId()).close();
    }

    /**
     * removes user from participants of event
     *
     * @param connection db connection
     * @param event      event
     * @param user       user
     */
    @Override
    public void deleteParticipant(Connection connection, Event event, User user) throws SQLException {
        JdbcTemplate.update(connection, DELETE_PARTICIPANT, user.getId(), event.getId()).close();
    }

    private User extractUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setPassword(rs.getString("password"));
        user.setRole(User.Role.valueOf(rs.getString("role").toUpperCase()));
        user.setLanguage(rs.getString("language"));
        return user;
    }
}
