package com.my.conferences.util;

import com.my.conferences.dto.ReportWithEvent;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;

import java.util.List;

public class JsonUtil {

    private JsonUtil() {}

    public static String usersToJson(List<User> users) {
        StringBuilder jsonResponse = new StringBuilder("[");
        for (User user : users) {
            jsonResponse.append("{")
                    .append("\"id\": ")
                    .append(user.getId())
                    .append(",\"firstName\": \"")
                    .append(user.getFirstName())
                    .append("\",\"lastName\": \"")
                    .append(user.getLastName())
                    .append("\",\"email\": \"")
                    .append(user.getEmail())
                    .append("\", \"role\": \"")
                    .append(user.getRole())
                    .append("\"},");
        }

        jsonResponse.deleteCharAt(jsonResponse.length() - 1).append("]");
        return jsonResponse.toString();
    }

    public static String eventsToJson(List<Event> events) {
        if (events.isEmpty()) {
            return "[]";
        }

        StringBuilder jsonResponse = new StringBuilder("[");
        for (Event event : events) {
            jsonResponse.append("{")
                    .append("\"id\": ")
                    .append(event.getId())
                    .append(",\"title\": \"")
                    .append(event.getTitle())
                    .append("\",\"date\": ")
                    .append(event.getDate().getTime())
                    .append("},");
        }

        jsonResponse.deleteCharAt(jsonResponse.length() - 1).append("]");
        return jsonResponse.toString();
    }

    public static String reportsWithEventsToJson(List<ReportWithEvent> reportsWithEvents) {
        if (reportsWithEvents.isEmpty()) {
            return "[]";
        }

        StringBuilder jsonResponse = new StringBuilder("[");
        for (ReportWithEvent reportWithEvent : reportsWithEvents) {
            jsonResponse.append("{")
                    .append("\"id\": ")
                    .append(reportWithEvent.getReport().getId())
                    .append(",\"event_id\": ")
                    .append(reportWithEvent.getEvent().getId())
                    .append(",\"topic\": \"")
                    .append(reportWithEvent.getReport().getTopic())
                    .append("\",\"title\": \"")
                    .append(reportWithEvent.getEvent().getTitle())
                    .append("\",\"date\": ")
                    .append(reportWithEvent.getEvent().getDate().getTime())
                    .append("},");
        }

        return jsonResponse.deleteCharAt(jsonResponse.length() - 1).append("]").toString();
    }
}
