package com.my.conferences.dto;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;

public class ReportWithEvent {
    private final Report report;
    private final Event event;

    public ReportWithEvent(Report report, Event event) {
        this.report = report;
        this.event = event;
    }

    public Report getReport() {
        return report;
    }

    public Event getEvent() {
        return event;
    }
}
