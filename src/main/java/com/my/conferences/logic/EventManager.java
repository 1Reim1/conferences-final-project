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

    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }

        return instance;
    }

    private EventManager() {

    }

    public List<Event> findAll() throws DBException {
        Connection connection = connectionManager.getConnection();
        List<Event> events;
        try {
            events = eventRepository.findAll(connection);
            for (Event event : events) {
                reportRepository.findAllByEvent(connection, event);
                userRepository.findAllParticipants(connection, event);
            }
        } catch (SQLException e) {
            throw new DBException("events was not loaded", e);
        }

//        reportRepository.findAll(connection, events);
//        for (Event event: events) {
//            userRepository.findAll(connection, event.getParticipants());
//            for (Report report : event.getReports())
//                userRepository.find(connection, report.getSpeaker());
//        }

        connectionManager.closeConnection(connection);
        return events;
    }
}
