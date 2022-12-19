package com.my.conferences.dao.implementation.mysql;

import com.my.conferences.dao.EventDao;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Mysql implementation of EventDao interface
 */
public class MysqlEventDaoImpl implements EventDao {

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

    private static final String GET_ALL_MODERATED_BY = "SELECT * FROM events WHERE moderator_id = ? ORDER BY date DESC";
    private static final String GET_ONE = "SELECT * FROM events WHERE id = ? AND hidden = false";
    private static final String GET_ONE_SHOW_HIDDEN = "SELECT * FROM events WHERE id = ?";
    private static final String INSERT_ONE = "INSERT INTO events VALUES (DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_ONE = "UPDATE events SET title = ?, description = ?, place = ?, date = ?, moderator_id = ?, hidden = ?, statistics = ?, language = ? WHERE id = ?";

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
    @Override
    public List<Event> findAll(Connection connection, Event.Order order, boolean reverseOrder, boolean futureEvents, int pageSize, int page, String language) throws SQLException {
        String query = getFindAllQuery(order);
        query = String.format(query, futureEvents ? ">" : "<", reverseOrder ? "DESC" : "");

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int k = 0;
            stmt.setTimestamp(++k, new Timestamp(System.currentTimeMillis()));
            return getAll(stmt, k, page, pageSize, language);
        }
    }

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
    @Override
    public List<Event> findAllMy(Connection connection, Event.Order order, boolean reverseOrder, boolean futureEvents, int pageSize, int page, User user) throws SQLException {
        String query = getFindAllMyQuery(order, user);
        query = String.format(query, futureEvents ? ">" : "<", reverseOrder ? "DESC" : "");

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int k = prepareStatementForMyEvents(stmt, user);
            return getAll(stmt, k, page, pageSize, user.getLanguage());
        }
    }


    private List<Event> getAll(PreparedStatement stmt, int k, int page, int pageSize, String language) throws SQLException {
        List<Event> events = new ArrayList<>();
        stmt.setString(++k, language);
        stmt.setInt(++k, pageSize);
        stmt.setInt(++k, (page - 1) * pageSize);
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                events.add(extractEvent(rs));
            }
        }

        return events;
    }

    /**
     * returns count of events
     *
     * @param futureOrder boolean that represents future or past events
     * @param language    language of events
     * @return count of events
     */
    @Override
    public int findCount(Connection connection, boolean futureOrder, String language) throws SQLException {
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

    /**
     * returns count of events with which the user is associated
     *
     * @param connection db connection
     * @param futureOrder boolean that represents future or past events
     * @param user        user
     * @return count of events
     */
    @Override
    public int findCountMy(Connection connection, boolean futureOrder, User user) throws SQLException {
        String query = getFindCountMyQuery(user);
        query = String.format(query, futureOrder ? ">" : "<");

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int k = prepareStatementForMyEvents(stmt, user);

            stmt.setString(++k, user.getLanguage());
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt("total");
            }
        }
    }

    /**
     * returns all events moderated by a user with that id
     *
     * @param connection db connection
     * @param moderator moderator
     * @return list of events moderated by a user with that id
     */
    @Override
    public List<Event> findAllByModerator(Connection connection, User moderator) throws SQLException {
        List<Event> events = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(GET_ALL_MODERATED_BY)) {
            stmt.setInt(1, moderator.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    events.add(extractEvent(rs));
                }
            }
        }

        return events;
    }

    /**
     * Returns event by id
     *
     * @param connection db connection
     * @param id         id of event
     * @param showHidden boolean that represents showing hidden events or not
     * @return event with this id
     */
    @Override
    public Event findOne(Connection connection, int id, boolean showHidden) throws SQLException {
        String query = showHidden ? GET_ONE_SHOW_HIDDEN : GET_ONE;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return extractEvent(rs);
            }
        }
    }

    /**
     * saves event to database
     * calls event.setId(unique identifier)
     *
     * @param connection db connection
     * @param event that should be saved
     */
    @Override
    public void insert(Connection connection, Event event) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_ONE, Statement.RETURN_GENERATED_KEYS)) {
            prepareStatementForEvent(stmt, event);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                event.setId(rs.getInt(1));
            }
        }
    }

    /**
     * updates event in database
     *
     * @param connection db connection
     * @param event event that should be updated
     */
    @Override
    public void update(Connection connection, Event event) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_ONE)) {
            int k = prepareStatementForEvent(stmt, event);
            stmt.setInt(++k, event.getId());
            stmt.executeUpdate();
        }
    }

    private String getFindAllQuery(Event.Order order) {
        if (order == Event.Order.DATE) {
            return GET_ALL_ORDER_BY_DATE;
        }
        if (order == Event.Order.REPORTS) {
            return GET_ALL_ORDER_BY_REPORTS;
        }

        return GET_ALL_ORDER_BY_PARTICIPANTS;
    }

    private String getFindAllMyQuery(Event.Order order, User user) {
        if (user.getRole() == User.Role.USER) {
            if (order == Event.Order.DATE) {
                return GET_ALL_MY_FOR_USER_ORDER_BY_DATE;
            }
            if (order == Event.Order.REPORTS) {
                return GET_ALL_MY_FOR_USER_ORDER_BY_REPORTS;
            }

            return GET_ALL_MY_FOR_USER_ORDER_BY_PARTICIPANTS;
        }

        if (user.getRole() == User.Role.SPEAKER) {
            if (order == Event.Order.DATE) {
                return GET_ALL_MY_FOR_SPEAKER_ORDER_BY_DATE;
            }
            if (order == Event.Order.REPORTS) {
                return GET_ALL_MY_FOR_SPEAKER_ORDER_BY_REPORTS;
            }

            return GET_ALL_MY_FOR_SPEAKER_ORDER_BY_PARTICIPANTS;
        }

        if (order == Event.Order.DATE) {
            return GET_ALL_MY_FOR_MODERATOR_ORDER_BY_DATE;
        }
        if (order == Event.Order.REPORTS) {
            return GET_ALL_MY_FOR_MODERATOR_ORDER_BY_REPORTS;
        }

        return GET_ALL_MY_FOR_MODERATOR_ORDER_BY_PARTICIPANTS;
    }

    private String getFindCountMyQuery(User user) {
        if (user.getRole() == User.Role.USER) {
            return GET_ALL_MY_COUNT_FOR_USER;
        }
        if (user.getRole() == User.Role.SPEAKER) {
            return GET_ALL_MY_COUNT_FOR_SPEAKER;
        }

        return GET_ALL_MY_COUNT_FOR_MODERATOR;
    }

    private int prepareStatementForEvent(PreparedStatement stmt, Event event) throws SQLException {
        int k = 0;
        stmt.setString(++k, event.getTitle());
        stmt.setString(++k, event.getDescription());
        stmt.setString(++k, event.getPlace());
        stmt.setTimestamp(++k, new java.sql.Timestamp(event.getDate().getTime()));
        stmt.setInt(++k, event.getModerator().getId());
        stmt.setBoolean(++k, event.isHidden());
        stmt.setInt(++k, event.getStatistics());
        stmt.setString(++k, event.getLanguage());
        return k;
    }

    private int prepareStatementForMyEvents(PreparedStatement stmt, User user) throws SQLException {
        int k = 0;
        stmt.setTimestamp(++k, new Timestamp(System.currentTimeMillis()));
        stmt.setInt(++k, user.getId());

        if (user.getRole() != User.Role.USER) {
            stmt.setInt(++k, user.getId());
        }

        return k;
    }

    private Event extractEvent(ResultSet rs) throws SQLException {
        Event event = new Event();
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

        return event;
    }
}
