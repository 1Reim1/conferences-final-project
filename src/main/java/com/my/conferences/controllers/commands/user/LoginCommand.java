package com.my.conferences.controllers.commands.user;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.db.DBException;
import com.my.conferences.entity.User;
import com.my.conferences.logic.UserManager;
import com.my.conferences.logic.ValidationException;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class LoginCommand implements Command {
    private static final UserManager userManager = UserManager.getInstance();

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String email = RequestUtil.getStringParameter(request, "email");
            String password = RequestUtil.getStringParameter(request, "password");

            Map<String, String> cookiesMap = RequestUtil.getCookiesMap(request);
            String language = User.validateLanguage(cookiesMap.getOrDefault("lang", "en"));
            User user = userManager.login(email, password, language);

            request.getSession().setAttribute("user", user);
        } catch (ValidationException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
        } catch (DBException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println(e.getMessage());
        }
    }
}
