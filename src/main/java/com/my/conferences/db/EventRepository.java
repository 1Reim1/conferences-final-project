package com.my.conferences.db;

import com.my.conferences.entity.Event;

import java.sql.Connection;
import java.util.List;

public class EventRepository {

    private static EventRepository instance;

    public static synchronized EventRepository getInstance() {
        if (instance == null) {
            instance = new EventRepository();
        }

        return instance;
    }

    private EventRepository() {

    }

    public List<Event> findAll(Connection connection) {
        return null;
    }
}
