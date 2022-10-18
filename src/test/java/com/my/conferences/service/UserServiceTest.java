package com.my.conferences.service;

import com.my.conferences.dao.UserDao;
import com.my.conferences.entity.User;
import com.my.conferences.util.ConnectionUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private static UserService userService;
    private static UserDao userDao;
    private static MockedStatic<ConnectionUtil> connectionUtilMockedStatic;

    @BeforeAll
    static void init() throws SQLException {
        userDao = Mockito.mock(UserDao.class);
        userService = new UserService(userDao);

        User user = new User();
        user.setId(1);
        user.setEmail("emailtest@gmail.com");
        user.setPassword("ba3253876aed6bc22d4a6ff53d8406c6ad864195ed144ab5c87621b6c233b548baeae6956df346ec8c17f5ea10f35ee3cbc514797ed7ddd3145464e2a0bab413"); // 123456
        user.setFirstName("Adam");
        user.setLastName("James");
        user.setRole(User.Role.SPEAKER);
        user.setLanguage("en");

        Mockito.doReturn(user)
                .when(userDao)
                .findByEmail(ArgumentMatchers.any(), ArgumentMatchers.eq("emailtest@gmail.com"));

        Mockito.doThrow(new SQLException("no records"))
                .when(userDao)
                .findByEmail(ArgumentMatchers.any(), ArgumentMatchers.eq("emailtest1@gmail.com"));

        connectionUtilMockedStatic = Mockito.mockStatic(ConnectionUtil.class);
    }

    @Test
    void login() throws DBException, ValidationException {
        User user = userService.login("emailtest@gmail.com", "123456", "en");

        assertEquals(1, user.getId());
    }

    @Test
    void loginWrongPassword() {
        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> userService.login("emailtest@gmail.com", "1234567", "en"),
                "Expected that password is wrong");

        assertEquals("Password is wrong", thrown.getMessage());
    }

    @Test
    void loginWrongEmail() {
        DBException thrown = assertThrows(
                DBException.class,
                () -> userService.login("emailtest1@gmail.com", "123456", "en"),
                "Expected that user not found");

        assertEquals("User with this email not found", thrown.getMessage());
    }

    @Test
    void register() throws DBException, ValidationException, SQLException {
        User user = new User();
        user.setId(1);
        user.setEmail("emailtest1@gmail.com");
        user.setPassword("123456");
        user.setFirstName("Adam");
        user.setLastName("James");
        user.setRole(User.Role.SPEAKER);
        user.setLanguage("en");

        userService.register(user);
        assertNotEquals("123456", user.getPassword());
        Mockito.verify(userDao, Mockito.times(1)).insert(ArgumentMatchers.any(), ArgumentMatchers.same(user));
    }

    @Test
    void registerUserThatExists() throws SQLException {
        User user = new User();
        user.setId(1);
        user.setEmail("emailtest@gmail.com");
        user.setPassword("123456");
        user.setFirstName("Adam");
        user.setLastName("James");
        user.setRole(User.Role.SPEAKER);
        user.setLanguage("en");

        DBException thrown = assertThrows(
                DBException.class,
                () -> userService.register(user),
                "Expected user already exists");

        assertEquals("The user with this email already exists", thrown.getMessage());
        Mockito.verify(userDao, Mockito.times(0)).insert(ArgumentMatchers.any(), ArgumentMatchers.same(user));
    }

    @Test
    void registerModerator() throws SQLException {
        User user = new User();
        user.setRole(User.Role.MODERATOR);

        ValidationException thrown = assertThrows(
                ValidationException.class,
                () -> userService.register(user),
                "Expected exception");

        assertEquals("Moderator can not be register", thrown.getMessage());
        Mockito.verify(userDao, Mockito.times(0)).insert(ArgumentMatchers.any(), ArgumentMatchers.same(user));
    }

    @Test
    void setLanguage() throws DBException, SQLException {
        User user = new User();
        user.setLanguage("en");

        userService.setLanguage(user, "uk");
        assertEquals("uk", user.getLanguage());
        Mockito.verify(userDao, Mockito.times(1)).update(ArgumentMatchers.any(), ArgumentMatchers.same(user));
    }

    @Test
    void setLanguageWrong() throws DBException {
        User user = new User();
        user.setLanguage("uk");

        userService.setLanguage(user, "ru");
        assertEquals("en", user.getLanguage());
    }

    @AfterAll
    static void resetMockedStatic() {
        connectionUtilMockedStatic.close();
    }
}