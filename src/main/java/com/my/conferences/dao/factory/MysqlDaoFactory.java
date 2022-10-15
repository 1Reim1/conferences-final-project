package com.my.conferences.dao.factory;

import com.my.conferences.dao.EventDao;
import com.my.conferences.dao.ReportDao;
import com.my.conferences.dao.UserDao;
import com.my.conferences.dao.implementation.mysql.MysqlEventDaoImpl;
import com.my.conferences.dao.implementation.mysql.MysqlReportDaoImpl;
import com.my.conferences.dao.implementation.mysql.MysqlUserDaoImpl;

public class MysqlDaoFactory implements DaoFactory {

    @Override
    public EventDao getEventDao() {
        return new MysqlEventDaoImpl();
    }

    @Override
    public ReportDao getReportDao() {
        return new MysqlReportDaoImpl();
    }

    @Override
    public UserDao getUserDao() {
        return new MysqlUserDaoImpl();
    }
}
