package com.my.conferences.controllers.commands.user;

import com.my.conferences.controllers.commands.Command;
import com.my.conferences.service.DBException;
import com.my.conferences.service.ValidationException;
import com.my.conferences.service.VerificationCodeService;
import com.my.conferences.util.RequestUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

import java.io.IOException;

public class VerifyCodeCommand implements Command {

    private final static String EXCEPTION_MESSAGE = "Exception in VerifyCodeCommand";
    private final static Logger logger = Logger.getLogger(VerifyCodeCommand.class);
    private final VerificationCodeService verificationCodeService;

    public VerifyCodeCommand(VerificationCodeService verificationCodeService) {
        this.verificationCodeService = verificationCodeService;
    }

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String email = RequestUtil.getStringParameter(request, "email");
            String code = RequestUtil.getStringParameter(request, "code");
            logger.trace("Email: " + email);
            logger.trace("Code: " + code);
            boolean codeIsCorrect = verificationCodeService.verifyCode(email, code);
            response.getWriter().println(codeIsCorrect);
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
