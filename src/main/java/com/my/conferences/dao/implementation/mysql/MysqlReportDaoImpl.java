package com.my.conferences.dao.implementation.mysql;

import com.my.conferences.dao.ReportDao;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;

import java.sql.*;
import java.util.ArrayList;
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
        List<Report> reports = new ArrayList<>();
        String query = onlyConfirmed ? GET_ALL_ONLY_CONFIRMED : GET_ALL;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, event.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(extractReport(rs));
                }
            }
        }

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
        try (PreparedStatement stmt = connection.prepareStatement(GET_ONE)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return extractReport(rs);
            }
        }
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
        return findNew(connection, user, GET_NEW_FOR_MODERATOR);
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
        return findNew(connection, user, GET_NEW_FOR_SPEAKER);
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
        List<Report> reports = new ArrayList<>();
        String query = String.format(GET_ALL_BY_SPEAKER, futureReports ? ">" : "<");
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            int k = 0;
            stmt.setTimestamp(++k, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(++k, speaker.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(extractReport(rs));
                }
            }
        }

        return reports;
    }

    private List<Report> findNew(Connection connection, User user, String query) throws SQLException {
        List<Report> reports = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, user.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(extractReport(rs));
                }
            }
        }

        return reports;
    }

    /**
     * saves report to database
     *
     * @param connection db connection
     * @param report     report that should be saved
     */
    @Override
    public void insert(Connection connection, Report report) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_ONE, Statement.RETURN_GENERATED_KEYS)) {
            prepareStatementForReport(stmt, report);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                report.setId(rs.getInt(1));
            }
        }
    }

    /**
     * updates report in database
     *
     * @param connection db connection
     * @param report     report that should be updated
     */
    @Override
    public void update(Connection connection, Report report) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_ONE)) {
            int k = prepareStatementForReport(stmt, report);
            stmt.setInt(++k, report.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * deletes report from database
     *
     * @param connection db connection
     * @param report     report that should be deleted
     */
    @Override
    public void delete(Connection connection, Report report) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_ONE)) {
            stmt.setInt(1, report.getId());
            stmt.executeUpdate();
        }
    }

    private int prepareStatementForReport(PreparedStatement stmt, Report report) throws SQLException {
        int k = 0;
        stmt.setString(++k, report.getTopic());
        stmt.setInt(++k, report.getEventId());
        stmt.setInt(++k, report.getCreator().getId());
        stmt.setInt(++k, report.getSpeaker().getId());
        stmt.setBoolean(++k, report.isConfirmed());
        return k;
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
