package com.my.conferences.service;

import com.my.conferences.dao.UserDao;
import com.my.conferences.dao.VerificationCodeDao;
import com.my.conferences.email.EmailManager;
import com.my.conferences.entity.User;
import com.my.conferences.entity.VerificationCode;
import com.my.conferences.exception.DBException;
import com.my.conferences.exception.ValidationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class VerificationCodeServiceTest {

    private static EmailManager emailManager;
    private static VerificationCodeService verificationCodeService;
    private static VerificationCode verificationCode;
    private static User user;

    @BeforeAll
    static void init() throws SQLException {
        emailManager = Mockito.mock(EmailManager.class);
        ConnectionManager connectionManager = Mockito.mock(ConnectionManager.class);
        VerificationCodeDao verificationCodeDao = Mockito.mock(VerificationCodeDao.class);
        UserDao userDao = Mockito.mock(UserDao.class);
        verificationCodeService = new VerificationCodeService(emailManager, connectionManager, verificationCodeDao, userDao);

        user = new User();
        user.setId(1);
        user.setEmail("test@test.com");
        Mockito.doReturn(user)
                .when(userDao)
                .findByEmail(ArgumentMatchers.any(), ArgumentMatchers.eq(user.getEmail()));

        verificationCode = new VerificationCode();
        verificationCode.setUser(user);
        verificationCode.setCode("123456");
        Mockito.doReturn(verificationCode)
                .when(verificationCodeDao)
                .findOne(ArgumentMatchers.any(), ArgumentMatchers.same(user));
    }

    @Test
    void sendCode() throws DBException, ValidationException {
        verificationCodeService.sendCode(user.getEmail(), "uk");
        assertEquals("uk", user.getLanguage());
        Mockito.verify(emailManager, Mockito.times(1))
                .sendVerificationCode(ArgumentMatchers.same(verificationCode));
    }

    @Test
    void verifyCode() throws DBException, ValidationException {
        boolean codeIsCorrect = verificationCodeService.verifyCode(user.getEmail(), verificationCode.getCode());
        assertTrue(codeIsCorrect);
    }

    @Test
    void verifyWrongCode() throws DBException, ValidationException {
        boolean codeIsCorrect = verificationCodeService.verifyCode(user.getEmail(), verificationCode.getCode() + "0");
        assertFalse(codeIsCorrect);
    }
}