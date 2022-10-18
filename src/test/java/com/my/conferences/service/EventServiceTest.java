package com.my.conferences.service;

import com.my.conferences.dao.EventDao;
import com.my.conferences.dao.ReportDao;
import com.my.conferences.dao.UserDao;
import com.my.conferences.email.EmailManager;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;
import com.my.conferences.util.ConnectionUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventServiceTest {

    private static EventService eventService;
    private static EmailManager emailManager;
    private static EventDao eventDao;
    private static UserDao userDao;
    private static MockedStatic<ConnectionUtil> connectionUtilMockedStatic;
    private static Event event;
    private static User moderator;
    private static User speaker;
    private static User participant;

    @BeforeAll
    static void init() throws SQLException {
        emailManager = Mockito.mock(EmailManager.class);
        eventDao = Mockito.mock(EventDao.class);
        ReportDao reportDao = Mockito.mock(ReportDao.class);
        userDao = Mockito.mock(UserDao.class);
        eventService = new EventService(emailManager, eventDao, reportDao, userDao, 2);

        Mockito.doReturn(new ArrayList<>())
                .when(eventDao)
                .findAll(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.anyBoolean(),
                        ArgumentMatchers.anyBoolean(),
                        ArgumentMatchers.anyInt(),
                        ArgumentMatchers.anyInt(),
                        ArgumentMatchers.any());

        Mockito.doReturn(new ArrayList<>())
                .when(eventDao)
                .findAllMy(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.anyBoolean(),
                        ArgumentMatchers.anyBoolean(),
                        ArgumentMatchers.anyInt(),
                        ArgumentMatchers.anyInt(),
                        ArgumentMatchers.any());

        moderator = new User();
        moderator.setId(1);
        moderator.setRole(User.Role.MODERATOR);
        moderator.setLanguage("en");

        speaker = new User();
        speaker.setId(2);
        speaker.setRole(User.Role.SPEAKER);

        participant = new User();
        participant.setId(3);
        participant.setRole(User.Role.USER);

        Report report = new Report();
        report.setSpeaker(speaker);

        event = new Event();
        event.setId(1);
        event.setTitle("Title");
        event.setDescription("Description 12345678");
        event.setPlace("Place");
        List<Report> reportList = new ArrayList<>();
        reportList.add(report);
        event.setReports(reportList);
        List<User> participantList = new ArrayList<>();
        participantList.add(participant);
        event.setParticipants(participantList);
        event.setModerator(moderator);
        event.setHidden(false);
        event.setStatistics(-1);

        Mockito.doReturn(event)
                .when(eventDao)
                .findOne(ArgumentMatchers.any(), ArgumentMatchers.eq(event.getId()), ArgumentMatchers.anyBoolean());

        connectionUtilMockedStatic = Mockito.mockStatic(ConnectionUtil.class);
    }

    @BeforeEach
    void tearUp() {
        Mockito.clearInvocations(eventDao);
        event.setDate(new Date(System.currentTimeMillis() + 10000)); // future date
    }

    @Test
    void findAll() throws DBException, SQLException {
        // test all events
        eventService.findAll(event.getId(), Event.Order.DATE, true, true, false, moderator);
        Mockito.verify(eventDao, Mockito.times(1))
                .findAll(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.anyBoolean(),
                        ArgumentMatchers.anyBoolean(),
                        ArgumentMatchers.anyInt(),
                        ArgumentMatchers.anyInt(),
                        ArgumentMatchers.any());
        // test only my events
        eventService.findAll(event.getId(), Event.Order.DATE, true, true, true, moderator);
        Mockito.verify(eventDao, Mockito.times(1))
                .findAllMy(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.anyBoolean(),
                        ArgumentMatchers.anyBoolean(),
                        ArgumentMatchers.anyInt(),
                        ArgumentMatchers.anyInt(),
                        ArgumentMatchers.any());
    }

    @Test
    void findOne() throws DBException, SQLException {
        Event event = eventService.findOne(EventServiceTest.event.getId(), moderator);
        assertEquals(EventServiceTest.event.getId(), event.getId());
        Mockito.verify(eventDao, Mockito.times(1))
                .findOne(ArgumentMatchers.any(), ArgumentMatchers.eq(1), ArgumentMatchers.eq(true));
    }

    @Test
    void countPages() throws SQLException, DBException {
        // test count of all events
        Mockito.doReturn(7)
                .when(eventDao)
                .findCount(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.anyString());
        int pages = eventService.countPages(true, false, moderator);
        assertEquals(4, pages);
        Mockito.verify(eventDao, Mockito.times(1))
                .findCount(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.anyString());
        // test count of my events
        Mockito.doReturn(4)
                .when(eventDao)
                .findCountMy(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.any());
        pages = eventService.countPages(true, true, moderator);
        assertEquals(2, pages);
        Mockito.verify(eventDao, Mockito.times(1))
                .findCountMy(ArgumentMatchers.any(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.any());
    }

    @Test
    void create() throws DBException, ValidationException, SQLException {
        Event event = new Event();
        event.setTitle("Title");
        event.setDescription("Description 12345678");
        event.setDate(new Date(System.currentTimeMillis() + 10000)); // future date
        event.setPlace("Place");
        event.setModerator(moderator);
        event.setHidden(false);

        eventService.create(event);
        assertTrue(event.isHidden());
        assertEquals(-1, event.getStatistics());
        assertEquals(moderator.getLanguage(), event.getLanguage());
        Mockito.verify(eventDao, Mockito.times(1))
                .insert(ArgumentMatchers.any(), ArgumentMatchers.same(event));
    }

    @Test
    void join() throws DBException, ValidationException, SQLException {
        User user = new User();
        user.setId(10);
        eventService.join(event.getId(), user);
        Mockito.verify(userDao, Mockito.times(1))
                .insertParticipant(ArgumentMatchers.any(), ArgumentMatchers.same(event), ArgumentMatchers.same(user));
        // test moderator
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> eventService.join(event.getId(), moderator),
                "Expected exception");
        assertEquals("You are a moderator", thrown.getMessage());
        // test speaker
        thrown = assertThrows(
                ValidationException.class,
                () -> eventService.join(event.getId(), speaker),
                "Expected exception");
        assertEquals("You have a report", thrown.getMessage());
        // test participant
        thrown = assertThrows(
                ValidationException.class,
                () -> eventService.join(event.getId(), participant),
                "Expected exception");
        assertEquals("You are already a participant", thrown.getMessage());
    }

    @Test
    void leave() throws DBException, ValidationException, SQLException {
        eventService.leave(1, participant);
        Mockito.verify(userDao, Mockito.times(1))
                .deleteParticipant(ArgumentMatchers.any(), ArgumentMatchers.same(event), ArgumentMatchers.same(participant));
        // test leave user which is not a participant
        User user = new User();
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> eventService.leave(1, user),
                "Expected exception");
        assertEquals("You are not a participant", thrown.getMessage());
    }

    @Test
    void hide() throws DBException, ValidationException, SQLException {
        eventService.hide(event.getId(), moderator);
        assertTrue(event.isHidden());
        Mockito.verify(eventDao, Mockito.times(1))
                .update(ArgumentMatchers.any(), ArgumentMatchers.same(event));
        // test hide an event that is already hidden
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> eventService.hide(event.getId(), moderator),
                "Expected exception");
        assertEquals("Event is already hidden", thrown.getMessage());
        // test hide event by user which is not a moderator
        event.setHidden(false);
        thrown = assertThrows(
                ValidationException.class,
                () -> eventService.hide(event.getId(), speaker),
                "Expected exception");
        assertEquals("You have not permissions", thrown.getMessage());
    }

    @Test
    void show() throws DBException, ValidationException, SQLException {
        event.setHidden(true);
        eventService.show(event.getId(), moderator);
        assertFalse(event.isHidden());
        Mockito.verify(eventDao, Mockito.times(1))
                .update(ArgumentMatchers.any(), ArgumentMatchers.same(event));
        // test show an event that is not hidden
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> eventService.show(event.getId(), moderator),
                "Expected exception");
        assertEquals("Event is already shown", thrown.getMessage());
        // test show an event that is not moderator
        event.setHidden(true);
        thrown = assertThrows(
                ValidationException.class,
                () -> eventService.show(event.getId(), speaker),
                "Expected exception");
        assertEquals("You have not permissions", thrown.getMessage());
    }

    @Test
    void modifyTitle() throws DBException, ValidationException, SQLException {
        String newTitle = "New title";
        eventService.modifyTitle(event.getId(), newTitle, moderator);
        assertEquals(newTitle, event.getTitle());
        Mockito.verify(eventDao, Mockito.times(1))
                .update(ArgumentMatchers.any(), ArgumentMatchers.same(event));
        Mockito.verify(emailManager, Mockito.times(1))
                .sendTitleChanged(ArgumentMatchers.same(event), ArgumentMatchers.anyString());
    }

    @Test
    void modifyDescription() throws DBException, ValidationException, SQLException {
        String newDescription = "New descritonasdhjhdghjdg";
        eventService.modifyDescription(event.getId(), newDescription, moderator);
        assertEquals(newDescription, event.getDescription());
        Mockito.verify(eventDao, Mockito.times(1))
                .update(ArgumentMatchers.any(), ArgumentMatchers.same(event));
        Mockito.verify(emailManager, Mockito.times(1))
                .sendDescriptionChanged(ArgumentMatchers.same(event));
    }

    @Test
    void modifyDate() throws DBException, ValidationException, SQLException {
        Date newDate = new Date(System.currentTimeMillis() + 20000);
        eventService.modifyDate(event.getId(), newDate, moderator);
        assertEquals(newDate, event.getDate());
        Mockito.verify(eventDao, Mockito.times(1))
                .update(ArgumentMatchers.any(), ArgumentMatchers.same(event));
        Mockito.verify(emailManager, Mockito.times(1))
                .sendDateChanged(ArgumentMatchers.same(event), ArgumentMatchers.any());
        // test set a past date
        Date pastDate = new Date(System.currentTimeMillis() - 1000);
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> eventService.modifyDate(event.getId(), pastDate, moderator),
                "Expected exception");
        assertEquals("Required future date", thrown.getMessage());
    }

    @Test
    void modifyPlace() throws DBException, ValidationException, SQLException {
        String newPlace = "New place";
        eventService.modifyPlace(event.getId(), newPlace, moderator);
        assertEquals(newPlace, event.getPlace());
        Mockito.verify(eventDao, Mockito.times(1))
                .update(ArgumentMatchers.any(), ArgumentMatchers.same(event));
        Mockito.verify(emailManager, Mockito.times(1))
                .sendPlaceChanged(ArgumentMatchers.same(event), ArgumentMatchers.anyString());
    }

    @Test
    void modifyStatistics() throws DBException, ValidationException, SQLException {
        int newStatistics = 1;
        event.setDate(new Date(System.currentTimeMillis() - 1000));
        eventService.modifyStatistics(event.getId(), newStatistics, moderator);
        assertEquals(newStatistics, event.getStatistics());
        Mockito.verify(eventDao, Mockito.times(1))
                .update(ArgumentMatchers.any(), ArgumentMatchers.same(event));
        // test statistics greater than a number of participants
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> eventService.modifyStatistics(event.getId(), event.getParticipants().size() + 1, moderator),
                "Expected exception");
        assertEquals("Statistics should be lesser than a number of participants", thrown.getMessage());
        // test statistics < 0
        thrown = assertThrows(
                ValidationException.class,
                () -> eventService.modifyStatistics(event.getId(), -1, moderator),
                "Expected exception");
        assertEquals("Statistics should be greater than zero", thrown.getMessage());
        // test for future event
        event.setDate(new Date(System.currentTimeMillis() + 10000));
        thrown = assertThrows(
                ValidationException.class,
                () -> eventService.modifyStatistics(event.getId(), newStatistics, moderator),
                "Expected exception");
        assertEquals("Unable to modify statistics for a future event", thrown.getMessage());
    }

    @Test
    void canInteractWithEventValidation() {
        event.setDate(new Date(System.currentTimeMillis() - 1000));
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> EventService.canInteractWithEventValidation(event),
                "Expected exception");
        assertEquals("You can’t interact with a past event", thrown.getMessage());
    }

    @AfterAll
    static void resetMockedStatic() {
        connectionUtilMockedStatic.close();
    }
}