package com.my.conferences.service;

import com.my.conferences.dao.EventDao;
import com.my.conferences.dao.ReportDao;
import com.my.conferences.dao.UserDao;
import com.my.conferences.dao.factory.DaoFactory;
import com.my.conferences.email.EmailManager;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;
import com.my.conferences.util.ConnectionUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventService {
    private static final EmailManager emailManager = EmailManager.getInstance();
    private final EventDao eventDao;
    private final ReportDao reportDao;
    private final UserDao userDao;
    private final int PAGE_SIZE;

    public EventService(DaoFactory daoFactory, int pageSize) {
        eventDao = daoFactory.getEventDao();
        reportDao = daoFactory.getReportDao();
        userDao = daoFactory.getUserDao();
        PAGE_SIZE = pageSize;
    }

    public List<Event> findAll(int page, Event.Order order, boolean reverseOrder, boolean futureOrder, boolean onlyMyEvents, User user) throws DBException {
        if (page == 0)
            return new ArrayList<>();

        if (order != Event.Order.DATE)
            reverseOrder = !reverseOrder;

        Connection connection = ConnectionUtil.getConnection();
        List<Event> events;
        try {
            if (onlyMyEvents)
                events = eventDao.findAllMy(connection, order, reverseOrder, futureOrder, PAGE_SIZE, page, user);
            else
                events = eventDao.findAll(connection, order, reverseOrder, futureOrder, PAGE_SIZE, page, user.getLanguage());

            for (Event event : events) {
                reportDao.findAll(connection, event, true);
                userDao.findAllParticipants(connection, event);
            }
        } catch (SQLException e) {
            throw new DBException("events was not loaded", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }

        return events;
    }

    private Event findOne(Connection connection, int id, boolean showHidden) throws DBException {
        Event event;
        try {
            event = eventDao.findOne(connection, id, showHidden);
            userDao.findAllParticipants(connection, event);
            reportDao.findAll(connection, event, !showHidden);
            userDao.findOne(connection, event.getModerator());
            for (Report report : event.getReports()) {
                userDao.findOne(connection, report.getCreator());
                userDao.findOne(connection, report.getSpeaker());
            }
        } catch (SQLException e) {
            throw new DBException("Event was not found", e);
        }

        return event;
    }

    public Event findOne(int id, User user) throws DBException {
        Connection connection = ConnectionUtil.getConnection();

        Event event;
        try {
            event = findOne(connection, id, user.getRole() != User.Role.USER);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }

        return event;
    }

    public int countPages(boolean futureOrder, boolean onlyMyEvents, User user) throws DBException {
        Connection connection = ConnectionUtil.getConnection();
        try {
            if (onlyMyEvents) {
                return (int) Math.ceil((double) eventDao.findCountMy(connection, futureOrder, user) / PAGE_SIZE);
            }

            return (int) Math.ceil((double) eventDao.findCount(connection, futureOrder, user.getLanguage()) / PAGE_SIZE);
        } catch (SQLException e) {
            throw new DBException("Count of pages was not loaded", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    public void create(Event event) throws DBException, ValidationException {
        event.setStatistics(-1);
        event.setHidden(true);
        event.setLanguage(event.getModerator().getLanguage());
        event.validate();

        Connection connection = ConnectionUtil.getConnection();
        try {
            eventDao.insert(connection, event);
        } catch (SQLException e) {
            throw new DBException("Unable to insert an event", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    public void join(int eventId, User user) throws DBException, ValidationException {
        Connection connection = ConnectionUtil.getConnection();
        try {
            Event event = findOne(connection, eventId, false);
            canInteractWithEvent(event);

            if (event.getModerator().equals(user))
                throw new ValidationException("You are a moderator");
            if (user.getRole() == User.Role.SPEAKER) {
                if (event.getReports().stream().map(Report::getSpeaker).anyMatch(s -> s.equals(user)))
                    throw new ValidationException("You have a report");
            }

            if (event.getParticipants().contains(user))
                throw new ValidationException("You are already a participant");

            userDao.insertParticipant(connection, event, user);
        } catch (SQLException e) {
            throw new DBException("Unable to join to the event", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    public void leave(int eventId, User user) throws DBException, ValidationException {
        Connection connection = ConnectionUtil.getConnection();
        try {
            Event event = findOne(connection, eventId, false);
            canInteractWithEvent(event);

            if (!event.getParticipants().contains(user))
                throw new ValidationException("You are not a participant");

            userDao.deleteParticipant(connection, event, user);
        } catch (SQLException e) {
            throw new DBException("Unable to leave from the event", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    public void hide(int eventId, User user) throws DBException, ValidationException {
        Connection connection = ConnectionUtil.getConnection();
        try {
            Event event = findOne(connection, eventId, true);

            if (event.isHidden())
                throw new ValidationException("Event is already hidden");
            if (!event.getModerator().equals(user))
                throw new ValidationException("You have not permissions");

            event.setHidden(true);
            eventDao.update(connection, event);
        } catch (SQLException e) {
            throw new DBException("Unable to hide the event");
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    public void show(int eventId, User user) throws DBException, ValidationException {
        Connection connection = ConnectionUtil.getConnection();
        try {
            Event event = findOne(connection, eventId, true);

            if (!event.isHidden())
                throw new ValidationException("Event is already shown");
            if (!event.getModerator().equals(user))
                throw new ValidationException("You have not permissions");

            event.setHidden(false);
            eventDao.update(connection, event);
        } catch (SQLException e) {
            throw new DBException("Unable to show the event", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    public void modifyTitle(int eventId, String newTitle, User user) throws DBException, ValidationException {
        Event.validateTitle(newTitle);
        Connection connection = ConnectionUtil.getConnection();
        try {
            Event event = findOne(connection, eventId, true);
            canInteractWithEvent(event);

            if (!event.getModerator().equals(user))
                throw new ValidationException("You have not permission");

            String prevTitle = event.getTitle();
            event.setTitle(newTitle);
            eventDao.update(connection, event);

            emailManager.sendTitleChanged(event, prevTitle);
        } catch (SQLException e) {
            throw new DBException("Unable to modify a title", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    public void modifyDescription(int eventId, String newDescription, User user) throws DBException, ValidationException {
        Event.validateDescription(newDescription);
        Connection connection = ConnectionUtil.getConnection();
        try {
            Event event = findOne(connection, eventId, true);
            canInteractWithEvent(event);

            if (!event.getModerator().equals(user))
                throw new ValidationException("You have not permission");

            event.setDescription(newDescription);
            eventDao.update(connection, event);

            emailManager.sendDescriptionChanged(event);
        } catch (SQLException e) {
            throw new DBException("Unable to modify a description", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    public void modifyDate(int eventId, Date newDate, User user) throws DBException, ValidationException {
        Event.validateDate(newDate);
        Connection connection = ConnectionUtil.getConnection();
        try {
            Event event = findOne(connection, eventId, true);
            canInteractWithEvent(event);

            if (!event.getModerator().equals(user))
                throw new ValidationException("You have not permission");

            Date prevDate = event.getDate();
            event.setDate(newDate);
            eventDao.update(connection, event);

            emailManager.sendDateChanged(event, prevDate);
        } catch (SQLException e) {
            throw new DBException("Unable to modify a date", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    public void modifyPlace(int eventId, String newPlace, User user) throws DBException, ValidationException {
        Event.validatePlace(newPlace);
        Connection connection = ConnectionUtil.getConnection();
        try {
            Event event = findOne(connection, eventId, true);
            canInteractWithEvent(event);

            if (!event.getModerator().equals(user))
                throw new ValidationException("You have not permission");

            String prevPlace = event.getPlace();
            event.setPlace(newPlace);
            eventDao.update(connection, event);

            emailManager.sendPlaceChanged(event, prevPlace);
        } catch (SQLException e) {
            throw new DBException("Unable to modify a place", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    public void modifyStatistics(int eventId, int newStatistics, User user) throws DBException, ValidationException {
        Connection connection = ConnectionUtil.getConnection();
        try {
            Event event = findOne(connection, eventId, true);

            if (!event.getModerator().equals(user))
                throw new ValidationException("You have not permission");
            if (event.getDate().compareTo(new Date()) > 0)
                throw new ValidationException("Unable to modify statistics for a future event");
            if (newStatistics < 0)
                throw new ValidationException("Statistics should be greater than zero");
            if (newStatistics > event.getParticipants().size())
                throw new ValidationException("Statistics should be lesser than a number of participants");

            event.setStatistics(newStatistics);
            eventDao.update(connection, event);
        } catch (SQLException e) {
            throw new DBException("Unable to modify a statistics", e);
        } finally {
            ConnectionUtil.closeConnection(connection);
        }
    }

    static void canInteractWithEvent(Event event) throws ValidationException {
        try {
            Event.validateDate(event.getDate());
        } catch (ValidationException e) {
            throw new ValidationException("You can’t interact with a past event", e);
        }
    }
}
