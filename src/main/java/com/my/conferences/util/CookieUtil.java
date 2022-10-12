package com.my.conferences.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class CookieUtil {

    CookieUtil() {}

    public static String getLang(HttpServletRequest request) {
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
                return "en";
            }
            return langCookie.getValue();
        }

        return "en";
    }
}
