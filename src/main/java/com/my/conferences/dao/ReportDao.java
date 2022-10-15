package com.my.conferences.dao;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ReportDao {
    void findAll(Connection connection, Event event, boolean onlyConfirmed) throws SQLException;

    Report findOne(Connection connection, int id) throws SQLException;

    List<Report> findNewForModerator(Connection connection, User user) throws SQLException;

    List<Report> findNewForSpeaker(Connection connection, User user) throws SQLException;

    void delete(Connection connection, Report report) throws SQLException;

    void update(Connection connection, Report report) throws SQLException;

    void insert(Connection connection, Report report) throws SQLException;
}
