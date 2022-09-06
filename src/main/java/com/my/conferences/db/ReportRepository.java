package com.my.conferences.db;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportRepository {
    private static ReportRepository instance;
    private static final String GET_ALL = "SELECT * FROM reports WHERE event_id = ?";
    private static final String GET_ALL_ONLY_CONFIRMED = "SELECT * FROM reports WHERE event_id = ? AND confirmed = true";
    private static final String GET_ONE = "SELECT * FROM reports WHERE id = ?";
    private static final String DELETE_ONE = "DELETE FROM reports WHERE id = ?";

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

    private Report extractReport(ResultSet rs) throws SQLException {
        Report report = new Report();
        extractReport(rs, report);
        return report;
    }

    private void extractReport(ResultSet rs, Report report) throws SQLException {
        report.setId(rs.getInt("id"));
        report.setTitle(rs.getString("topic"));
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
