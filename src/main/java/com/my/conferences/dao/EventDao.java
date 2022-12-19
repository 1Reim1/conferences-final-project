package com.my.conferences.dao;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Dao interface for interaction with events in storage
 */
public interface EventDao {

    /**
     * returns list of events by parameters
     *
     * @param connection   db connection
     * @param order        sort order
     * @param reverseOrder boolean that represents reverse order or not
     * @param futureEvents boolean that represents future or past events
     * @param pageSize     number of events per page
     * @param page         page
     * @param language     language of events
     * @return list of events
     */
    List<Event> findAll(Connection connection, Event.Order order, boolean reverseOrder, boolean futureEvents, int pageSize, int page, String language) throws SQLException;

    /**
     * returns the list of events with which the user is associated
     *
     * @param connection   db connection
     * @param order        sort order
     * @param reverseOrder boolean that represents reverse order or not
     * @param futureEvents boolean that represents future or past events
     * @param pageSize     number of events per page
     * @param page         page
     * @param user         user
     * @return list of user’s events
     */
    List<Event> findAllMy(Connection connection, Event.Order order, boolean reverseOrder, boolean futureEvents, int pageSize, int page, User user) throws SQLException;

    /**
     * returns count of events
     *
     * @param futureOrder boolean that represents future or past events
     * @param language    language of events
     * @return count of events
     */
    int findCount(Connection connection, boolean futureOrder, String language) throws SQLException;

    /**
     * returns count of events with which the user is associated
     *
     * @param connection db connection
     * @param futureOrder boolean that represents future or past events
     * @param user        user
     * @return count of events
     */
    int findCountMy(Connection connection, boolean futureOrder, User user) throws SQLException;

    /**
     * returns all events moderated by a user with that id
     *
     * @param connection db connection
     * @param moderator moderator
     * @return list of events moderated by a user with that id
     */
    List<Event> findAllByModerator(Connection connection, User moderator) throws SQLException;

    /**
     * Returns event by id
     *
     * @param connection db connection
     * @param id         id of event
     * @param showHidden boolean that represents showing hidden events or not
     * @return event with this id
     */
    Event findOne(Connection connection, int id, boolean showHidden) throws SQLException;

    /**
     * saves event to database
     * calls event.setId(unique identifier)
     *
     * @param connection db connection
     * @param event that should be saved
     */
    void insert(Connection connection, Event event) throws SQLException;

    /**
     * updates event in database
     *
     * @param connection db connection
     * @param event event that should be updated
     */
    void update(Connection connection, Event event) throws SQLException;
}
