package com.my.conferences.db;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class EventRepository {

    private static EventRepository instance;
    private static final String GET_ALL_EVENTS = "SELECT * FROM events";

    public static synchronized EventRepository getInstance() {
        if (instance == null) {
            instance = new EventRepository();
        }

        return instance;
    }

    private EventRepository() {

    }

    public List<Event> findAll(Connection connection) throws SQLException {
        List<Event> events = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(GET_ALL_EVENTS);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                events.add(extractEvent(rs));
            }

            return events;
        }
    }

    private Event extractEvent(ResultSet rs) throws SQLException {
        Event event = new Event();
        extractEvent(rs, event);
        return event;
    }

    private void extractEvent(ResultSet rs, Event event) throws SQLException {
        event.setId(rs.getInt("id"));
        event.setTitle(rs.getString("title"));
        event.setDescription(rs.getString("description"));
        event.setDate(new Date(rs.getTimestamp("date").getTime()));
        event.setPlace(rs.getString("place"));
        event.setParticipants(new ArrayList<>());
        event.setReports(new ArrayList<>());
    }
}
