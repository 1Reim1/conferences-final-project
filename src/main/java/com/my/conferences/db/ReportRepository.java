package com.my.conferences.db;

import com.my.conferences.entity.Event;

import java.sql.Connection;
import java.util.List;

public class ReportRepository {
    private static ReportRepository instance;

    public static synchronized ReportRepository getInstance() {
        if (instance == null) {
            instance = new ReportRepository();
        }

        return instance;
    }

    private ReportRepository() {

    }

    private void findAll(Event event) {

    }

    public void findAll(Connection connection, List<Event> events) {
        for (Event event : events)
            findAll(event);
    }
}
