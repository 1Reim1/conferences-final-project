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
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

public class LoginCommand implements Command {
    private static final UserManager userManager = UserManager.getInstance();
    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email;
        String password;

        try {
            email = RequestUtil.getStringParameter(request, "email");
            password = RequestUtil.getStringParameter(request, "password");
        }   catch (ValidationException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println(e.getMessage());
            return;
        }

        Map<String, String> cookiesMap = RequestUtil.getCookiesMap(request);
        String language = cookiesMap.getOrDefault("lang", "en");
        User.validateLanguage(language);

        User user;
        try {
            user = userManager.login(email, password, language);
        } catch (DBException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().println(e.getMessage());
            return;
        }

        HttpSession session = request.getSession();
        session.setAttribute("user", user);
    }
}
