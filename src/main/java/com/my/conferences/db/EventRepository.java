package com.my.conferences.db;

import com.my.conferences.entity.Event;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventRepository {

    private static EventRepository instance;
    private static final String GET_ALL_EVENTS_BY_DATE = "SELECT * FROM events WHERE hidden = false ORDER BY `date`, `id` LIMIT ? OFFSET ?";
    private static final String GET_ALL_EVENTS_BY_DATE_REVERSE = "SELECT * FROM events WHERE hidden = false ORDER BY `date` DESC, `id` LIMIT ? OFFSET ?";
    private static final String GET_ALL_EVENTS_BY_REPORTS = "SELECT events.*, COUNT(reports.id) AS reports_count" +
            " FROM events LEFT JOIN reports " +
            " ON events.id = reports.event_id" +
            " WHERE events.hidden = false" +
            " GROUP BY events.id" +
            " ORDER BY reports_count DESC, events.id LIMIT ? OFFSET ?";
    private static final String GET_ALL_EVENTS_BY_REPORTS_REVERSE = "SELECT events.*, COUNT(reports.id) AS reports_count" +
            " FROM events LEFT JOIN reports " +
            " ON events.id = reports.event_id" +
            " WHERE events.hidden = false" +
            " GROUP BY events.id" +
            " ORDER BY reports_count, events.id LIMIT ? OFFSET ?";
    private static final String GET_ALL_EVENTS_BY_PARTICIPANTS = "SELECT events.*, COUNT(participants.user_id) AS participants_count" +
            " FROM events LEFT JOIN participants " +
            " ON events.id = participants.event_id" +
            " WHERE events.hidden = false" +
            " GROUP BY events.id" +
            " ORDER BY participants_count DESC, events.id LIMIT ? OFFSET ?";
    private static final String GET_ALL_EVENTS_BY_PARTICIPANTS_REVERSE = "SELECT events.*, COUNT(participants.user_id) AS participants_count" +
            " FROM events LEFT JOIN participants " +
            " ON events.id = participants.event_id" +
            " WHERE events.hidden = false" +
            " GROUP BY events.id" +
            " ORDER BY participants_count, events.id LIMIT ? OFFSET ?";
    private static final String GET_ALL_EVENTS_COUNT = "SELECT COUNT(*) AS total FROM events WHERE events.hidden = false";
    private static final String GET_ONE_EVENT = "SELECT * FROM events WHERE id = ? AND hidden = false";
    private static final String GET_ONE_EVENT_SHOW_HIDDEN = "SELECT * FROM events WHERE id = ?";

    public static synchronized EventRepository getInstance() {
        if (instance == null) {
            instance = new EventRepository();
        }

        return instance;
    }

    private EventRepository() {

    }

    public List<Event> findAll(Connection connection, Event.Order order, boolean reverseOrder, int pageSize, int page) throws SQLException {
        List<Event> events = new ArrayList<>();
        String query;
        if (reverseOrder) {
            query = GET_ALL_EVENTS_BY_DATE_REVERSE;
            if (order == Event.Order.REPORTS)
                query = GET_ALL_EVENTS_BY_REPORTS_REVERSE;
            else if (order == Event.Order.PARTICIPANTS)
                query = GET_ALL_EVENTS_BY_PARTICIPANTS_REVERSE;
        } else {
            query = GET_ALL_EVENTS_BY_DATE;
            if (order == Event.Order.REPORTS)
                query = GET_ALL_EVENTS_BY_REPORTS;
            else if (order == Event.Order.PARTICIPANTS)
                query = GET_ALL_EVENTS_BY_PARTICIPANTS;
        }

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, pageSize);
            stmt.setInt(2, (page - 1) * pageSize);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(extractEvent(rs));
                }

                return events;
            }
        }
    }

    public int getCount(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(GET_ALL_EVENTS_COUNT)) {
            rs.next();
            return rs.getInt("total");
        }
    }

    public Event findOne(Connection connection, int id, boolean showHidden) throws SQLException {
        String query = GET_ONE_EVENT;
        if (showHidden)
            query = GET_ONE_EVENT_SHOW_HIDDEN;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return extractEvent(rs);
            }
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
