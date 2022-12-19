package com.my.conferences.dao.implementation.mysql;

import com.my.conferences.dao.ReportDao;
import com.my.conferences.dao.implementation.JdbcTemplate;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;

import java.sql.*;
import java.util.List;

/**
 * Mysql implementation of ReportDao interface
 */
public class MysqlReportDaoImpl implements ReportDao {

    private static final String GET_ALL = "SELECT * FROM reports WHERE event_id = ?";
    private static final String GET_ALL_ONLY_CONFIRMED = "SELECT * FROM reports WHERE event_id = ? AND confirmed = true";
    private static final String GET_ALL_BY_SPEAKER = "SELECT reports.* FROM reports JOIN events e on e.id = reports.event_id AND e.date %s ? WHERE reports.speaker_id = ?";
    private static final String GET_ONE = "SELECT * FROM reports WHERE id = ?";
    private static final String DELETE_ONE = "DELETE FROM reports WHERE id = ?";
    private static final String INSERT_ONE = "INSERT INTO reports VALUES (DEFAULT, ?, ?, ?, ?, ?)";
    private static final String UPDATE_ONE = "UPDATE reports SET topic = ?, event_id = ?, creator_id = ?, speaker_id = ?, confirmed = ? WHERE id = ?";
    private static final String GET_NEW_FOR_MODERATOR = "SELECT reports.* FROM reports JOIN events e ON reports.event_id = e.id WHERE confirmed = false AND e.moderator_id = ? AND creator_id = speaker_id";
    private static final String GET_NEW_FOR_SPEAKER = "SELECT reports.* FROM reports WHERE confirmed = false AND speaker_id = ? AND creator_id != speaker_id";

    /**
     * returns reports of event
     *
     * @param connection    db connection
     * @param event         event
     * @param onlyConfirmed boolean represents all events or only confirmed
     */
    @Override
    public void findAll(Connection connection, Event event, boolean onlyConfirmed) throws SQLException {
        String sql = onlyConfirmed ? GET_ALL_ONLY_CONFIRMED : GET_ALL;
        List<Report> reports = JdbcTemplate.query(connection, sql, this::extractReport, event.getId());
        event.setReports(reports);
    }

    /**
     * returns report with that id
     *
     * @param connection db connection
     * @param id         if of report
     * @return report
     */
    @Override
    public Report findOne(Connection connection, int id) throws SQLException {
        return JdbcTemplate
                .query(connection, GET_ONE, this::extractReport, id)
                .stream().findAny().orElseThrow(SQLException::new);
    }

    /**
     * returns list of unconfirmed reports for moderator
     *
     * @param connection db connection
     * @param user       moderator
     * @return list of reports
     */
    @Override
    public List<Report> findNewForModerator(Connection connection, User user) throws SQLException {
        return JdbcTemplate.query(connection, GET_NEW_FOR_MODERATOR, this::extractReport, user.getId());
    }

    /**
     * returns list of unconfirmed reports for speaker
     *
     * @param connection db connection
     * @param user       speaker
     * @return list of reports
     */
    @Override
    public List<Report> findNewForSpeaker(Connection connection, User user) throws SQLException {
        return JdbcTemplate.query(connection, GET_NEW_FOR_SPEAKER, this::extractReport, user.getId());
    }

    /**
     * returns all reports by speaker
     *
     * @param connection    db connection
     * @param speaker       speaker
     * @param futureReports future or past reports
     * @return list of reports by speaker
     */
    @Override
    public List<Report> findAllBySpeaker(Connection connection, User speaker, boolean futureReports) throws SQLException {
        String sql = String.format(GET_ALL_BY_SPEAKER, futureReports ? ">" : "<");
        return JdbcTemplate.query(connection, sql, this::extractReport, new Timestamp(System.currentTimeMillis()), speaker.getId());
    }

    /**
     * saves report to database
     *
     * @param connection db connection
     * @param report     report that should be saved
     */
    @Override
    public void insert(Connection connection, Report report) throws SQLException {
        PreparedStatement statement = JdbcTemplate.update(
                connection,
                INSERT_ONE,
                report.getTopic(),
                report.getEventId(),
                report.getCreator().getId(),
                report.getSpeaker().getId(),
                report.isConfirmed());
        try (ResultSet rs = statement.getGeneratedKeys()) {
            rs.next();
            report.setId(rs.getInt(1));
        }
        statement.close();
    }

    /**
     * updates report in database
     *
     * @param connection db connection
     * @param report     report that should be updated
     */
    @Override
    public void update(Connection connection, Report report) throws SQLException {
        JdbcTemplate.update(
                connection,
                UPDATE_ONE,
                report.getTopic(),
                report.getEventId(),
                report.getCreator().getId(),
                report.getSpeaker().getId(),
                report.isConfirmed(),
                report.getId()).close();
    }

    /**
     * deletes report from database
     *
     * @param connection db connection
     * @param report     report that should be deleted
     */
    @Override
    public void delete(Connection connection, Report report) throws SQLException {
        JdbcTemplate.update(connection, DELETE_ONE, report.getId()).close();
    }

    private Report extractReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        report.setId(rs.getInt("id"));
        report.setTopic(rs.getString("topic"));
        report.setEventId(rs.getInt("event_id"));
        User speaker = new User();
        speaker.setId(rs.getInt("speaker_id"));
        report.setSpeaker(speaker);
        User creator = new User();
        creator.setId(rs.getInt("creator_id"));
        report.setCreator(creator);
        report.setConfirmed(rs.getBoolean("confirmed"));

        return report;
    }
}
