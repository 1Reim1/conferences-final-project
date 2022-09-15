package com.my.conferences.logic;

import com.my.conferences.db.*;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;

import java.sql.Connection;
import java.sql.SQLException;

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

    public void cancelReport(int reportId, User user) throws DBException {
        Connection connection = connectionManager.getConnection();
        try {
            Report report = reportRepository.findOne(connection, reportId);
            Event event = eventRepository.findOne(connection, report.getEventId(), true);
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

    public void offerReport(int eventId, String topic, int speakerId, User creator) throws DBException {
        topic = topic.trim();
        if (topic.length() < 3)
            throw new DBException("Topic length min: 3");
        Connection connection = connectionManager.getConnection();
        try {
            Event event = eventRepository.findOne(connection, eventId, true);
            userRepository.findAllParticipants(connection, event);
            User speaker = new User();
            speaker.setId(speakerId);
            userRepository.findOne(connection, speaker);
            if (creator.getRole() == User.Role.USER)
                throw new DBException("You have not permission");
            if (creator.getRole() == User.Role.SPEAKER && !creator.equals(speaker))
                throw new DBException("Speaker can not offer a report to someone");
            if (speaker.getRole() != User.Role.SPEAKER)
                throw new DBException("User is not a speaker");
            if (event.getParticipants().contains(speaker))
                throw new DBException("Speaker is already a participant");

            Report report = new Report();
            report.setTopic(topic);
            report.setEventId(eventId);
            report.setCreator(creator);
            report.setSpeaker(speaker);
            report.setConfirmed(false);
            reportRepository.insert(connection, report);
        } catch (SQLException e) {
            throw new DBException("Unable to offer a report");
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void modifyReportTopic(int reportId, String topic, User user) throws DBException {
        topic = topic.trim();
        if (topic.length() < 3)
            throw new DBException("Topic length min: 3");
        Connection connection = connectionManager.getConnection();
        try {
            Report report = reportRepository.findOne(connection, reportId);
            Event event = eventRepository.findOne(connection, report.getEventId(), true);
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