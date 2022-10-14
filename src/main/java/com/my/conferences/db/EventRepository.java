package com.my.conferences.db;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventRepository {

    private static EventRepository instance;
    private static final String GET_ALL_COUNT = "SELECT COUNT(events.id) AS total FROM events WHERE events.hidden = false AND events.date %s ? AND events.language = ?";
    private static final String GET_ALL_ORDER_BY_DATE = "SELECT events.* FROM events WHERE events.hidden = false AND events.date %s ? AND events.language = ? ORDER BY events.date %s, events.id LIMIT ? OFFSET ?";
    private static final String GET_ALL_ORDER_BY_REPORTS = "SELECT events.*, COUNT(r.id) AS reports_count FROM events LEFT JOIN reports r ON events.id = r.event_id AND r.confirmed = true WHERE events.hidden = false AND events.date %s ? AND events.language = ? GROUP BY events.id ORDER BY reports_count %s, events.id LIMIT ? OFFSET ?";
    private static final String GET_ALL_ORDER_BY_PARTICIPANTS = "SELECT events.*, COUNT(p.user_id) AS participants_count FROM events LEFT JOIN participants p ON events.id = p.event_id WHERE events.hidden = false AND events.date %s ? AND events.language = ? GROUP BY events.id ORDER BY participants_count %s, events.id LIMIT ? OFFSET ?";

    private static final String GET_ALL_MY_COUNT_FOR_MODERATOR = "SELECT COUNT(DISTINCT events.id) AS total FROM events LEFT JOIN participants p ON events.id = p.event_id WHERE events.date %s ? AND ( p.user_id = ? OR events.moderator_id = ? ) AND events.language = ?";
    private static final String GET_ALL_MY_FOR_MODERATOR_ORDER_BY_DATE = "SELECT events.* FROM events LEFT JOIN participants p ON events.id = p.event_id WHERE events.date %s ? AND ( p.user_id = ? OR events.moderator_id = ? ) AND events.language = ? GROUP BY events.id ORDER BY events.date %s, events.id LIMIT ? OFFSET ?";
    private static final String GET_ALL_MY_FOR_MODERATOR_ORDER_BY_REPORTS = "SELECT events.*, COUNT(r.id) AS reports_count FROM events LEFT JOIN participants p ON events.id = p.event_id LEFT JOIN reports r ON events.id = r.event_id WHERE events.date %s ? AND ( p.user_id = ? OR events.moderator_id = ? ) AND events.language = ? GROUP BY events.id ORDER BY reports_count %s, events.id LIMIT ? OFFSET ?";
    private static final String GET_ALL_MY_FOR_MODERATOR_ORDER_BY_PARTICIPANTS = "SELECT events.*, COUNT(p.user_id) AS participants_count FROM events LEFT JOIN participants p ON events.id = p.event_id WHERE events.date %s ? AND ( p.user_id = ? OR events.moderator_id = ? ) AND events.language = ? GROUP BY events.id ORDER BY participants_count %s, events.id LIMIT ? OFFSET ?";

    private static final String GET_ALL_MY_COUNT_FOR_SPEAKER = "SELECT COUNT(DISTINCT events.id) AS total FROM events LEFT JOIN participants p ON events.id = p.event_id LEFT JOIN reports r ON events.id = r.event_id WHERE events.date %s ? AND ( p.user_id = ? OR r.speaker_id = ? ) AND events.language = ?";
    private static final String GET_ALL_MY_FOR_SPEAKER_ORDER_BY_DATE = "SELECT events.* FROM events LEFT JOIN participants p ON events.id = p.event_id LEFT JOIN reports r ON events.id = r.event_id WHERE events.date %s ? AND ( p.user_id = ? OR r.speaker_id = ? ) AND events.language = ? GROUP BY events.id ORDER BY events.date %s, events.id LIMIT ? OFFSET ?";
    private static final String GET_ALL_MY_FOR_SPEAKER_ORDER_BY_REPORTS = "SELECT events.*, COUNT(r.id) AS reports_count FROM events LEFT JOIN participants p ON events.id = p.event_id LEFT JOIN reports r ON events.id = r.event_id WHERE events.date %s ? AND ( p.user_id = ? OR r.speaker_id = ? ) AND events.language = ? GROUP BY events.id ORDER BY reports_count %s, events.id LIMIT ? OFFSET ?";
    private static final String GET_ALL_MY_FOR_SPEAKER_ORDER_BY_PARTICIPANTS = "SELECT events.*, COUNT(p.user_id) AS participants_count FROM events LEFT JOIN participants p ON events.id = p.event_id LEFT JOIN reports r ON events.id = r.event_id WHERE events.date %s ? AND ( p.user_id = ? OR r.speaker_id = ? ) AND events.language = ? GROUP BY events.id ORDER BY participants_count %s, events.id LIMIT ? OFFSET ?";

    private static final String GET_ALL_MY_COUNT_FOR_USER = "SELECT COUNT(events.id) AS total FROM events LEFT JOIN participants p ON events.id = p.event_id WHERE events.hidden = false AND events.date %s ? AND ( p.user_id = ? ) AND events.language = ?";
    private static final String GET_ALL_MY_FOR_USER_ORDER_BY_DATE = "SELECT events.* FROM events LEFT JOIN participants p ON events.id = p.event_id WHERE events.hidden = false AND events.date %s ? AND ( p.user_id = ? ) AND events.language = ? GROUP BY events.id ORDER BY events.date %s, events.id LIMIT ? OFFSET ?";
    private static final String GET_ALL_MY_FOR_USER_ORDER_BY_REPORTS = "SELECT events.*, COUNT(r.id) AS reports_count FROM events LEFT JOIN participants p ON events.id = p.event_id LEFT JOIN reports r ON events.id = r.event_id AND r.confirmed = true WHERE events.hidden = false AND events.date %s ? AND ( p.user_id = ? ) AND events.language = ? GROUP BY events.id ORDER BY reports_count %s, events.id LIMIT ? OFFSET ?";
    private static final String GET_ALL_MY_FOR_USER_ORDER_BY_PARTICIPANTS = "SELECT events.*, COUNT(p.user_id) AS participants_count FROM events LEFT JOIN participants p ON events.id = p.event_id WHERE events.hidden = false AND events.date %s ? AND ( p.user_id = ? ) AND events.language = ? GROUP BY events.id ORDER BY participants_count %s, events.id LIMIT ? OFFSET ?";

    private static final String GET_ONE = "SELECT * FROM events WHERE id = ? AND hidden = false";
    private static final String GET_ONE_SHOW_HIDDEN = "SELECT * FROM events WHERE id = ?";
    private static final String INSERT_ONE = "INSERT INTO events VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_ONE = "UPDATE events SET title = ?, description = ?, place = ?, date = ?, moderator_id = ?, hidden = ?, statistics = ?, language = ? WHERE id = ?";
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

    public List<Event> findAll(Connection connection, Event.Order order, boolean reverseOrder, boolean futureOrder, int pageSize, int page, String language) throws SQLException {
        List<Event> events = new ArrayList<>();
        String query;
        if (order == Event.Order.DATE)
            query = GET_ALL_ORDER_BY_DATE;
        else if (order == Event.Order.REPORTS)
            query = GET_ALL_ORDER_BY_REPORTS;
        else
            query = GET_ALL_ORDER_BY_PARTICIPANTS;

        if (order != Event.Order.DATE) {
            reverseOrder = !reverseOrder;
        }

        query = String.format(query, futureOrder ? ">" : "<", reverseOrder ? "DESC" : "");

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int k = 0;
            stmt.setTimestamp(++k, new Timestamp(System.currentTimeMillis()));
            stmt.setString(++k, language);
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

    public List<Event> findAllMy(Connection connection, Event.Order order, boolean reverseOrder, boolean futureOrder, int pageSize, int page, User user) throws SQLException {
        List<Event> events = new ArrayList<>();
        String query;
        if (user.getRole() == User.Role.USER) {
            if (order == Event.Order.DATE)
                query = GET_ALL_MY_FOR_USER_ORDER_BY_DATE;
            else if (order == Event.Order.REPORTS)
                query = GET_ALL_MY_FOR_USER_ORDER_BY_REPORTS;
            else
                query = GET_ALL_MY_FOR_USER_ORDER_BY_PARTICIPANTS;
        } else if (user.getRole() == User.Role.SPEAKER) {
            if (order == Event.Order.DATE)
                query = GET_ALL_MY_FOR_SPEAKER_ORDER_BY_DATE;
            else if (order == Event.Order.REPORTS)
                query = GET_ALL_MY_FOR_SPEAKER_ORDER_BY_REPORTS;
            else
                query = GET_ALL_MY_FOR_SPEAKER_ORDER_BY_PARTICIPANTS;
        } else {
            if (order == Event.Order.DATE)
                query = GET_ALL_MY_FOR_MODERATOR_ORDER_BY_DATE;
            else if (order == Event.Order.REPORTS)
                query = GET_ALL_MY_FOR_MODERATOR_ORDER_BY_REPORTS;
            else
                query = GET_ALL_MY_FOR_MODERATOR_ORDER_BY_PARTICIPANTS;
        }

        if (order != Event.Order.DATE) {
            reverseOrder = !reverseOrder;
        }
        query = String.format(query, futureOrder ? ">" : "<", reverseOrder ? "DESC" : "");

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int k = 0;
            stmt.setTimestamp(++k, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(++k, user.getId());
            if (user.getRole() != User.Role.USER) {
                stmt.setInt(++k, user.getId());
            }

            stmt.setString(++k, user.getLanguage());
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

    public int getCount(Connection connection, boolean futureOrder, String language) throws SQLException {
        String query = String.format(GET_ALL_COUNT, futureOrder ? ">" : "<");

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setString(2, language);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt("total");
            }
        }
    }

    public int getCountMy(Connection connection, boolean futureOrder, User user) throws SQLException {
        String query;
        if (user.getRole() == User.Role.USER)
            query = GET_ALL_MY_COUNT_FOR_USER;
        else if (user.getRole() == User.Role.SPEAKER)
            query = GET_ALL_MY_COUNT_FOR_SPEAKER;
        else
            query = GET_ALL_MY_COUNT_FOR_MODERATOR;

        query = String.format(query, futureOrder ? ">" : "<");

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int k = 0;
            stmt.setTimestamp(++k, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(++k, user.getId());
            if (user.getRole() != User.Role.USER) {
                stmt.setInt(++k, user.getId());
            }

            stmt.setString(++k, user.getLanguage());
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

    public void insert(Connection connection, Event event) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_ONE, Statement.RETURN_GENERATED_KEYS)) {
            int k = 0;
            stmt.setString(++k, event.getTitle());
            stmt.setString(++k, event.getDescription());
            stmt.setString(++k, event.getPlace());
            stmt.setTimestamp(++k, new java.sql.Timestamp(event.getDate().getTime()));
            stmt.setInt(++k, event.getModerator().getId());
            stmt.setBoolean(++k, event.isHidden());
            stmt.setInt(++k, event.getStatistics());
            stmt.setString(++k, event.getLanguage());
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
            stmt.setInt(++k, event.getStatistics());
            stmt.setString(++k, event.getLanguage());
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
        event.setStatistics(rs.getInt("statistics"));
        event.setLanguage(rs.getString("language"));
    }
}
