package com.my.conferences.dao.factory;

import com.my.conferences.dao.EventDao;
import com.my.conferences.dao.ReportDao;
import com.my.conferences.dao.UserDao;

public interface DaoFactory {
    EventDao getEventDao();

    ReportDao getReportDao();

    UserDao getUserDao();
}
