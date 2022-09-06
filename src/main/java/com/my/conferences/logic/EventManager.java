package com.my.conferences.logic;

import com.my.conferences.db.*;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EventManager {
    private static EventManager instance;
    private static final ConnectionManager connectionManager = ConnectionManager.getInstance();
    private static final EventRepository eventRepository = EventRepository.getInstance();
    private static final ReportRepository reportRepository = ReportRepository.getInstance();
    private static final UserRepository userRepository = UserRepository.getInstance();
    private static final int PAGE_SIZE = 2;

    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }

        return instance;
    }

    private EventManager() {

    }

    public List<Event> findAll(int page, Event.Order order, boolean reverseOrder) throws DBException {
        if (page == 0)
            return new ArrayList<>();
        Connection connection = connectionManager.getConnection();
        List<Event> events;
        try {
            events = eventRepository.findAll(connection, order, reverseOrder, PAGE_SIZE, page);
            for (Event event : events) {
                reportRepository.findAll(connection, event, true);
                userRepository.findAllParticipants(connection, event);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DBException("events was not loaded", e);
        }   finally {
            connectionManager.closeConnection(connection);
        }

        return events;
    }

    private Event findOne(Connection connection, int id, boolean showHidden) throws DBException {
        Event event;
        try {
            event = eventRepository.findOne(connection, id, showHidden);
            userRepository.findAllParticipants(connection, event);
            reportRepository.findAll(connection, event, !showHidden);
            userRepository.findOne(connection, event.getModerator());
            for (Report report : event.getReports()) {
                userRepository.findOne(connection, report.getCreator());
                userRepository.findOne(connection, report.getSpeaker());
            }
        } catch (SQLException e) {
            throw new DBException("Event was not found", e);
        }

        return event;
    }

    public Event findOne(int id, boolean showHidden) throws DBException {
        Connection connection = connectionManager.getConnection();

        Event event;
        try {
            event = findOne(connection, id, showHidden);
        } finally {
            connectionManager.closeConnection(connection);
        }

        return event;
    }

    public int countPages() throws DBException {
        Connection connection = connectionManager.getConnection();
        try {
            return (int) Math.ceil((double) eventRepository.getCount(connection) / PAGE_SIZE);
        } catch (SQLException e) {
            throw new DBException("count of pages was not loaded");
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void join(int eventId, User user) throws DBException {
        Connection connection = connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, false);
            if (event.getModerator().equals(user))
                throw new DBException("You are a moderator");
            if (user.getRole() == User.Role.SPEAKER)
                for (Report report : event.getReports())
                    if (report.getSpeaker().equals(user))
                        throw new DBException("You have a report");
            if (event.getParticipants().contains(user))
                throw new DBException("You are already a participant");
            eventRepository.insertParticipant(connection, event, user);
        } catch (SQLException e) {
            throw new DBException("Unable to join to the event", e);
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void leave(int eventId, User user) throws DBException {
        Connection connection = connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, false);
            if (!event.getParticipants().contains(user))
                throw new DBException("You are not a participant");
            eventRepository.deleteParticipant(connection, event, user);
        } catch (SQLException e) {
            throw new DBException("Unable to leave from the event", e);
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void hide(int eventId, User user) throws DBException {
        Connection connection = connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, true);
            if (!event.getModerator().equals(user))
                throw new DBException("You have not permissions");
            event.setHidden(true);
            eventRepository.update(connection, event);
        }   catch (SQLException e) {
            throw new DBException("Unable to hide the event");
        }   finally {
            connectionManager.closeConnection(connection);
        }
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
            reportRepository.confirm(connection, report);
        } catch (SQLException e) {
            throw new DBException("Unable to confirm a report");
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void offerReport(int eventId, String topic, int speakerId, User creator) throws DBException {
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
}
