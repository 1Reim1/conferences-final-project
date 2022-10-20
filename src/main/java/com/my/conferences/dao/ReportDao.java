package com.my.conferences.dao;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ReportDao {

    /**
     * returns reports of event
     *
     * @param event         event
     * @param onlyConfirmed boolean represents all events or only confirmed
     */
    void findAll(Connection connection, Event event, boolean onlyConfirmed) throws SQLException;

    /**
     * returns report with that id
     *
     * @param id if of report
     * @return report
     */
    Report findOne(Connection connection, int id) throws SQLException;

    /**
     * returns list of unconfirmed reports for moderator
     *
     * @param user moderator
     * @return list of reports
     */
    List<Report> findNewForModerator(Connection connection, User user) throws SQLException;

    /**
     * returns list of unconfirmed reports for speaker
     *
     * @param user speaker
     * @return list of reports
     */
    List<Report> findNewForSpeaker(Connection connection, User user) throws SQLException;

    /**
     * saves report to storage
     *
     * @param report report that should be saved
     */
    void insert(Connection connection, Report report) throws SQLException;

    /**
     * updates report in storage
     *
     * @param report report that should be updated
     */
    void update(Connection connection, Report report) throws SQLException;

    /**
     * deletes report from storage
     *
     * @param report report that should be deleted
     */
    void delete(Connection connection, Report report) throws SQLException;
}
