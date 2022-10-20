package com.my.conferences.controllers.tag;

import jakarta.servlet.jsp.tagext.SimpleTagSupport;

import java.io.IOException;

public class ErrorAlertTag extends SimpleTagSupport {

    @Override
    public void doTag() throws IOException {
        getJspContext().getOut().print("<div class=\"alert alert-danger\" id=\"error-alert\" role=\"alert\" style=\"text-align: center; display: none\"></div>");
    }
}
