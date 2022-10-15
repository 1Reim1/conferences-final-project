package com.my.conferences.logic;

import com.my.conferences.db.*;
import com.my.conferences.dto.ReportWithEvent;
import com.my.conferences.email.EmailManager;
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
    private static final EmailManager emailManager = EmailManager.getInstance();

    public static synchronized ReportManager getInstance() {
        if (instance == null) {
            instance = new ReportManager();
        }

        return instance;
    }

    private ReportManager() {
    }

    public List<ReportWithEvent> findNewReports(User user) throws DBException, ValidationException {
        Connection connection = connectionManager.getConnection();
        List<Report> reports;
        List<ReportWithEvent> reportsWithEvents;

        try {
            if (user.getRole() == User.Role.MODERATOR)
                reports = reportRepository.findNewForModerator(connection, user);
            else if (user.getRole() == User.Role.SPEAKER)
                reports = reportRepository.findNewForSpeaker(connection, user);
            else
                throw new ValidationException("You have not permissions");

            reportsWithEvents = new ArrayList<>(reports.size());
            for (Report report : reports) {
                Event event = eventRepository.findOne(connection, report.getEventId(), true);

                if (user.getRole() == User.Role.MODERATOR)
                    userRepository.findOne(connection, report.getSpeaker());
                else
                    userRepository.findOne(connection, event.getModerator());

                reportsWithEvents.add(new ReportWithEvent(report, event));
            }
        } catch (SQLException e) {
            throw new DBException("Unable to find new reports", e);
        } finally {
            connectionManager.closeConnection(connection);
        }

        return reportsWithEvents;
    }

    public void cancelReport(int reportId, User user) throws DBException, ValidationException {
        Connection connection = connectionManager.getConnection();
        try {
            Report report = reportRepository.findOne(connection, reportId);
            Event event = eventRepository.findOne(connection, report.getEventId(), true);
            EventManager.canInteractWithEvent(event);
            reportRepository.findAll(connection, event, false);
            userRepository.findOne(connection, event.getModerator());

            userRepository.findOne(connection, report.getSpeaker());
            if (!(report.getSpeaker().equals(user) || event.getModerator().equals(user)))
                throw new ValidationException("You have not permissions");

            reportRepository.delete(connection, report);

            if (report.isConfirmed()) {
                for (Report r : event.getReports())
                    userRepository.findOne(connection, r.getSpeaker());

                userRepository.findAllParticipants(connection, event);
                emailManager.sendConfirmedReportCancelled(report, event);
            } else {
                if (user.getRole() == User.Role.MODERATOR)
                    emailManager.sendReportCancelledByModerator(report, event);
                else
                    emailManager.sendReportCancelledBySpeaker(report, event);
            }
        } catch (SQLException e) {
            throw new DBException("Unable to cancel a report");
        } finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void confirmReport(int reportId, User user) throws DBException, ValidationException {
        Connection connection = connectionManager.getConnection();
        try {
            Report report = reportRepository.findOne(connection, reportId);
            Event event = eventRepository.findOne(connection, report.getEventId(), true);
            EventManager.canInteractWithEvent(event);
            userRepository.findOne(connection, event.getModerator());
            userRepository.findOne(connection, report.getCreator());
            userRepository.findOne(connection, report.getSpeaker());
            userRepository.findAllParticipants(connection, event);

            if (report.isConfirmed())
                throw new ValidationException("Report is already confirmed");
            if (!((event.getModerator().equals(user) && report.getCreator().equals(report.getSpeaker())) ||
                    (report.getSpeaker().equals(user) && report.getCreator().equals(event.getModerator())))) {
                throw new ValidationException("You have not permissions");
            }
            report.setConfirmed(true);
            reportRepository.update(connection, report);

            reportRepository.findAll(connection, event, false);
            for (Report r : event.getReports())
                userRepository.findOne(connection, r.getSpeaker());

            if (user.getRole() == User.Role.MODERATOR)
                emailManager.sendReportConfirmedByModerator(report, event);
            else
                emailManager.sendReportConfirmedBySpeaker(report, event);

            emailManager.sendAddedNewReport(report, event);
        } catch (SQLException e) {
            throw new DBException("Unable to confirm a report");
        } finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void offerReport(Report report) throws DBException, ValidationException {
        report.validate();
        Connection connection = connectionManager.getConnection();
        try {
            Event event = eventRepository.findOne(connection, report.getEventId(), true);
            EventManager.canInteractWithEvent(event);
            userRepository.findAllParticipants(connection, event);
            userRepository.findOne(connection, report.getSpeaker());

            if (report.getCreator().getRole() == User.Role.USER)
                throw new ValidationException("You have not permission");
            if (report.getCreator().getRole() == User.Role.SPEAKER && !report.getCreator().equals(report.getSpeaker()))
                throw new ValidationException("Speaker can not offer a report to someone");
            if (report.getSpeaker().getRole() != User.Role.SPEAKER)
                throw new ValidationException("User is not a speaker");
            if (event.getParticipants().contains(report.getSpeaker()))
                throw new ValidationException("Speaker is already a participant");

            report.setConfirmed(false);
            reportRepository.insert(connection, report);

            if (report.getCreator().getRole() == User.Role.SPEAKER) {
                userRepository.findOne(connection, event.getModerator());
                emailManager.sendReportOfferedBySpeaker(report, event);
            } else {
                emailManager.sendReportOfferedByModerator(report, event);
            }
        } catch (SQLException e) {
            throw new DBException("Unable to offer a report");
        } finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void modifyReportTopic(int reportId, String topic, User user) throws DBException, ValidationException {
        Report.validateTopic(topic);
        Connection connection = connectionManager.getConnection();
        try {
            Report report = reportRepository.findOne(connection, reportId);
            Event event = eventRepository.findOne(connection, report.getEventId(), true);
            EventManager.canInteractWithEvent(event);

            userRepository.findOne(connection, event.getModerator());
            if (!event.getModerator().equals(user))
                throw new ValidationException("You have not permissions");

            String prevTopic = report.getTopic();
            report.setTopic(topic);
            reportRepository.update(connection, report);

            if (report.isConfirmed()) {
                userRepository.findAllParticipants(connection, event);
                reportRepository.findAll(connection, event, false);
                for (Report r : event.getReports()) {
                    userRepository.findOne(connection, r.getSpeaker());
                }
                emailManager.sendConfirmedReportTopicChanged(report, event, prevTopic);
            } else {
                emailManager.sendReportTopicChanged(report, event, prevTopic);
            }
        } catch (SQLException e) {
            throw new DBException("Unable to modify a topic");
        } finally {
            connectionManager.closeConnection(connection);
        }
    }
}
