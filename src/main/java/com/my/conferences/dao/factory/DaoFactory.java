package com.my.conferences.dao.factory;

import com.my.conferences.dao.EventDao;
import com.my.conferences.dao.ReportDao;
import com.my.conferences.dao.UserDao;
import com.my.conferences.dao.VerificationCodeDao;

public interface DaoFactory {
    EventDao getEventDao();

    ReportDao getReportDao();

    UserDao getUserDao();

    VerificationCodeDao getVerificationCodeDao();
}
