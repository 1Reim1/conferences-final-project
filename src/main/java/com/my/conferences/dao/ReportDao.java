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
     * @param connection    db connection
     * @param event         event
     * @param onlyConfirmed boolean represents all events or only confirmed
     */
    void findAll(Connection connection, Event event, boolean onlyConfirmed) throws SQLException;

    /**
     * returns report with that id
     *
     * @param connection db connection
     * @param id         if of report
     * @return report
     */
    Report findOne(Connection connection, int id) throws SQLException;

    /**
     * returns list of unconfirmed reports for moderator
     *
     * @param connection db connection
     * @param user       moderator
     * @return list of reports
     */
    List<Report> findNewForModerator(Connection connection, User user) throws SQLException;

    /**
     * returns list of unconfirmed reports for speaker
     *
     * @param connection db connection
     * @param user       speaker
     * @return list of reports
     */
    List<Report> findNewForSpeaker(Connection connection, User user) throws SQLException;

    /**
     * returns all reports by speaker
     *
     * @param connection    db connection
     * @param speaker       speaker
     * @param futureReports future or past reports
     * @return list of reports by speaker
     */
    List<Report> findAllBySpeaker(Connection connection, User speaker, boolean futureReports) throws SQLException;

    /**
     * saves report to database
     *
     * @param connection db connection
     * @param report     report that should be saved
     */
    void insert(Connection connection, Report report) throws SQLException;

    /**
     * updates report in database
     *
     * @param connection db connection
     * @param report     report that should be updated
     */
    void update(Connection connection, Report report) throws SQLException;

    /**
     * deletes report from database
     *
     * @param connection db connection
     * @param report     report that should be deleted
     */
    void delete(Connection connection, Report report) throws SQLException;
}
