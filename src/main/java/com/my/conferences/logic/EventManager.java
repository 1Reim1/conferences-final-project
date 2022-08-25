package com.my.conferences.logic;

import com.my.conferences.db.*;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;

import java.sql.Connection;
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
        List<Event> events = eventRepository.findAll(connection);
        reportRepository.findAll(connection, events);
        for (Event event: events) {
            userRepository.findAll(connection, event.getParticipants());
            for (Report report : event.getReports())
                userRepository.find(connection, report.getSpeaker());
        }

        return events;
    }
}
