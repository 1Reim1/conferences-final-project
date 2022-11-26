package com.my.conferences.service;

import com.my.conferences.dao.EventDao;
import com.my.conferences.dao.ReportDao;
import com.my.conferences.dao.UserDao;
import com.my.conferences.email.EmailManager;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;
import com.my.conferences.exception.DBException;
import com.my.conferences.exception.ValidationException;
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

class ReportServiceTest {

    private static ReportService reportService;
    private static EmailManager emailManager;
    private static ReportDao reportDao;
    private static MockedStatic<ConnectionUtil> connectionUtilMockedStatic;
    private static Report report;
    private static Event event;
    private static User moderator;
    private static User speaker;

    @BeforeAll
    static void init() throws SQLException {
        emailManager = Mockito.mock(EmailManager.class);
        EventDao eventDao = Mockito.mock(EventDao.class);
        reportDao = Mockito.mock(ReportDao.class);
        UserDao userDao = Mockito.mock(UserDao.class);
        reportService = new ReportService(emailManager, eventDao, reportDao, userDao);

        Mockito.doReturn(new ArrayList<>())
                .when(reportDao)
                .findNewForModerator(ArgumentMatchers.any(), ArgumentMatchers.any());
        Mockito.doReturn(new ArrayList<>())
                .when(reportDao)
                .findNewForSpeaker(ArgumentMatchers.any(), ArgumentMatchers.any());

        moderator = new User();
        moderator.setId(1);
        moderator.setRole(User.Role.MODERATOR);

        speaker = new User();
        speaker.setId(2);
        speaker.setRole(User.Role.SPEAKER);

        report = new Report();
        report.setId(1);
        report.setTopic("Topic");
        report.setConfirmed(true);
        report.setSpeaker(speaker);
        report.setEventId(1);

        event = new Event();
        event.setId(1);
        event.setDate(new Date(System.currentTimeMillis() + 10000)); // future date
        event.setModerator(moderator);
        event.setReports(new ArrayList<>());

        Mockito.doReturn(report)
                .when(reportDao)
                .findOne(ArgumentMatchers.any(), ArgumentMatchers.eq(report.getId()));
        Mockito.doReturn(event)
                .when(eventDao)
                .findOne(ArgumentMatchers.any(), ArgumentMatchers.eq(report.getEventId()), ArgumentMatchers.eq(true));
        Mockito.doAnswer(invocation -> {
            User user = invocation.getArgument(1, User.class);
            if (user != null && user.getId() == 2) {
                user.setRole(User.Role.SPEAKER);
            }
            return null;
        }).when(userDao).findOne(ArgumentMatchers.any(), ArgumentMatchers.any());


        connectionUtilMockedStatic = Mockito.mockStatic(ConnectionUtil.class);
    }

    @BeforeEach
    void tearUp() {
        Mockito.clearInvocations(reportDao);
    }

    @Test
    void findNewReports() throws DBException, ValidationException, SQLException {
        User user = new User();
        user.setRole(User.Role.SPEAKER);
        reportService.findNewReports(user);
        Mockito.verify(reportDao, Mockito.times(1))
                .findNewForSpeaker(ArgumentMatchers.any(), ArgumentMatchers.same(user));

        user = new User();
        user.setRole(User.Role.MODERATOR);
        reportService.findNewReports(user);
        Mockito.verify(reportDao, Mockito.times(1))
                .findNewForModerator(ArgumentMatchers.any(), ArgumentMatchers.same(user));
    }

    @Test
    void findAllFutureSpeakerReports() throws DBException, ValidationException, SQLException {
        reportService.findAllFutureSpeakerReports(2);
        Mockito.verify(reportDao)
                .findAllBySpeaker(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.eq(true));
    }

    @Test
    void cancelReport() throws SQLException, DBException, ValidationException {
        // Test cancel report by moderator
        report.setConfirmed(true);
        reportService.cancelReport(report.getId(), moderator);
        Mockito.verify(reportDao, Mockito.times(1))
                .delete(ArgumentMatchers.any(), ArgumentMatchers.same(report));
        Mockito.verify(emailManager, Mockito.times(1))
                .sendConfirmedReportCancelled(ArgumentMatchers.same(report), ArgumentMatchers.same(event));
        // Test cancel unconfirmed report by moderator
        report.setConfirmed(false);
        reportService.cancelReport(report.getId(), moderator);
        Mockito.verify(reportDao, Mockito.times(2))
                .delete(ArgumentMatchers.any(), ArgumentMatchers.same(report));
        Mockito.verify(emailManager, Mockito.times(1))
                .sendReportCancelledByModerator(ArgumentMatchers.same(report), ArgumentMatchers.same(event));
        // Test cancel unconfirmed report by speaker
        reportService.cancelReport(report.getId(), speaker);
        Mockito.verify(reportDao, Mockito.times(3))
                .delete(ArgumentMatchers.any(), ArgumentMatchers.same(report));
        Mockito.verify(emailManager, Mockito.times(1))
                .sendReportCancelledBySpeaker(ArgumentMatchers.same(report), ArgumentMatchers.same(event));
        // Test cancel report by user
        User user = new User();
        user.setRole(User.Role.USER);
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> reportService.cancelReport(report.getId(), user),
                "Expected exception");
        assertEquals("You have not permissions", thrown.getMessage());
    }

    @Test
    void confirmReport() throws SQLException, DBException, ValidationException {
        // test confirm by moderator
        report.setConfirmed(false);
        report.setCreator(speaker);
        reportService.confirmReport(report.getId(), moderator);
        assertTrue(report.isConfirmed());
        Mockito.verify(reportDao, Mockito.times(1))
                .update(ArgumentMatchers.any(), ArgumentMatchers.same(report));
        Mockito.verify(emailManager, Mockito.times(1))
                .sendReportConfirmedByModerator(ArgumentMatchers.same(report), ArgumentMatchers.same(event));
        Mockito.verify(emailManager, Mockito.times(1))
                .sendAddedNewReport(ArgumentMatchers.same(report), ArgumentMatchers.same(event));
        // test confirm by speaker
        report.setConfirmed(false);
        report.setCreator(moderator);
        reportService.confirmReport(report.getId(), speaker);
        assertTrue(report.isConfirmed());
        Mockito.verify(reportDao, Mockito.times(2))
                .update(ArgumentMatchers.any(), ArgumentMatchers.same(report));
        Mockito.verify(emailManager, Mockito.times(1))
                .sendReportConfirmedBySpeaker(ArgumentMatchers.same(report), ArgumentMatchers.same(event));
        Mockito.verify(emailManager, Mockito.times(2))
                .sendAddedNewReport(ArgumentMatchers.same(report), ArgumentMatchers.same(event));
        // test confirm by moderator (moderator created report)
        report.setConfirmed(false);
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> reportService.confirmReport(report.getId(), moderator),
                "Expected exception");
        assertEquals("You have not permissions", thrown.getMessage());
        // test confirm by speaker (speaker created report)
        report.setCreator(speaker);
        thrown = assertThrows(
                ValidationException.class,
                () -> reportService.confirmReport(report.getId(), speaker),
                "Expected exception");
        assertEquals("You have not permissions", thrown.getMessage());
        // test confirm report that is already confirmed
        report.setConfirmed(true);
        thrown = assertThrows(
                ValidationException.class,
                () -> reportService.confirmReport(report.getId(), moderator),
                "Expected exception");
        assertEquals("Report is already confirmed", thrown.getMessage());
    }

    @Test
    void offerReport() throws SQLException, DBException, ValidationException {
        User speaker2 = new User();
        speaker2.setRole(User.Role.SPEAKER);
        List<User> participants = new ArrayList<>();
        participants.add(speaker2);
        event.setParticipants(participants);
        // test offer report by moderator
        report.setCreator(moderator);
        reportService.offerReport(report);
        assertFalse(report.isConfirmed());
        Mockito.verify(reportDao, Mockito.times(1))
                .insert(ArgumentMatchers.any(), ArgumentMatchers.same(report));
        Mockito.verify(emailManager, Mockito.times(1))
                .sendReportOfferedByModerator(ArgumentMatchers.same(report), ArgumentMatchers.same(event));
        // test offer report by speaker
        report.setCreator(speaker);
        reportService.offerReport(report);
        assertFalse(report.isConfirmed());
        Mockito.verify(reportDao, Mockito.times(2))
                .insert(ArgumentMatchers.any(), ArgumentMatchers.same(report));
        Mockito.verify(emailManager, Mockito.times(1))
                .sendReportOfferedBySpeaker(ArgumentMatchers.same(report), ArgumentMatchers.same(event));
        // test offer by speaker which is already participant
        report.setCreator(speaker2);
        report.setSpeaker(speaker2);
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> reportService.offerReport(report),
                "Expected exception");
        assertEquals("Speaker is already a participant", thrown.getMessage());
        // test offer speaker to another speaker
        event.getParticipants().clear();
        report.setSpeaker(speaker);
        thrown = assertThrows(
                ValidationException.class,
                () -> reportService.offerReport(report),
                "Expected exception");
        assertEquals("Speaker can not offer a report to someone", thrown.getMessage());
        // test offer to user (not speaker)
        User user = new User();
        user.setRole(User.Role.USER);
        report.setSpeaker(user);
        report.setCreator(moderator);
        thrown = assertThrows(
                ValidationException.class,
                () -> reportService.offerReport(report),
                "Expected exception");
        assertEquals("User is not a speaker", thrown.getMessage());
        // test offer by user
        report.setSpeaker(speaker);
        report.setCreator(user);
        thrown = assertThrows(
                ValidationException.class,
                () -> reportService.offerReport(report),
                "Expected exception");
        assertEquals("You have not permission", thrown.getMessage());
    }

    @Test
    void modifyReportTopic() throws SQLException, DBException, ValidationException {
        // test confirmed report
        report.setConfirmed(true);
        String newTopic = "new topic";
        reportService.modifyReportTopic(report.getId(), newTopic, moderator);
        assertEquals(newTopic, report.getTopic());
        Mockito.verify(reportDao, Mockito.times(1))
                .update(ArgumentMatchers.any(), ArgumentMatchers.same(report));
        Mockito.verify(emailManager, Mockito.times(1))
                .sendConfirmedReportTopicChanged(ArgumentMatchers.same(report), ArgumentMatchers.same(event), ArgumentMatchers.any());
        // test unconfirmed report
        newTopic = "new topic 2";
        report.setConfirmed(false);
        reportService.modifyReportTopic(report.getId(), newTopic, moderator);
        assertEquals(newTopic, report.getTopic());
        Mockito.verify(reportDao, Mockito.times(2))
                .update(ArgumentMatchers.any(), ArgumentMatchers.same(report));
        Mockito.verify(emailManager, Mockito.times(1))
                .sendReportTopicChanged(ArgumentMatchers.same(report), ArgumentMatchers.same(event), ArgumentMatchers.any());
        // test by moderator (not of this event)
        User moderator2 = new User();
        moderator2.setRole(User.Role.MODERATOR);
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> reportService.modifyReportTopic(report.getId(), "new topic", moderator2),
                "Expected exception");
        assertEquals("You have not permissions", thrown.getMessage());
    }

    @AfterAll
    static void resetMockedStatic() {
        connectionUtilMockedStatic.close();
    }
}