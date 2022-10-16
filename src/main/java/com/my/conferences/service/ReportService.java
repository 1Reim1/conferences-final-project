package com.my.conferences.service;

import com.my.conferences.dao.EventDao;
import com.my.conferences.dao.ReportDao;
import com.my.conferences.dao.UserDao;
import com.my.conferences.dto.ReportWithEvent;
import com.my.conferences.email.EmailManager;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;
import com.my.conferences.util.ConnectionUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportService {
    private final EmailManager emailManager;
    private final EventDao eventDao;
    private final ReportDao reportDao;
    private final UserDao userDao;

    public ReportService(EmailManager emailManager, EventDao eventDao, ReportDao reportDao, UserDao userDao) {
        this.emailManager = emailManager;
        this.eventDao = eventDao;
        this.reportDao = reportDao;
        this.userDao = userDao;
    }

    public List<ReportWithEvent> findNewReports(User user) throws DBException, ValidationException {
        Connection connection = ConnectionUtil.getConnection();
        List<Report> reports;
        List<ReportWithEvent> reportsWithEvents;

        try {
            if (user.getRole() == User.Role.MODERATOR)
                reports = reportDao.findNewForModerator(connection, user);
            else if (user.getRole() == User.Role.SPEAKER)
                reports = reportDao.findNewForSpeaker(connection, user);
            else
                throw new ValidationException("You have not permissions");

            reportsWithEvents = new ArrayList<>(reports.size());
            for (Report report : reports) {
                Event event = eventDao.findOne(connection, report.getEventId(), true);

                if (user.getRole() == User.Role.MODERATOR)
                    userDao.findOne(connection, report.getSpeaker());
                else
                    userDao.findOne(connection, event.getModerator());

                reportsWithEvents.add(new ReportWithEvent(report, event));
            }
        } catch (SQLException e) {
            throw new DBException("Unable to find new reports", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }

        return reportsWithEvents;
    }

    public void cancelReport(int reportId, User user) throws DBException, ValidationException {
        Connection connection = ConnectionUtil.getConnection();
        try {
            Report report = reportDao.findOne(connection, reportId);
            Event event = eventDao.findOne(connection, report.getEventId(), true);
            EventService.canInteractWithEvent(event);
            reportDao.findAll(connection, event, false);
            userDao.findOne(connection, event.getModerator());

            userDao.findOne(connection, report.getSpeaker());
            if (!(report.getSpeaker().equals(user) || event.getModerator().equals(user)))
                throw new ValidationException("You have not permissions");

            reportDao.delete(connection, report);

            if (report.isConfirmed()) {
                for (Report r : event.getReports())
                    userDao.findOne(connection, r.getSpeaker());

                userDao.findAllParticipants(connection, event);
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
            ConnectionUtil.closeConnection(connection);
        }
    }

    public void confirmReport(int reportId, User user) throws DBException, ValidationException {
        Connection connection = ConnectionUtil.getConnection();
        try {
            Report report = reportDao.findOne(connection, reportId);
            Event event = eventDao.findOne(connection, report.getEventId(), true);
            EventService.canInteractWithEvent(event);
            userDao.findOne(connection, event.getModerator());
            userDao.findOne(connection, report.getCreator());
            userDao.findOne(connection, report.getSpeaker());
            userDao.findAllParticipants(connection, event);

            if (report.isConfirmed())
                throw new ValidationException("Report is already confirmed");
            if (!((event.getModerator().equals(user) && report.getCreator().equals(report.getSpeaker())) ||
                    (report.getSpeaker().equals(user) && report.getCreator().equals(event.getModerator())))) {
                throw new ValidationException("You have not permissions");
            }
            report.setConfirmed(true);
            reportDao.update(connection, report);

            reportDao.findAll(connection, event, false);
            for (Report r : event.getReports())
                userDao.findOne(connection, r.getSpeaker());

            if (user.getRole() == User.Role.MODERATOR)
                emailManager.sendReportConfirmedByModerator(report, event);
            else
                emailManager.sendReportConfirmedBySpeaker(report, event);

            emailManager.sendAddedNewReport(report, event);
        } catch (SQLException e) {
            throw new DBException("Unable to confirm a report");
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    public void offerReport(Report report) throws DBException, ValidationException {
        report.validate();
        Connection connection = ConnectionUtil.getConnection();
        try {
            Event event = eventDao.findOne(connection, report.getEventId(), true);
            EventService.canInteractWithEvent(event);
            userDao.findAllParticipants(connection, event);
            userDao.findOne(connection, report.getSpeaker());

            if (report.getCreator().getRole() == User.Role.USER)
                throw new ValidationException("You have not permission");
            if (report.getCreator().getRole() == User.Role.SPEAKER && !report.getCreator().equals(report.getSpeaker()))
                throw new ValidationException("Speaker can not offer a report to someone");
            if (report.getSpeaker().getRole() != User.Role.SPEAKER)
                throw new ValidationException("User is not a speaker");
            if (event.getParticipants().contains(report.getSpeaker()))
                throw new ValidationException("Speaker is already a participant");

            report.setConfirmed(false);
            reportDao.insert(connection, report);

            if (report.getCreator().getRole() == User.Role.SPEAKER) {
                userDao.findOne(connection, event.getModerator());
                emailManager.sendReportOfferedBySpeaker(report, event);
            } else {
                emailManager.sendReportOfferedByModerator(report, event);
            }
        } catch (SQLException e) {
            throw new DBException("Unable to offer a report");
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    public void modifyReportTopic(int reportId, String topic, User user) throws DBException, ValidationException {
        Report.validateTopic(topic);
        Connection connection = ConnectionUtil.getConnection();
        try {
            Report report = reportDao.findOne(connection, reportId);
            Event event = eventDao.findOne(connection, report.getEventId(), true);
            EventService.canInteractWithEvent(event);

            userDao.findOne(connection, event.getModerator());
            if (!event.getModerator().equals(user))
                throw new ValidationException("You have not permissions");

            String prevTopic = report.getTopic();
            report.setTopic(topic);
            reportDao.update(connection, report);

            if (report.isConfirmed()) {
                userDao.findAllParticipants(connection, event);
                reportDao.findAll(connection, event, false);
                for (Report r : event.getReports()) {
                    userDao.findOne(connection, r.getSpeaker());
                }
                emailManager.sendConfirmedReportTopicChanged(report, event, prevTopic);
            } else {
                emailManager.sendReportTopicChanged(report, event, prevTopic);
            }
        } catch (SQLException e) {
            throw new DBException("Unable to modify a topic");
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }
}
