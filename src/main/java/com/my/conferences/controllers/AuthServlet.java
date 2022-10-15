package com.my.conferences.controllers;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.controllers.commands.user.LoginCommand;
import com.my.conferences.controllers.commands.user.RegisterCommand;
import com.my.conferences.service.UserService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    private static final Map<String, Command> commandMap = new HashMap<>();

    @Override
    public void init() {
        UserService userService = (UserService) getServletContext().getAttribute("app/userService");
        commandMap.put("login", new LoginCommand(userService));
        commandMap.put("register", new RegisterCommand(userService));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.removeAttribute("user");

        getServletContext().getRequestDispatcher("/WEB-INF/jsp/auth.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String commandKey = request.getParameter("command");
        Command command = commandMap.get(commandKey);
        if (command == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().printf("Command '%s' is unknown.", commandKey);
            return;
        }

        command.execute(request, response);
    }
}
