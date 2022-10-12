package com.my.conferences.logic;

import com.my.conferences.db.*;
import com.my.conferences.email.EmailManager;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventManager {
    private static EventManager instance;
    private static final ConnectionManager connectionManager = ConnectionManager.getInstance();
    private static final EventRepository eventRepository = EventRepository.getInstance();
    private static final ReportRepository reportRepository = ReportRepository.getInstance();
    private static final UserRepository userRepository = UserRepository.getInstance();
    private static final EmailManager emailManager = EmailManager.getInstance();
    private static final int PAGE_SIZE = 2;

    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }

        return instance;
    }

    private EventManager() {

    }

    public List<Event> findAll(int page, Event.Order order, boolean reverseOrder, boolean futureOrder, User user) throws DBException {
        if (page == 0)
            return new ArrayList<>();
        Connection connection = connectionManager.getConnection();
        List<Event> events;
        try {
            if (user == null)
                events = eventRepository.findAll(connection, order, reverseOrder, futureOrder, PAGE_SIZE, page);
            else
                events = eventRepository.findAll(connection, order, reverseOrder, futureOrder, PAGE_SIZE, page, user);
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

    public int countPages(boolean futureOrder, User user) throws DBException {
        Connection connection = connectionManager.getConnection();
        try {
            if (user == null)
                return (int) Math.ceil((double) eventRepository.getCount(connection, futureOrder) / PAGE_SIZE);
            return (int) Math.ceil((double) eventRepository.getCount(connection, futureOrder, user) / PAGE_SIZE);
        } catch (SQLException e) {
            throw new DBException("Count of pages was not loaded", e);
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void create(Event event) throws DBException {
        event.validate();
        Connection connection = connectionManager.getConnection();
        try {
            eventRepository.insert(connection, event);
        }   catch (SQLException e) {
            throw new DBException("Unable to insert an event", e);
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void join(int eventId, User user) throws DBException {
        Connection connection = connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, false);
            canInteractWithEvent(event);
            if (event.getModerator().equals(user))
                throw new DBException("You are a moderator");
            if (user.getRole() == User.Role.SPEAKER)
                if (event.getReports().stream().map(Report::getSpeaker).anyMatch(s -> s.equals(user)))
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
            canInteractWithEvent(event);
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
            if (event.isHidden())
                throw new DBException("Event is already hidden");
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

    public void show(int eventId, User user) throws DBException {
        Connection connection = connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, true);
            if (!event.isHidden())
                throw new DBException("Event is already shown");
            if (!event.getModerator().equals(user))
                throw new DBException("You have not permissions");
            event.setHidden(false);
            eventRepository.update(connection, event);
        }   catch (SQLException e) {
            throw new DBException("Unable to show the event", e);
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void modifyTitle(int eventId, String newTitle, User user) throws DBException {
        Event.validateTitle(newTitle);
        Connection connection = connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, true);
            canInteractWithEvent(event);
            if (!event.getModerator().equals(user))
                throw new DBException("You have not permission");
            String prevTitle = event.getTitle();
            event.setTitle(newTitle);
            userRepository.findAllParticipants(connection, event);
            reportRepository.findAll(connection, event, false);
            for (Report report : event.getReports())
                userRepository.findOne(connection, report.getSpeaker());
            userRepository.findOne(connection, event.getModerator());
            eventRepository.update(connection, event);

            emailManager.sendTitleChanged(event, prevTitle);
        }   catch (SQLException e) {
            throw new DBException("Unable to modify a title", e);
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void modifyDescription(int eventId, String newDescription, User user) throws DBException {
        Event.validateDescription(newDescription);
        Connection connection = connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, true);
            canInteractWithEvent(event);
            if (!event.getModerator().equals(user))
                throw new DBException("You have not permission");
            event.setDescription(newDescription);
            reportRepository.findAll(connection, event, false);
            for (Report report : event.getReports())
                userRepository.findOne(connection, report.getSpeaker());
            userRepository.findOne(connection, event.getModerator());
            eventRepository.update(connection, event);

            emailManager.sendDescriptionChanged(event);
        }   catch (SQLException e) {
            throw new DBException("Unable to modify a description", e);
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void modifyDate(int eventId, Date newDate, User user) throws DBException {
        Event.validateDate(newDate);
        Connection connection = connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, true);
            canInteractWithEvent(event);
            if (!event.getModerator().equals(user))
                throw new DBException("You have not permission");
            Date prevDate = event.getDate();
            event.setDate(newDate);
            reportRepository.findAll(connection, event, false);
            for (Report report : event.getReports())
                userRepository.findOne(connection, report.getSpeaker());
            userRepository.findOne(connection, event.getModerator());
            eventRepository.update(connection, event);

            emailManager.sendDateChanged(event, prevDate);
        }   catch (SQLException e) {
            throw new DBException("Unable to modify a date", e);
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    public void modifyPlace(int eventId, String newPlace, User user) throws DBException {
        Event.validatePlace(newPlace);
        Connection connection = connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, true);
            canInteractWithEvent(event);
            if (!event.getModerator().equals(user))
                throw new DBException("You have not permission");
            String prevPlace = event.getPlace();
            event.setPlace(newPlace);
            reportRepository.findAll(connection, event, false);
            for (Report report : event.getReports())
                userRepository.findOne(connection, report.getSpeaker());
            userRepository.findOne(connection, event.getModerator());
            eventRepository.update(connection, event);

            emailManager.sendPlaceChanged(event, prevPlace);
        }   catch (SQLException e) {
            throw new DBException("Unable to modify a place", e);
        }   finally {
            connectionManager.closeConnection(connection);
        }
    }

    static void canInteractWithEvent(Event event) throws DBException {
        try {
            Event.validateDate(event.getDate());
        }   catch (DBException e) {
            throw new DBException("You can’t interact with a past event", e);
        }
    }
}
