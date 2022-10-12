package com.my.conferences.logic;

import com.my.conferences.db.*;
import com.my.conferences.dto.ReportWithEvent;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportManager {
    private static ReportManager instance;
    private static final ConnectionManager connectionManager = ConnectionManager.getInstance();
    private static final EventRepository eventRepository = EventRepository.getInstance();
    private static final ReportRepository reportRepository = ReportRepository.getInstance();
    private static final UserRepository userRepository = UserRepository.getInstance();

    public static synchronized ReportManager getInstance() {
        if (instance == null) {
            instance = new ReportManager();
        }

        return instance;
    }

    public List<ReportWithEvent> findNewReports(User user) throws DBException {
        Connection connection = connectionManager.getConnection();
        List<Report> reports;
        List<ReportWithEvent> reportsWithEvents;
        try {
            if (user.getRole() == User.Role.MODERATOR)
                reports = reportRepository.findNewForModerator(connection, user);
            else if (user.getRole() == User.Role.SPEAKER)
                reports = reportRepository.findNewForSpeaker(connection, user);
            else
                throw new DBException("You have not permissions");

            reportsWithEvents = new ArrayList<>(reports.size());
            for (Report report : reports) {
                Event event = eventRepository.findOne(connection, report.getEventId(), true);

                if (user.getRole() == User.Role.MODERATOR)
                    userRepository.findOne(connection, report.getSpeaker());
                else
                    userRepository.findOne(connection, event.getModerator());

                reportsWithEvents.add(new ReportWithEvent(report, event));
            }

        }   catch (SQLException e) {
            throw new DBException("Unable to find new reports", e);
        }   finally {
            connectionManager.closeConnection(connection);
        }

        return reportsWithEvents;
    }

    public void cancelReport(int reportId, User user) throws DBException {
        Connection connection = connectionManager.getConnection();
        try {
            Report report = reportRepository.findOne(connection, reportId);
            Event event = eventRepository.findOne(connection, report.getEventId(), true);
            EventManager.canInteractWithEvent(event);
            userRepository.findOne(connection, event.getModerator());
            userRepository.findOne(connection, report.getSpeaker());
            if (!(report.getSpeaker().equals(user) || event.getModerator().equals(user)))
                throw new DBException("You have not permissions");
            reportRepository.delete(connection, report);
        }   catch (SQLException e) {
            throw new DBException("Unable to cancel a report");
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void confirmReport(int reportId, User user) throws DBException {
        Connection connection = connectionManager.getConnection();
        try {
            Report report = reportRepository.findOne(connection, reportId);
            Event event = eventRepository.findOne(connection, report.getEventId(), true);
            EventManager.canInteractWithEvent(event);
            userRepository.findOne(connection, event.getModerator());
            userRepository.findOne(connection, report.getCreator());
            userRepository.findOne(connection, report.getSpeaker());
            if (report.isConfirmed())
                throw new DBException("Report is already confirmed");
            if (!((event.getModerator().equals(user) && report.getCreator().equals(report.getSpeaker())) ||
                    (report.getSpeaker().equals(user) && report.getCreator().equals(event.getModerator())))) {
                throw new DBException("You have not permissions");
            }
            report.setConfirmed(true);
            reportRepository.update(connection, report);
        } catch (SQLException e) {
            throw new DBException("Unable to confirm a report");
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void offerReport(Report report) throws DBException {
        report.validate();
        Connection connection = connectionManager.getConnection();
        try {
            Event event = eventRepository.findOne(connection, report.getEventId(), true);
            EventManager.canInteractWithEvent(event);
            userRepository.findAllParticipants(connection, event);
            userRepository.findOne(connection, report.getSpeaker());
            if (report.getCreator().getRole() == User.Role.USER)
                throw new DBException("You have not permission");
            if (report.getCreator().getRole() == User.Role.SPEAKER && !report.getCreator().equals(report.getSpeaker()))
                throw new DBException("Speaker can not offer a report to someone");
            if (report.getSpeaker().getRole() != User.Role.SPEAKER)
                throw new DBException("User is not a speaker");
            if (event.getParticipants().contains(report.getSpeaker()))
                throw new DBException("Speaker is already a participant");

            report.setConfirmed(false);
            reportRepository.insert(connection, report);
        } catch (SQLException e) {
            throw new DBException("Unable to offer a report");
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void modifyReportTopic(int reportId, String topic, User user) throws DBException {
        Report.validateTopic(topic);
        Connection connection = connectionManager.getConnection();
        try {
            Report report = reportRepository.findOne(connection, reportId);
            Event event = eventRepository.findOne(connection, report.getEventId(), true);
            EventManager.canInteractWithEvent(event);
            userRepository.findOne(connection, event.getModerator());
            if (!event.getModerator().equals(user))
                throw new DBException("You have not permissions");

            report.setTopic(topic);
            reportRepository.update(connection, report);
        } catch (SQLException e) {
            throw new DBException("Unable to modify a topic");
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }
}
