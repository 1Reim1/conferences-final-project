package com.my.conferences.logic;

import com.my.conferences.db.*;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;

import java.sql.Connection;
import java.sql.SQLException;
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

    public List<Event> findAll(int page) throws DBException {
        Connection connection = connectionManager.getConnection();
        List<Event> events;
        try {
            events = eventRepository.findAll(connection, PAGE_SIZE, page);
            for (Event event : events) {
                reportRepository.findAllByEvent(connection, event);
                userRepository.findAllParticipants(connection, event);
            }
        } catch (SQLException e) {
            throw new DBException("events was not loaded", e);
        }   finally {
            connectionManager.closeConnection(connection);
        }

        return events;
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
