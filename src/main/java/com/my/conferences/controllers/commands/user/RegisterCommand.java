package com.my.conferences.controllers.commands.user;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.entity.User;
import com.my.conferences.exception.DBException;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.service.UserService;
import com.my.conferences.util.RequestUtil;
import com.my.conferences.validation.RecaptchaValidation;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;

public class RegisterCommand implements Command {

    private static final String EXCEPTION_MESSAGE = "Exception in RegisterCommand";
    private static final Logger logger = Logger.getLogger(RegisterCommand.class);
    private final UserService userService;
    private final RecaptchaValidation recaptchaValidation;

    public RegisterCommand(UserService userService, RecaptchaValidation recaptchaValidation) {
        this.userService = userService;
        this.recaptchaValidation = recaptchaValidation;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            User user = new User();
            user.setEmail(RequestUtil.getStringParameter(request, "email"));
            user.setFirstName(RequestUtil.getStringParameter(request, "first_name"));
            user.setLastName(RequestUtil.getStringParameter(request, "last_name"));
            user.setPassword(RequestUtil.getStringParameter(request, "password"));
            String role = RequestUtil.getStringParameter(request, "role").toUpperCase();
            String gRecaptchaResponse = RequestUtil.getStringParameter(request, "g_recaptcha_response");
            if (!role.equals(User.Role.USER.toString()) && !role.equals(User.Role.SPEAKER.toString())) {
                role = User.Role.USER.toString();
            }
            user.setRole(User.Role.valueOf(role));
            user.setLanguage(RequestUtil.getCookiesMap(request).get("lang"));

            logger.trace("Email: " + user.getEmail());
            logger.trace("First name: " + user.getFirstName());
            logger.trace("Last name: " + user.getLastName());
            logger.trace("Role: " + role);
            logger.trace("Language: " + user.getLanguage());
            logger.trace("gRecaptchaResponse: " + gRecaptchaResponse);
            if (!recaptchaValidation.verify(gRecaptchaResponse)) {
                throw new ValidationException("Captcha is wrong");
            }

            userService.register(user);
            request.getSession().setAttribute("user", user);
        } catch (ValidationException e) {
            logger.error(EXCEPTION_MESSAGE, e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
        } catch (DBException e) {
            logger.error(EXCEPTION_MESSAGE, e);
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println(e.getMessage());
        }
    }
}
