package com.my.conferences.util;

import com.my.conferences.dto.ReportWithEvent;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilTest {

    @Test
    void usersToJson() {
        List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setId(1);
        user1.setFirstName("Adam");
        user1.setLastName("James");
        user1.setEmail("adamjames@gmail.com");
        user1.setRole(User.Role.SPEAKER);
        users.add(user1);
        String json = JsonUtil.usersToJson(users);
        assertEquals("[{\"id\": 1,\"firstName\": \"Adam\",\"lastName\": \"James\",\"email\": \"adamjames@gmail.com\", \"role\": \"SPEAKER\"}]", json);

        User user2 = new User();
        user2.setId(2);
        user2.setFirstName("Toni");
        user2.setLastName("Bullock");
        user2.setEmail("tonibullock@gmail.com");
        user2.setRole(User.Role.MODERATOR);
        users.add(user2);
        json = JsonUtil.usersToJson(users);
        assertEquals("[{\"id\": 1,\"firstName\": \"Adam\",\"lastName\": \"James\",\"email\": \"adamjames@gmail.com\", \"role\": \"SPEAKER\"}," +
                        "{\"id\": 2,\"firstName\": \"Toni\",\"lastName\": \"Bullock\",\"email\": \"tonibullock@gmail.com\", \"role\": \"MODERATOR\"}]",
                json);
    }

    @Test
    void eventsToJson() {
        List<Event> events = new ArrayList<>();
        String json = JsonUtil.eventsToJson(events);
        assertEquals("[]", json);

        Event event1 = new Event();
        event1.setId(1);
        event1.setTitle("Event 1");
        event1.setDate(new Date(1666943116919L));
        events.add(event1);
        json = JsonUtil.eventsToJson(events);
        assertEquals("[{\"id\": 1,\"title\": \"Event 1\",\"date\": 1666943116919}]", json);

        Event event2 = new Event();
        event2.setId(1);
        event2.setTitle("Event 2");
        event2.setDate(new Date(1666943144444L));
        events.add(event2);
        json = JsonUtil.eventsToJson(events);
        assertEquals("[{\"id\": 1,\"title\": \"Event 1\",\"date\": 1666943116919}," +
                        "{\"id\": 1,\"title\": \"Event 2\",\"date\": 1666943144444}]",
                json);
    }

    @Test
    void reportsWithEventsToJson() {
        List<ReportWithEvent> reportsWithEvents = new ArrayList<>();
        String json = JsonUtil.reportsWithEventsToJson(reportsWithEvents);
        assertEquals("[]", json);

        Report report1 = new Report();
        report1.setId(1);
        report1.setTopic("Topic 1");
        Event event1 = new Event();
        event1.setId(10);
        event1.setTitle("Title 1");
        event1.setDate(new Date(1666943116919L));
        reportsWithEvents.add(new ReportWithEvent(report1, event1));
        json = JsonUtil.reportsWithEventsToJson(reportsWithEvents);
        assertEquals("[{\"id\": 1,\"event_id\": 10,\"topic\": \"Topic 1\",\"title\": \"Title 1\",\"date\": 1666943116919}]", json);

        Report report2 = new Report();
        report2.setId(2);
        report2.setTopic("Topic 2");
        Event event2 = new Event();
        event2.setId(20);
        event2.setTitle("Title 2");
        event2.setDate(new Date(1666943144444L));
        reportsWithEvents.add(new ReportWithEvent(report2, event2));
        json = JsonUtil.reportsWithEventsToJson(reportsWithEvents);
        assertEquals("[{\"id\": 1,\"event_id\": 10,\"topic\": \"Topic 1\",\"title\": \"Title 1\",\"date\": 1666943116919}," +
                        "{\"id\": 2,\"event_id\": 20,\"topic\": \"Topic 2\",\"title\": \"Title 2\",\"date\": 1666943144444}]",
                json);
    }
}