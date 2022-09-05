package com.my.conferences.logic;

import com.my.conferences.db.*;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;

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

    public Event findOne(int id, boolean showHidden) throws DBException {
        Connection connection = connectionManager.getConnection();

        Event event;
        try {
            event = eventRepository.findOne(connection, id, showHidden);
            userRepository.findAllParticipants(connection, event);
            reportRepository.findAll(connection, event, !showHidden);
            for (Report report : event.getReports()) {
                userRepository.findOne(connection, report.getCreator());
                userRepository.findOne(connection, report.getSpeaker());
            }
        } catch (SQLException e) {
            throw new DBException("Event was not found", e);
        }   finally {
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
}
