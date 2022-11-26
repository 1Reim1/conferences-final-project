package com.my.conferences.controllers.commands.user;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.entity.User;
import com.my.conferences.exception.DBException;
import com.my.conferences.exception.ValidationException;
import com.my.conferences.service.UserService;
import com.my.conferences.util.RequestUtil;
import com.my.conferences.validation.RecaptchaValidation;
import com.my.conferences.validation.UserValidation;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;

public class LoginCommand implements Command {

    private final static String EXCEPTION_MESSAGE = "Exception in LoginCommand";
    private final static Logger logger = Logger.getLogger(LoginCommand.class);
    private final UserService userService;
    private final RecaptchaValidation recaptchaValidation;

    public LoginCommand(UserService userService, RecaptchaValidation recaptchaValidation) {
        this.userService = userService;
        this.recaptchaValidation = recaptchaValidation;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String email = RequestUtil.getStringParameter(request, "email");
            String password = RequestUtil.getStringParameter(request, "password");
            String gRecaptchaResponse = RequestUtil.getStringParameter(request, "g_recaptcha_response");

            Map<String, String> cookiesMap = RequestUtil.getCookiesMap(request);
            String language = UserValidation.validateLanguage(cookiesMap.get("lang"));
            logger.trace("Email: " + email);
            logger.trace("Language: " + language);
            logger.trace("gRecaptchaResponse: " + gRecaptchaResponse);
            if (!recaptchaValidation.verify(gRecaptchaResponse)) {
                throw new ValidationException("Captcha is wrong");
            }

            User user = userService.login(email, password, language);
            request.getSession().setAttribute("user", user);
        } catch (ValidationException e) {
            logger.error(EXCEPTION_MESSAGE, e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
        } catch (DBException e) {
            logger.error(EXCEPTION_MESSAGE, e);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println(e.getMessage());
        }
    }
}
