package com.my.conferences.controllers;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.controllers.commands.user.*;
import com.my.conferences.service.UserService;
import com.my.conferences.service.VerificationCodeService;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

    private final static Logger logger = Logger.getLogger(AuthServlet.class);
    private static final Map<String, Command> commandMap = new HashMap<>();

    @Override
    public void init() {
        UserService userService = (UserService) getServletContext().getAttribute("app/userService");
        VerificationCodeService verificationCodeService = (VerificationCodeService) getServletContext().getAttribute("app/verificationCodeService");
        commandMap.put("login", new LoginCommand(userService));
        commandMap.put("register", new RegisterCommand(userService));
        commandMap.put("send-verification-code", new SendVerificationCodeCommand(verificationCodeService));
        commandMap.put("verify-code", new VerifyCodeCommand(verificationCodeService));
        commandMap.put("modify-password", new ModifyPasswordCommand(userService));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getSession().removeAttribute("user");
        getServletContext().getRequestDispatcher("/WEB-INF/jsp/auth.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.debug("Auth post request");
        RequestUtil.executeCommand(request, response, commandMap);
    }
}
