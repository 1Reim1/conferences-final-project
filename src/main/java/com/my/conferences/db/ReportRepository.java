package com.my.conferences.db;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportRepository {
    private static ReportRepository instance;
    private static final String GET_ALL_REPORTS_BY_EVENT = "SELECT * FROM reports WHERE event_id = ?";

    public static synchronized ReportRepository getInstance() {
        if (instance == null) {
            instance = new ReportRepository();
        }

        return instance;
    }

    private ReportRepository() {

    }

    public void findAllByEvent(Connection connection, Event event) throws SQLException {
        List<Report> reports = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(GET_ALL_REPORTS_BY_EVENT)) {
            stmt.setInt(1, event.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reports.add(extractReport(rs));
                }

                event.setReports(reports);
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
        report.setTitle(rs.getString("topic"));
        User speaker = new User();
        speaker.setId(rs.getInt("speaker_id"));
        report.setSpeaker(speaker);
    }
}
