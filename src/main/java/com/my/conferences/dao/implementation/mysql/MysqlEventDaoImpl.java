package com.my.conferences.dao.implementation.mysql;

import com.my.conferences.dao.EventDao;
import com.my.conferences.dao.implementation.JdbcTemplate;
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
        String sql = getFindAllSql(order);
        sql = String.format(sql, futureEvents ? ">" : "<", reverseOrder ? "DESC" : "");

        return JdbcTemplate.query(
                connection,
                sql,
                this::extractEvent,
                new Timestamp(System.currentTimeMillis()),
                language,
                pageSize,
                (page - 1) * pageSize);
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
        String sql = getFindAllMySql(order, user);
        sql = String.format(sql, futureEvents ? ">" : "<", reverseOrder ? "DESC" : "");
        Object[] parameters;
        if (user.getRole() == User.Role.USER) {
            parameters = new Object[]{
                    new Timestamp(System.currentTimeMillis()),
                    user.getId(),
                    user.getLanguage(),
                    pageSize,
                    (page - 1) * pageSize};
        } else {
            parameters = new Object[]{
                    new Timestamp(System.currentTimeMillis()),
                    user.getId(),
                    user.getId(),
                    user.getLanguage(),
                    pageSize,
                    (page - 1) * pageSize
            };
        }

        return JdbcTemplate.query(connection, sql, this::extractEvent, parameters);
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
        String sql = String.format(GET_ALL_COUNT, futureOrder ? ">" : "<");

        return JdbcTemplate
                .query(connection, sql, (rs) -> rs.getInt("total"), new Timestamp(System.currentTimeMillis()), language)
                .stream().findAny().orElseThrow(SQLException::new);
    }

    /**
     * returns count of events with which the user is associated
     *
     * @param connection  db connection
     * @param futureOrder boolean that represents future or past events
     * @param user        user
     * @return count of events
     */
    @Override
    public int findCountMy(Connection connection, boolean futureOrder, User user) throws SQLException {
        String sql = getFindCountMySql(user);
        sql = String.format(sql, futureOrder ? ">" : "<");
        Object[] parameters;
        if (user.getRole() == User.Role.USER) {
            parameters = new Object[]{
                    new Timestamp(System.currentTimeMillis()),
                    user.getId(),
                    user.getLanguage()
            };
        } else {
            parameters = new Object[]{
                    new Timestamp(System.currentTimeMillis()),
                    user.getId(),
                    user.getId(),
                    user.getLanguage()
            };
        }

        return JdbcTemplate
                .query(connection, sql, (rs) -> rs.getInt("total"), parameters)
                .stream().findAny().orElseThrow(SQLException::new);
    }

    /**
     * returns all events moderated by a user with that id
     *
     * @param connection db connection
     * @param moderator  moderator
     * @return list of events moderated by a user with that id
     */
    @Override
    public List<Event> findAllByModerator(Connection connection, User moderator) throws SQLException {
        return JdbcTemplate.query(connection, GET_ALL_MODERATED_BY, this::extractEvent, moderator.getId());
    }

    /**
     * returns event by id
     *
     * @param connection db connection
     * @param id         id of event
     * @param showHidden boolean that represents showing hidden events or not
     * @return event with that id
     */
    @Override
    public Event findOne(Connection connection, int id, boolean showHidden) throws SQLException {
        String sql = showHidden ? GET_ONE_SHOW_HIDDEN : GET_ONE;

        return JdbcTemplate
                .query(connection, sql, this::extractEvent, id)
                .stream().findAny().orElseThrow(SQLException::new);
    }

    /**
     * saves event to database
     * calls event.setId(unique identifier)
     *
     * @param connection db connection
     * @param event      that should be saved
     */
    @Override
    public void insert(Connection connection, Event event) throws SQLException {
        PreparedStatement statement = JdbcTemplate.update(
                connection,
                INSERT_ONE,
                event.getTitle(),
                event.getDescription(),
                event.getPlace(),
                new Timestamp(event.getDate().getTime()),
                event.getModerator().getId(),
                event.isHidden(),
                event.getStatistics(),
                event.getLanguage());
        try (ResultSet rs = statement.getGeneratedKeys()) {
            rs.next();
            event.setId(rs.getInt(1));
        }
        statement.close();
    }

    /**
     * updates event in database
     *
     * @param connection db connection
     * @param event      event that should be updated
     */
    @Override
    public void update(Connection connection, Event event) throws SQLException {
        JdbcTemplate.update(
                connection,
                UPDATE_ONE,
                event.getTitle(),
                event.getDescription(),
                event.getPlace(),
                new Timestamp(event.getDate().getTime()),
                event.getModerator().getId(),
                event.isHidden(),
                event.getStatistics(),
                event.getLanguage(),
                event.getId()).close();
    }

    private String getFindAllSql(Event.Order order) {
        if (order == Event.Order.DATE) {
            return GET_ALL_ORDER_BY_DATE;
        }
        if (order == Event.Order.REPORTS) {
            return GET_ALL_ORDER_BY_REPORTS;
        }

        return GET_ALL_ORDER_BY_PARTICIPANTS;
    }

    private String getFindAllMySql(Event.Order order, User user) {
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

    private String getFindCountMySql(User user) {
        if (user.getRole() == User.Role.USER) {
            return GET_ALL_MY_COUNT_FOR_USER;
        }
        if (user.getRole() == User.Role.SPEAKER) {
            return GET_ALL_MY_COUNT_FOR_SPEAKER;
        }

        return GET_ALL_MY_COUNT_FOR_MODERATOR;
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
