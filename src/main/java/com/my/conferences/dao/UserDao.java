package com.my.conferences.dao;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Dao interface for interaction with users in storage
 */
public interface UserDao {
    /**
     * fills user fields from storage
     *
     * @param user user with id
     */
    void findOne(Connection connection, User user) throws SQLException;

    /**
     * returns user with that email
     *
     * @param email email of user
     * @return user
     */
    User findByEmail(Connection connection, String email) throws SQLException;

    /**
     * saves user to storage
     *
     * @param user user which should be saved
     */
    void insert(Connection connection, User user) throws SQLException;

    /**
     * updates user in storage
     *
     * @param user user which should be updated
     */
    void update(Connection connection, User user) throws SQLException;

    /**
     * returns list of speakers which are not participants by query
     *
     * @param eventId     id of event
     * @param searchQuery query
     * @return list of speakers
     */
    List<User> findAllAvailableSpeakersByEmail(Connection connection, int eventId, String searchQuery) throws SQLException;

    /**
     * saves all participants to event.participants field
     *
     * @param event event with id
     */
    void findAllParticipants(Connection connection, Event event) throws SQLException;

    /**
     * add user to participants of event
     *
     * @param event event
     * @param user  user
     */
    void insertParticipant(Connection connection, Event event, User user) throws SQLException;

    /**
     * removes user from participants of event
     *
     * @param event event
     * @param user  user
     */
    void deleteParticipant(Connection connection, Event event, User user) throws SQLException;

}
