package com.my.conferences.service;

import com.my.conferences.dao.EventDao;
import com.my.conferences.dao.ReportDao;
import com.my.conferences.dao.UserDao;
import com.my.conferences.email.EmailManager;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;
import com.my.conferences.exception.DBException;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.validation.EventValidation;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class with logic for interaction with events
 */
public class EventService {

    private static final Logger logger = Logger.getLogger(EventService.class);
    private final EmailManager emailManager;
    private final ConnectionManager connectionManager;
    private final EventDao eventDao;
    private final ReportDao reportDao;
    private final UserDao userDao;
    private final int PAGE_SIZE;

    public EventService(EmailManager emailManager, ConnectionManager connectionManager, EventDao eventDao, ReportDao reportDao, UserDao userDao, int pageSize) {
        this.emailManager = emailManager;
        this.connectionManager = connectionManager;
        this.eventDao = eventDao;
        this.reportDao = reportDao;
        this.userDao = userDao;
        this.PAGE_SIZE = pageSize;
    }

    /**
     * returns all events in selected order
     * returns only events which user participate or all
     *
     * @param page page
     * @param order sort order
     * @param reverseOrder boolean value that represents reverse sort or not
     * @param futureEvents  boolean value that represents future or past events
     * @param onlyMyEvents boolean value that represent all or only my events
     * @param user         which performs operation
     * @return list of events
     */
    public List<Event> findAll(int page, Event.Order order, boolean reverseOrder, boolean futureEvents, boolean onlyMyEvents, User user) throws DBException {
        if (page == 0) {
            return new ArrayList<>();
        }
        if (order != Event.Order.DATE) {
            reverseOrder = !reverseOrder;
        }
        Connection connection = this.connectionManager.getConnection();
        List<Event> events;
        try {
            if (onlyMyEvents) {
                events = eventDao.findAllMy(connection, order, reverseOrder, futureEvents, PAGE_SIZE, page, user);
            } else {
                events = eventDao.findAll(connection, order, reverseOrder, futureEvents, PAGE_SIZE, page, user.getLanguage());
            }
            // load reports and participants for view layer
            for (Event event : events) {
                reportDao.findAll(connection, event, true);
                userDao.findAllParticipants(connection, event);
            }
        } catch (SQLException e) {
            logger.error("SQLException in findAll", e);
            throw new DBException("events was not loaded", e);
        } finally {
            this.connectionManager.closeConnection(connection);
        }

        return events;
    }

    /**
     * return count of pages
     *
     * @param futureOrder  boolean value that represents future or past events
     * @param onlyMyEvents boolean value that represent all or only my events
     * @param user         user that performs action
     * @return count of pages
     */
    public int countPages(boolean futureOrder, boolean onlyMyEvents, User user) throws DBException {
        Connection connection = this.connectionManager.getConnection();
        try {
            if (onlyMyEvents) {
                return (int) Math.ceil((double) eventDao.findCountMy(connection, futureOrder, user) / PAGE_SIZE);
            }

            return (int) Math.ceil((double) eventDao.findCount(connection, futureOrder, user.getLanguage()) / PAGE_SIZE);
        } catch (SQLException e) {
            logger.error("SQLException in countPages", e);
            throw new DBException("Count of pages was not loaded", e);
        } finally {
            this.connectionManager.closeConnection(connection);
        }
    }

    /**
     * returns all events moderated by a that moderator
     *
     * @param moderatorId id of moderator
     * @return list of events moderated by a that moderator
     */
    public List<Event> findAllModeratorEvents(int moderatorId) throws DBException, ValidationException {
        Connection connection = this.connectionManager.getConnection();
        try {
            User moderator = new User();
            moderator.setId(moderatorId);
            userDao.findOne(connection, moderator);
            if (moderator.getRole() != User.Role.MODERATOR) {
                throw new ValidationException("User is not a moderator");
            }

            return eventDao.findAllByModerator(connection, moderator);
        }   catch (SQLException e) {
            logger.error("SQLException in findAllModeratorEvents", e);
            throw new DBException("events was not loaded", e);
        }   finally {
            this.connectionManager.closeConnection(connection);
        }
    }

    /**
     * returns one event by id
     *
     * @param id   id of event
     * @param user user which performs action
     * @return event
     */
    public Event findOne(int id, User user) throws DBException {
        Connection connection = this.connectionManager.getConnection();

        Event event;
        try {
            event = findOne(connection, id, user.getRole() != User.Role.USER);
        } finally {
            this.connectionManager.closeConnection(connection);
        }

        return event;
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
            logger.error("SQLException in findOne", e);
            throw new DBException("Event was not found", e);
        }

        return event;
    }

    /**
     * inserts event into the database
     *
     * @param event event that should be inserted
     */
    public void create(Event event) throws DBException, ValidationException {
        event.setStatistics(-1);
        event.setHidden(true);
        event.setLanguage(event.getModerator().getLanguage());
        event.validate();
        if (event.getModerator().getRole() != User.Role.MODERATOR) {
            throw new ValidationException("You have not permissions");
        }

        Connection connection = this.connectionManager.getConnection();
        try {
            eventDao.insert(connection, event);
        } catch (SQLException e) {
            logger.error("SQLException in create", e);
            throw new DBException("Unable to insert an event", e);
        } finally {
            this.connectionManager.closeConnection(connection);
        }
    }

    /**
     * joins the user to the event
     *
     * @param eventId id of event
     * @param user    user which should be joined
     */
    public void join(int eventId, User user) throws DBException, ValidationException {
        Connection connection = this.connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, false);
            canInteractWithEventValidation(event);
            if (event.getModerator().equals(user)) {
                throw new ValidationException("You are a moderator");
            }
            if (user.getRole() == User.Role.SPEAKER &&
                    event.getReports().stream().map(Report::getSpeaker).anyMatch(s -> s.equals(user))) {
                throw new ValidationException("You have a report");
            }
            if (event.getParticipants().contains(user)) {
                throw new ValidationException("You are already a participant");
            }

            userDao.insertParticipant(connection, event, user);
        } catch (SQLException e) {
            logger.error("SQLException in join", e);
            throw new DBException("Unable to join to the event", e);
        } finally {
            this.connectionManager.closeConnection(connection);
        }
    }

    /**
     * removes the user from the event
     *
     * @param eventId id of event
     * @param user    user which should be removed from event
     */
    public void leave(int eventId, User user) throws DBException, ValidationException {
        Connection connection = this.connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, false);
            canInteractWithEventValidation(event);
            if (!event.getParticipants().contains(user)) {
                throw new ValidationException("You are not a participant");
            }

            userDao.deleteParticipant(connection, event, user);
        } catch (SQLException e) {
            logger.error("SQLException in leave", e);
            throw new DBException("Unable to leave from the event", e);
        } finally {
            this.connectionManager.closeConnection(connection);
        }
    }

    /**
     * hides event from users with role 'user'
     *
     * @param eventId id of event
     * @param user    user which performs action
     */
    public void hide(int eventId, User user) throws DBException, ValidationException {
        Connection connection = this.connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, true);

            if (event.isHidden())
                throw new ValidationException("Event is already hidden");
            if (!event.getModerator().equals(user))
                throw new ValidationException("You have not permissions");

            event.setHidden(true);
            eventDao.update(connection, event);
        } catch (SQLException e) {
            logger.error("SQLException in hide", e);
            throw new DBException("Unable to hide the event");
        } finally {
            this.connectionManager.closeConnection(connection);
        }
    }

    /**
     * shows the event
     *
     * @param eventId id of event
     * @param user    user which action
     */
    public void show(int eventId, User user) throws DBException, ValidationException {
        Connection connection = this.connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, true);
            if (!event.isHidden()) {
                throw new ValidationException("Event is already shown");
            }
            if (!event.getModerator().equals(user)) {
                throw new ValidationException("You have not permissions");
            }

            event.setHidden(false);
            eventDao.update(connection, event);
        } catch (SQLException e) {
            logger.error("SQLException in show", e);
            throw new DBException("Unable to show the event", e);
        } finally {
            this.connectionManager.closeConnection(connection);
        }
    }

    /**
     * changes a title of event
     *
     * @param eventId  id of event
     * @param newTitle new title for event
     * @param user     user which performs action
     */
    public void modifyTitle(int eventId, String newTitle, User user) throws DBException, ValidationException {
        EventValidation.validateTitle(newTitle);
        Connection connection = this.connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, true);
            canInteractWithEventValidation(event);
            if (!event.getModerator().equals(user)) {
                throw new ValidationException("You have not permission");
            }

            String prevTitle = event.getTitle();
            event.setTitle(newTitle);
            eventDao.update(connection, event);
            // Send email notifications
            emailManager.sendTitleChanged(event, prevTitle);
        } catch (SQLException e) {
            logger.error("SQLException in modifyTitle", e);
            throw new DBException("Unable to modify a title", e);
        } finally {
            this.connectionManager.closeConnection(connection);
        }
    }

    /**
     * changes a description of event
     *
     * @param eventId        id of event
     * @param newDescription new description for event
     * @param user           user which performs action
     */
    public void modifyDescription(int eventId, String newDescription, User user) throws DBException, ValidationException {
        EventValidation.validateDescription(newDescription);
        Connection connection = this.connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, true);
            canInteractWithEventValidation(event);
            if (!event.getModerator().equals(user)) {
                throw new ValidationException("You have not permission");
            }

            event.setDescription(newDescription);
            eventDao.update(connection, event);
            emailManager.sendDescriptionChanged(event);
        } catch (SQLException e) {
            logger.error("SQLException in modifyDescription", e);
            throw new DBException("Unable to modify a description", e);
        } finally {
            this.connectionManager.closeConnection(connection);
        }
    }

    /**
     * changes a date of event
     *
     * @param eventId id of event
     * @param newDate new date for event
     * @param user    user which performs action
     */
    public void modifyDate(int eventId, Date newDate, User user) throws DBException, ValidationException {
        EventValidation.validateDate(newDate);
        Connection connection = this.connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, true);
            canInteractWithEventValidation(event);
            if (!event.getModerator().equals(user)) {
                throw new ValidationException("You have not permission");
            }

            Date prevDate = event.getDate();
            event.setDate(newDate);
            eventDao.update(connection, event);
            emailManager.sendDateChanged(event, prevDate);
        } catch (SQLException e) {
            logger.error("SQLException in modifyDate", e);
            throw new DBException("Unable to modify a date", e);
        } finally {
            this.connectionManager.closeConnection(connection);
        }
    }

    /**
     * changes place of event
     *
     * @param eventId  id of event
     * @param newPlace new place for event
     * @param user     user which performs action
     */
    public void modifyPlace(int eventId, String newPlace, User user) throws DBException, ValidationException {
        EventValidation.validatePlace(newPlace);
        Connection connection = this.connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, true);
            canInteractWithEventValidation(event);
            if (!event.getModerator().equals(user)) {
                throw new ValidationException("You have not permission");
            }

            String prevPlace = event.getPlace();
            event.setPlace(newPlace);
            eventDao.update(connection, event);
            emailManager.sendPlaceChanged(event, prevPlace);
        } catch (SQLException e) {
            logger.error("SQLException in modifyPlace", e);
            throw new DBException("Unable to modify a place", e);
        } finally {
            this.connectionManager.closeConnection(connection);
        }
    }

    /**
     * changes statistics of event
     *
     * @param eventId       id of event
     * @param newStatistics new statistics for event
     * @param user          user which performs action
     */
    public void modifyStatistics(int eventId, int newStatistics, User user) throws DBException, ValidationException {
        Connection connection = this.connectionManager.getConnection();
        try {
            Event event = findOne(connection, eventId, true);
            if (!event.getModerator().equals(user)) {
                throw new ValidationException("You have not permission");
            }
            if (event.getDate().compareTo(new Date()) > 0) {
                throw new ValidationException("Unable to modify statistics for a future event");
            }
            if (newStatistics < 0) {
                throw new ValidationException("Statistics should be greater than zero");
            }
            if (newStatistics > event.getParticipants().size()) {
                throw new ValidationException("Statistics should be lesser than a number of participants");
            }

            event.setStatistics(newStatistics);
            eventDao.update(connection, event);
        } catch (SQLException e) {
            logger.error("SQLException in modifyStatistics", e);
            throw new DBException("Unable to modify a statistics", e);
        } finally {
            this.connectionManager.closeConnection(connection);
        }
    }

    /**
     * @param event event
     * @throws ValidationException if date is past
     */
    static void canInteractWithEventValidation(Event event) throws ValidationException {
        try {
            EventValidation.validateDate(event.getDate());
        } catch (ValidationException e) {
            throw new ValidationException("You can???t interact with a past event", e);
        }
    }
}
