package com.my.conferences.db;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventRepository {

    private static EventRepository instance;
    private static final String SELECT_EVENTS = "SELECT events.* FROM events ";
    private static final String SELECT_EVENTS_REPORTS_COUNT = "SELECT events.*, COUNT(r.id) AS reports_count FROM events ";
    private static final String SELECT_EVENTS_PARTICIPANTS_COUNT = "SELECT events.*, COUNT(p.user_id) AS participants_count FROM events ";
    private static final String LEFT_JOIN_REPORTS = "LEFT JOIN reports r ON events.id = r.event_id ";
    private static final String LEFT_JOIN_PARTICIPANTS = "LEFT JOIN participants p ON events.id = p.event_id ";
    private static final String WHERE_HIDDEN_AND_DATE = "WHERE events.hidden = false AND events.date %s ? ";
    private static final String WHERE_DATE = "WHERE events.date %s ? ";
    private static final String AND_REPORT_CONFIRMED = "AND r.confirmed = true ";
    private static final String GROUP_BY_EVENTS_ID = "GROUP BY events.id ";
    private static final String ORDER_BY_DATE = "ORDER BY events.date %s, events.id ";
    private static final String ORDER_BY_REPORTS_COUNT = "ORDER BY reports_count %s, events.id ";
    private static final String ORDER_BY_PARTICIPANTS_COUNT = "ORDER BY participants_count %s, events.id ";
    private static final String LIMIT_OFFSET = "LIMIT ? OFFSET ?";
    private static final String SELECT_COUNT = "SELECT COUNT(events.id) AS total FROM events ";
    private static final String SELECT_COUNT_DISTINCT = "SELECT COUNT(DISTINCT events.id) AS total FROM events ";
    private static final String GET_ONE = "SELECT * FROM events WHERE id = ? AND hidden = false";
    private static final String GET_ONE_SHOW_HIDDEN = "SELECT * FROM events WHERE id = ?";
    private static final String INSERT_ONE = "INSERT INTO events VALUES (DEFAULT, ?, ?, ?, ?, ?, ?)";
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
        StringBuilder queryBuilder = new StringBuilder();
        if (order == Event.Order.DATE) {
            queryBuilder.append(SELECT_EVENTS)
                    .append(WHERE_HIDDEN_AND_DATE)
                    .append(ORDER_BY_DATE)
                    .append(LIMIT_OFFSET);
        } else if (order == Event.Order.REPORTS) {
            queryBuilder.append(SELECT_EVENTS_REPORTS_COUNT)
                    .append(LEFT_JOIN_REPORTS)
                    .append(AND_REPORT_CONFIRMED)
                    .append(WHERE_HIDDEN_AND_DATE)
                    .append(GROUP_BY_EVENTS_ID)
                    .append(ORDER_BY_REPORTS_COUNT)
                    .append(LIMIT_OFFSET);
        } else {
            queryBuilder.append(SELECT_EVENTS_PARTICIPANTS_COUNT)
                    .append(LEFT_JOIN_PARTICIPANTS)
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

    public List<Event> findAll(Connection connection, Event.Order order, boolean reverseOrder, boolean futureOrder, int pageSize, int page, User user) throws SQLException {
        List<Event> events = new ArrayList<>();
        String query;
        if (user.getRole() == User.Role.USER)
            query = buildFindAllSqlForUser(order);
        else if (user.getRole() == User.Role.SPEAKER)
            query = buildFindAllSqlForSpeaker(order);
        else
            query = buildFindAllSqlForModerator(order);

        if (order != Event.Order.DATE)
            reverseOrder = !reverseOrder;
        query = String.format(query, futureOrder ? ">" : "<", reverseOrder ? "DESC" : "");

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int k = 0;
            stmt.setTimestamp(++k, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(++k, user.getId());
            if (user.getRole() != User.Role.USER)
                stmt.setInt(++k, user.getId());
            stmt.setInt(++k, pageSize);
            stmt.setInt(++k, (page - 1) * pageSize);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(extractEvent(rs));
                }

                return events;
            }
        }
    }

    private String buildFindAllSqlForUser(Event.Order order) {
        StringBuilder queryBuilder = new StringBuilder();
        if (order == Event.Order.DATE)
            queryBuilder.append(SELECT_EVENTS);
        else if (order == Event.Order.REPORTS)
            queryBuilder.append(SELECT_EVENTS_REPORTS_COUNT);
        else
            queryBuilder.append(SELECT_EVENTS_PARTICIPANTS_COUNT);
        queryBuilder.append(LEFT_JOIN_PARTICIPANTS);

        if (order == Event.Order.REPORTS) {
            queryBuilder.append(LEFT_JOIN_REPORTS)
                    .append(AND_REPORT_CONFIRMED);
        }

        queryBuilder.append(WHERE_HIDDEN_AND_DATE)
                .append("AND ( ")
                .append("p.user_id = ? ")
                .append(") ")
                .append(GROUP_BY_EVENTS_ID);

        if (order == Event.Order.DATE)
            queryBuilder.append(ORDER_BY_DATE);
        else if (order == Event.Order.REPORTS)
            queryBuilder.append(ORDER_BY_REPORTS_COUNT);
        else
            queryBuilder.append(ORDER_BY_PARTICIPANTS_COUNT);
        queryBuilder.append(LIMIT_OFFSET);

        return queryBuilder.toString();
    }

    private String buildFindAllSqlForSpeaker(Event.Order order) {
        StringBuilder queryBuilder = new StringBuilder();
        if (order == Event.Order.DATE)
            queryBuilder.append(SELECT_EVENTS);
        else if (order == Event.Order.REPORTS)
            queryBuilder.append(SELECT_EVENTS_REPORTS_COUNT);
        else
            queryBuilder.append(SELECT_EVENTS_PARTICIPANTS_COUNT);

        queryBuilder.append(LEFT_JOIN_PARTICIPANTS)
                    .append(LEFT_JOIN_REPORTS)
                    .append(WHERE_DATE)
                    .append("AND ( ")
                    .append("p.user_id = ? ")
                    .append("OR r.speaker_id = ? ")
                    .append(") ")
                    .append(GROUP_BY_EVENTS_ID);

        if (order == Event.Order.DATE)
            queryBuilder.append(ORDER_BY_DATE);
        else if (order == Event.Order.REPORTS)
            queryBuilder.append(ORDER_BY_REPORTS_COUNT);
        else
            queryBuilder.append(ORDER_BY_PARTICIPANTS_COUNT);
        queryBuilder.append(LIMIT_OFFSET);

        return queryBuilder.toString();
    }

    private String buildFindAllSqlForModerator(Event.Order order) {
        StringBuilder queryBuilder = new StringBuilder();
        if (order == Event.Order.DATE)
            queryBuilder.append(SELECT_EVENTS);
        else if (order == Event.Order.REPORTS)
            queryBuilder.append(SELECT_EVENTS_REPORTS_COUNT);
        else
            queryBuilder.append(SELECT_EVENTS_PARTICIPANTS_COUNT);
        queryBuilder.append(LEFT_JOIN_PARTICIPANTS);
        if (order == Event.Order.REPORTS) {
            queryBuilder.append(LEFT_JOIN_REPORTS);
        }

        queryBuilder.append(WHERE_DATE)
                .append("AND ( ")
                .append("p.user_id = ? ")
                .append("OR events.moderator_id = ? ")
                .append(") ")
                .append(GROUP_BY_EVENTS_ID);

        if (order == Event.Order.DATE)
            queryBuilder.append(ORDER_BY_DATE);
        else if (order == Event.Order.REPORTS)
            queryBuilder.append(ORDER_BY_REPORTS_COUNT);
        else
            queryBuilder.append(ORDER_BY_PARTICIPANTS_COUNT);
        queryBuilder.append(LIMIT_OFFSET);

        return queryBuilder.toString();
    }

    public int getCount(Connection connection, boolean futureOrder) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append(SELECT_COUNT)
                .append(WHERE_HIDDEN_AND_DATE);

        String query;
        if (futureOrder)
            query = String.format(queryBuilder.toString(), ">");
        else
            query = String.format(queryBuilder.toString(), "<");

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt("total");
            }
        }
    }

    public int getCount(Connection connection, boolean futureOrder, User user) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(SELECT_COUNT_DISTINCT)
                .append(LEFT_JOIN_PARTICIPANTS);

        if (user.getRole() == User.Role.SPEAKER)
            queryBuilder.append(LEFT_JOIN_REPORTS);

        if (user.getRole() == User.Role.USER)
            queryBuilder.append(WHERE_HIDDEN_AND_DATE);
        else
            queryBuilder.append(WHERE_DATE);

        queryBuilder.append("AND ( ")
                .append("p.user_id = ? ");

        if (user.getRole() == User.Role.SPEAKER)
            queryBuilder.append("OR r.speaker_id = ? ");
        else if (user.getRole() == User.Role.MODERATOR)
            queryBuilder.append("OR events.moderator_id = ? ");

        queryBuilder.append(") ");

        String query;
        if (futureOrder)
            query = String.format(queryBuilder.toString(), ">");
        else
            query = String.format(queryBuilder.toString(), "<");

        System.out.println(query);

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int k = 0;
            stmt.setTimestamp(++k, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(++k, user.getId());
            if (user.getRole() != User.Role.USER)
                stmt.setInt(++k, user.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                int res = rs.getInt("total");
                System.out.println(res);
                return res;
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

    public void insert(Connection connection, Event event) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_ONE, Statement.RETURN_GENERATED_KEYS)) {
            int k = 0;
            stmt.setString(++k, event.getTitle());
            stmt.setString(++k, event.getDescription());
            stmt.setString(++k, event.getPlace());
            stmt.setTimestamp(++k, new java.sql.Timestamp(event.getDate().getTime()));
            stmt.setInt(++k, event.getModerator().getId());
            stmt.setBoolean(++k, event.isHidden());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                event.setId(rs.getInt(1));
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
