package com.my.conferences.dao;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface EventDao {
    List<Event> findAll(Connection connection, Event.Order order, boolean reverseOrder, boolean futureEvents, int pageSize, int page, String language) throws SQLException;

    List<Event> findAllMy(Connection connection, Event.Order order, boolean reverseOrder, boolean futureEvents, int pageSize, int page, User user) throws SQLException;

    int findCount(Connection connection, boolean futureOrder, String language) throws SQLException;

    int findCountMy(Connection connection, boolean futureOrder, User user) throws SQLException;

    Event findOne(Connection connection, int id, boolean showHidden) throws SQLException;

    void insert(Connection connection, Event event) throws SQLException;

    void update(Connection connection, Event event) throws SQLException;
}
