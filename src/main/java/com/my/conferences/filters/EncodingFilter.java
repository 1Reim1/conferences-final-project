package com.my.conferences.filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(filterName = "encoding")
public class EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        Cookie[] cookies = request.getCookies();
        Cookie langCookie = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("lang")) {
                    langCookie = cookie;
                }
            }
        }

        if (langCookie != null) {
            if (!langCookie.getValue().equals("en") && !langCookie.getValue().equals("uk")) {
                langCookie.setValue("en");
                response.addCookie(langCookie);
            }
        }   else {
            langCookie = new Cookie("lang", "en");
            response.addCookie(langCookie);
        }

        chain.doFilter(request, response);
    }
}
