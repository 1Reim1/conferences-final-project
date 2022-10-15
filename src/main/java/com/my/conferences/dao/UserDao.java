package com.my.conferences.dao;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface UserDao {
    void findOne(Connection connection, User user) throws SQLException;

    User findByEmail(Connection connection, String email) throws SQLException;

    void insert(Connection connection, User user) throws SQLException;

    void update(Connection connection, User user) throws SQLException;

    List<User> findAllAvailableSpeakersByEmail(Connection connection, int eventId, String searchQuery) throws SQLException;

    void findAllParticipants(Connection connection, Event event) throws SQLException;

    void insertParticipant(Connection connection, Event event, User user) throws SQLException;

    void deleteParticipant(Connection connection, Event event, User user) throws SQLException;

}
