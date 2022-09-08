package com.my.conferences.db;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportRepository {
    private static ReportRepository instance;
    private static final String GET_ALL = "SELECT * FROM reports WHERE event_id = ?";
    private static final String GET_ALL_ONLY_CONFIRMED = "SELECT * FROM reports WHERE event_id = ? AND confirmed = true";
    private static final String GET_ONE = "SELECT * FROM reports WHERE id = ?";
    private static final String DELETE_ONE = "DELETE FROM reports WHERE id = ?";
    private static final String CONFIRM_ONE = "UPDATE reports SET confirmed = true WHERE id = ?";
    private static final String INSERT_ONE = "INSERT INTO reports VALUES (DEFAULT, ?, ?, ?, ?, ?)";
    private static final String UPDATE_ONE = "UPDATE reports SET topic = ?, event_id = ?, creator_id = ?, speaker_id = ?, confirmed = ? WHERE id = ?";

    public static synchronized ReportRepository getInstance() {
        if (instance == null) {
            instance = new ReportRepository();
        }

        return instance;
    }

    private ReportRepository() {

    }

    public void findAll(Connection connection, Event event, boolean onlyConfirmed) throws SQLException {
        List<Report> reports = new ArrayList<>();
        String query = GET_ALL;
        if (onlyConfirmed)
            query = GET_ALL_ONLY_CONFIRMED;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, event.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(extractReport(rs));
                }

                event.setReports(reports);
            }
        }
    }

    public Report findOne(Connection connection, int id) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(GET_ONE)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return extractReport(rs);
            }
        }
    }

    public void delete(Connection connection, Report report) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(DELETE_ONE)) {
            stmt.setInt(1, report.getId());
            stmt.executeUpdate();
        }
    }

    public void update(Connection connection, Report report) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(UPDATE_ONE)) {
            int k = 0;
            stmt.setString(++k, report.getTopic());
            stmt.setInt(++k, report.getEventId());
            stmt.setInt(++k, report.getCreator().getId());
            stmt.setInt(++k, report.getSpeaker().getId());
            stmt.setBoolean(++k, report.isConfirmed());
            stmt.setInt(++k, report.getId());
            stmt.executeUpdate();
        }
    }

    public void insert(Connection connection, Report report) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(INSERT_ONE, Statement.RETURN_GENERATED_KEYS)) {
            int k = 0;
            stmt.setString(++k, report.getTopic());
            stmt.setInt(++k, report.getEventId());
            stmt.setInt(++k, report.getCreator().getId());
            stmt.setInt(++k, report.getSpeaker().getId());
            stmt.setBoolean(++k, report.isConfirmed());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                rs.next();
                report.setId(rs.getInt(1));
            }
        }
    }

    private Report extractReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        extractReport(rs, report);
        return report;
    }

    private void extractReport(ResultSet rs, Report report) throws SQLException {
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
    }
}
