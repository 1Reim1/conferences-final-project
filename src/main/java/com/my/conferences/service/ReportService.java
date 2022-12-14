package com.my.conferences.service;

import com.my.conferences.dao.EventDao;
import com.my.conferences.dao.ReportDao;
import com.my.conferences.dao.UserDao;
import com.my.conferences.dto.ReportWithEvent;
import com.my.conferences.email.EmailManager;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;
import com.my.conferences.exception.DBException;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.validation.ReportValidation;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class with logic for interaction with reports
 */
public class ReportService {

    private static final Logger logger = Logger.getLogger(ReportService.class);
    private final EmailManager emailManager;
    private final ConnectionManager connectionManager;
    private final EventDao eventDao;
    private final ReportDao reportDao;
    private final UserDao userDao;

    public ReportService(EmailManager emailManager, ConnectionManager connectionManager, EventDao eventDao, ReportDao reportDao, UserDao userDao) {
        this.emailManager = emailManager;
        this.connectionManager = connectionManager;
        this.eventDao = eventDao;
        this.reportDao = reportDao;
        this.userDao = userDao;
    }

    /**
     * Returns the list of events with reports (that are not confirmed)
     *
     * @param user User for which performs search of new reports
     * @return List of reports with events which are not accepted or rejected
     */
    public List<ReportWithEvent> findNewReports(User user) throws DBException, ValidationException {
        Connection connection = this.connectionManager.getConnection();
        List<ReportWithEvent> reportsWithEvents;

        try {
            List<Report> reports;
            if (user.getRole() == User.Role.MODERATOR) {
                reports = reportDao.findNewForModerator(connection, user);
            } else if (user.getRole() == User.Role.SPEAKER) {
                reports = reportDao.findNewForSpeaker(connection, user);
            } else {
                throw new ValidationException("You have not permissions");
            }

            reportsWithEvents = new ArrayList<>(reports.size());
            for (Report report : reports) {
                Event event = eventDao.findOne(connection, report.getEventId(), true);

                if (user.getRole() == User.Role.MODERATOR) {
                    userDao.findOne(connection, report.getSpeaker());
                } else {
                    userDao.findOne(connection, event.getModerator());
                }

                reportsWithEvents.add(new ReportWithEvent(report, event));
            }
        } catch (SQLException e) {
            logger.error("SQLException in findNewReports", e);
            throw new DBException("Unable to find new reports", e);
        } finally {
            this.connectionManager.closeConnection(connection);
        }

        return reportsWithEvents;
    }

    /**
     * @param speakerId id of speaker
     * @return list of speaker's reports with events
     */
    public List<ReportWithEvent> findAllFutureSpeakerReports(int speakerId) throws DBException, ValidationException {
        Connection connection = this.connectionManager.getConnection();
        List<ReportWithEvent> reportsWithEvents;

        try {
            User speaker = new User();
            speaker.setId(speakerId);
            userDao.findOne(connection, speaker);
            if (speaker.getRole() != User.Role.SPEAKER) {
                throw new ValidationException("User should be a speaker");
            }

            List<Report> reports = reportDao.findAllBySpeaker(connection, speaker, true);
            reportsWithEvents = new ArrayList<>(reports.size());
            for (Report report : reports) {
                reportsWithEvents.add(new ReportWithEvent(report, eventDao.findOne(connection, report.getEventId(), true)));
            }

        } catch (SQLException e) {
            logger.error("SQLException in findAllFutureSpeakerReports", e);
            throw new DBException("Unable to find future reports of speaker");
        }

        return reportsWithEvents;
    }

    /**
     * Removes report
     *
     * @param reportId id of report
     * @param user     user which performs operation
     */
    public void cancelReport(int reportId, User user) throws DBException, ValidationException {
        Connection connection = this.connectionManager.getConnection();
        try {
            Report report = reportDao.findOne(connection, reportId);
            Event event = eventDao.findOne(connection, report.getEventId(), true);
            EventService.canInteractWithEventValidation(event);
            reportDao.findAll(connection, event, false);
            userDao.findOne(connection, event.getModerator());
            userDao.findOne(connection, report.getSpeaker());

            if (!(report.getSpeaker().equals(user) || event.getModerator().equals(user))) {
                throw new ValidationException("You have not permissions");
            }
            reportDao.delete(connection, report);
            // Send email notification about changing event
            if (report.isConfirmed()) {
                for (Report r : event.getReports()) {
                    userDao.findOne(connection, r.getSpeaker());
                }
                userDao.findAllParticipants(connection, event);
                emailManager.sendConfirmedReportCancelled(report, event);
            } else {
                if (user.getRole() == User.Role.MODERATOR) {
                    emailManager.sendReportCancelledByModerator(report, event);
                } else {
                    emailManager.sendReportCancelledBySpeaker(report, event);
                }
            }
        } catch (SQLException e) {
            logger.error("SQLException in cancelReport", e);
            throw new DBException("Unable to cancel a report");
        } finally {
            this.connectionManager.closeConnection(connection);
        }
    }

    /**
     * Confirms a report
     *
     * @param reportId id of report
     * @param user     user which performs operation
     */
    public void confirmReport(int reportId, User user) throws DBException, ValidationException {
        Connection connection = this.connectionManager.getConnection();
        try {
            Report report = reportDao.findOne(connection, reportId);
            Event event = eventDao.findOne(connection, report.getEventId(), true);
            EventService.canInteractWithEventValidation(event);
            userDao.findOne(connection, event.getModerator());
            userDao.findOne(connection, report.getCreator());
            userDao.findOne(connection, report.getSpeaker());
            userDao.findAllParticipants(connection, event);

            if (report.isConfirmed()) {
                throw new ValidationException("Report is already confirmed");
            }
            // throw exception if user is not a moderator (in case speaker created a report)
            // or if user is not a speaker (in case moderator created a report)
            if (!((event.getModerator().equals(user) && report.getCreator().equals(report.getSpeaker())) ||
                    (report.getSpeaker().equals(user) && report.getCreator().equals(event.getModerator())))) {
                throw new ValidationException("You have not permissions");
            }
            report.setConfirmed(true);
            reportDao.update(connection, report);
            // Send email notifications
            reportDao.findAll(connection, event, false);
            for (Report r : event.getReports()) {
                userDao.findOne(connection, r.getSpeaker());
            }
            if (user.getRole() == User.Role.MODERATOR) {
                emailManager.sendReportConfirmedByModerator(report, event);
            } else {
                emailManager.sendReportConfirmedBySpeaker(report, event);
            }
            // Send notification for all participants and speakers
            emailManager.sendAddedNewReport(report, event);
        } catch (SQLException e) {
            logger.error("SQLException in confirmReport", e);
            throw new DBException("Unable to confirm a report");
        } finally {
            this.connectionManager.closeConnection(connection);
        }
    }

    /**
     * Offers a report for event
     *
     * @param report report that should be offered
     */
    public void offerReport(Report report) throws DBException, ValidationException {
        report.validate();
        Connection connection = this.connectionManager.getConnection();
        try {
            Event event = eventDao.findOne(connection, report.getEventId(), true);
            EventService.canInteractWithEventValidation(event);
            userDao.findAllParticipants(connection, event);
            userDao.findOne(connection, report.getSpeaker());

            if (report.getCreator().getRole() == User.Role.USER) {
                throw new ValidationException("You have not permission");
            }
            if (report.getCreator().getRole() == User.Role.SPEAKER && !report.getCreator().equals(report.getSpeaker())) {
                throw new ValidationException("Speaker can not offer a report to someone");
            }
            if (report.getSpeaker().getRole() != User.Role.SPEAKER) {
                throw new ValidationException("User is not a speaker");
            }
            if (event.getParticipants().contains(report.getSpeaker())) {
                throw new ValidationException("Speaker is already a participant");
            }
            report.setConfirmed(false);
            reportDao.insert(connection, report);
            // Send email notification
            if (report.getCreator().getRole() == User.Role.SPEAKER) {
                userDao.findOne(connection, event.getModerator());
                emailManager.sendReportOfferedBySpeaker(report, event);
            } else {
                emailManager.sendReportOfferedByModerator(report, event);
            }
        } catch (SQLException e) {
            logger.error("SQLException in offerReport", e);
            throw new DBException("Unable to offer a report");
        } finally {
            this.connectionManager.closeConnection(connection);
        }
    }

    /**
     * Changes a topic of report
     *
     * @param reportId id of report whose topic should be changed
     * @param newTopic new topic
     * @param user     user that performs operation
     */
    public void modifyReportTopic(int reportId, String newTopic, User user) throws DBException, ValidationException {
        ReportValidation.validateTopic(newTopic);
        Connection connection = this.connectionManager.getConnection();
        try {
            Report report = reportDao.findOne(connection, reportId);
            Event event = eventDao.findOne(connection, report.getEventId(), true);
            EventService.canInteractWithEventValidation(event);
            userDao.findOne(connection, event.getModerator());
            if (!event.getModerator().equals(user)) {
                throw new ValidationException("You have not permissions");
            }

            String prevTopic = report.getTopic();
            report.setTopic(newTopic);
            reportDao.update(connection, report);
            // Send notifications
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
            logger.error("SQLException in modifyReportTopic", e);
            throw new DBException("Unable to modify a topic");
        } finally {
            this.connectionManager.closeConnection(connection);
        }
    }
}
