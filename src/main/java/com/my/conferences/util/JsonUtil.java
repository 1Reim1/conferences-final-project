package com.my.conferences.util;

import com.my.conferences.dto.ReportWithEvent;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;

import java.util.List;

public class JsonUtil {

    private JsonUtil() {}

    public static String usersToJson(List<User> users) {
        JsonArrayBuilder json = Json.createArrayBuilder();
        users.forEach(user -> json.add(
                Json.createObjectBuilder()
                        .add("id", user.getId())
                        .add("firstName", user.getFirstName())
                        .add("lastName", user.getLastName())
                        .add("email", user.getEmail())
                        .add("role", user.getRole().toString())
                        .build()
        ));

        return json.build().toString();
    }

    public static String eventsToJson(List<Event> events) {
        JsonArrayBuilder json = Json.createArrayBuilder();
        events.forEach(event -> json.add(
                Json.createObjectBuilder()
                        .add("id", event.getId())
                        .add("title", event.getTitle())
                        .add("date", event.getDate().getTime())
                        .build()
        ));

        return json.build().toString();
    }

    public static String reportsWithEventsToJson(List<ReportWithEvent> reportsWithEvents) {
        JsonArrayBuilder json = Json.createArrayBuilder();
        reportsWithEvents.forEach(reportWithEvent -> json.add(
                Json.createObjectBuilder()
                        .add("id", reportWithEvent.getReport().getId())
                        .add("event_id", reportWithEvent.getEvent().getId())
                        .add("topic", reportWithEvent.getReport().getTopic())
                        .add("title", reportWithEvent.getEvent().getTitle())
                        .add("date", reportWithEvent.getEvent().getDate().getTime())
                        .build()
        ));

        return json.build().toString();
    }
}
