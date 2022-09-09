package com.my.conferences.db;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventRepository {

    private static EventRepository instance;
    private static final String GET_ALL_BY_DATE = "SELECT * FROM events WHERE hidden = false AND date %s ? ORDER BY `date`, `id` LIMIT ? OFFSET ?";
    private static final String GET_ALL_BY_DATE_REVERSE = "SELECT * FROM events WHERE hidden = false AND date %s ? ORDER BY `date` DESC, `id` LIMIT ? OFFSET ?";
    private static final String GET_ALL_BY_REPORTS = "SELECT events.*, COUNT(reports.id) AS reports_count" +
            " FROM events LEFT JOIN reports " +
            " ON events.id = reports.event_id" +
            " WHERE events.hidden = false AND date %s ?" +
            " GROUP BY events.id" +
            " ORDER BY reports_count DESC, events.id LIMIT ? OFFSET ?";
    private static final String GET_ALL_BY_REPORTS_REVERSE = "SELECT events.*, COUNT(reports.id) AS reports_count" +
            " FROM events LEFT JOIN reports " +
            " ON events.id = reports.event_id" +
            " WHERE events.hidden = false AND date %s ?" +
            " GROUP BY events.id" +
            " ORDER BY reports_count, events.id LIMIT ? OFFSET ?";
    private static final String GET_ALL_BY_PARTICIPANTS = "SELECT events.*, COUNT(participants.user_id) AS participants_count" +
            " FROM events LEFT JOIN participants " +
            " ON events.id = participants.event_id" +
            " WHERE events.hidden = false AND date %s ?" +
            " GROUP BY events.id" +
            " ORDER BY participants_count DESC, events.id LIMIT ? OFFSET ?";
    private static final String GET_ALL_BY_PARTICIPANTS_REVERSE = "SELECT events.*, COUNT(participants.user_id) AS participants_count" +
            " FROM events LEFT JOIN participants " +
            " ON events.id = participants.event_id" +
            " WHERE events.hidden = false AND date %s ?" +
            " GROUP BY events.id" +
            " ORDER BY participants_count, events.id LIMIT ? OFFSET ?";

    private static final String SELECT_EVENTS = "SELECT events.* FROM events ";
    private static final String SELECT_EVENTS_REPORTS_COUNT = "SELECT events.*, COUNT(reports.id) AS reports_count FROM events " +
            "LEFT JOIN reports ON events.id = reports.event_id ";
    private static final String SELECT_EVENTS_PARTICIPANTS_COUNT = "SELECT events.*, COUNT(participants.user_id) AS participants_count " +
            "FROM events LEFT JOIN participants " +
            "ON events.id = participants.event_id ";
    private static final String WHERE_HIDDEN_AND_DATE = "WHERE events.hidden = false AND date %s ? ";
    private static final String AND_REPORT_CONFIRMED = "AND reports.confirmed = true ";
    private static final String GROUP_BY_EVENTS_ID = "GROUP BY events.id ";
    private static final String ORDER_BY_DATE = "ORDER BY events.date %s, events.id ";
    private static final String ORDER_BY_REPORTS_COUNT = "ORDER BY reports_count %s, events.id ";
    private static final String ORDER_BY_PARTICIPANTS_COUNT = "ORDER BY participants_count %s, events.id ";
    private static final String LIMIT_OFFSET = "LIMIT ? OFFSET ?";

    private static final String GET_EVENTS_COUNT = "SELECT COUNT(*) AS total FROM events WHERE events.hidden = false AND date %s ?";
    private static final String GET_ONE = "SELECT * FROM events WHERE id = ? AND hidden = false";
    private static final String GET_ONE_SHOW_HIDDEN = "SELECT * FROM events WHERE id = ?";
    private static final String UPDATE_ONE = "UPDATE events SET title = ?, description = ?, place = ?, date = ?, moderator_id = ?, hidden = ? WHERE id = ?";
    private static final String INSERT_PARTICIPANT = "INSERT INTO participants VALUES (?, ?)";
    private static final String DELETE_PARTICIPANT = "DELETE FROM participants WHERE user_id = ? AND event_id = ?";

    public static synchronized EventRepository getInstance() {
        if (instance == null) {
            instance = new EventRepository();
        }

        return instance;
    }

    private EventRepository() {

    }

    public List<Event> findAll(Connection connection, Event.Order order, boolean reverseOrder, boolean futureOrder, int pageSize, int page) throws SQLException {
        List<Event> events = new ArrayList<>();
//        String query;
//        if (reverseOrder) {
//            query = GET_ALL_BY_DATE_REVERSE;
//            if (order == Event.Order.REPORTS)
//                query = GET_ALL_BY_REPORTS_REVERSE;
//            else if (order == Event.Order.PARTICIPANTS)
//                query = GET_ALL_BY_PARTICIPANTS_REVERSE;
//        } else {
//            query = GET_ALL_BY_DATE;
//            if (order == Event.Order.REPORTS)
//                query = GET_ALL_BY_REPORTS;
//            else if (order == Event.Order.PARTICIPANTS)
//                query = GET_ALL_BY_PARTICIPANTS;
//        }
//
//        if (futureOrder)
//            query = String.format(query, ">");
//        else
//            query = String.format(query, "<");

        StringBuilder queryBuilder = new StringBuilder();
        if (order == Event.Order.DATE) {
            queryBuilder.append(SELECT_EVENTS)
                    .append(WHERE_HIDDEN_AND_DATE)
                    .append(ORDER_BY_DATE)
                    .append(LIMIT_OFFSET);
        } else if (order == Event.Order.REPORTS) {
            queryBuilder.append(SELECT_EVENTS_REPORTS_COUNT)
                    .append(AND_REPORT_CONFIRMED)
                    .append(WHERE_HIDDEN_AND_DATE)
                    .append(GROUP_BY_EVENTS_ID)
                    .append(ORDER_BY_REPORTS_COUNT)
                    .append(LIMIT_OFFSET);
        } else {
            queryBuilder.append(SELECT_EVENTS_PARTICIPANTS_COUNT)
                    .append(WHERE_HIDDEN_AND_DATE)
                    .append(GROUP_BY_EVENTS_ID)
                    .append(ORDER_BY_PARTICIPANTS_COUNT)
                    .append(LIMIT_OFFSET);
        }

        if (order != Event.Order.DATE)
            reverseOrder = !reverseOrder;
        String query = String.format(queryBuilder.toString(), futureOrder ? ">" : "<", reverseOrder ? "DESC" : "");

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(2, pageSize);
            stmt.setInt(3, (page - 1) * pageSize);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(extractEvent(rs));
                }

                return events;
            }
        }
    }

    public int getCount(Connection connection, boolean futureOrder) throws SQLException {
        String query = GET_EVENTS_COUNT;
        if (futureOrder)
            query = String.format(query, ">");
        else
            query = String.format(query, "<");

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt("total");
            }
        }
    }

    public Event findOne(Connection connection, int id, boolean showHidden) throws SQLException {
        String query = GET_ONE;
        if (showHidden)
            query = GET_ONE_SHOW_HIDDEN;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return extractEvent(rs);
            }
        }
    }

    public void update(Connection connection, Event event) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_ONE)) {
            int k = 0;
            stmt.setString(++k, event.getTitle());
            stmt.setString(++k, event.getDescription());
            stmt.setString(++k, event.getPlace());
            stmt.setTimestamp(++k, new java.sql.Timestamp(event.getDate().getTime()));
            stmt.setInt(++k, event.getModerator().getId());
            stmt.setBoolean(++k, event.isHidden());
            stmt.setInt(++k, event.getId());
            stmt.executeUpdate();
        }
    }

    public void insertParticipant(Connection connection, Event event, User user) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_PARTICIPANT)) {
            stmt.setInt(1, user.getId());
            stmt.setInt(2, event.getId());
            stmt.executeUpdate();
        }
    }

    public void deleteParticipant(Connection connection, Event event, User user) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_PARTICIPANT)) {
            stmt.setInt(1, user.getId());
            stmt.setInt(2, event.getId());
            stmt.executeUpdate();
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
        User moderator = new User();
        moderator.setId(rs.getInt("moderator_id"));
        event.setModerator(moderator);
        event.setHidden(rs.getBoolean("hidden"));
    }
}
