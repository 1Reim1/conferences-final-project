package com.my.conferences.controllers;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;
import com.my.conferences.logic.EventManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private static final EventManager eventManager = EventManager.getInstance();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        request.setAttribute("user", user);
        request.setAttribute("normalRole", User.Role.USER);

//        Event event = new Event();
//        event.setId(3);
//        event.setTitle("Title Test");
//        event.setDescription("Description ;)");
//        event.setParticipants(1000);
//        event.setReports(6);
//        event.setDate(new GregorianCalendar(2022, Calendar.SEPTEMBER, 27, 16, 0));
//
//        Event event2 = new Event();
//        event2.setId(4);
//        event2.setTitle("Title Test2");
//        event2.setDescription("Description2 ;)");
//        event2.setParticipants(1432);
//        event2.setReports(4);
//        event2.setDate(new GregorianCalendar(2022, Calendar.SEPTEMBER, 28, 16, 0));


        List<Event> events = new ArrayList<>();
//        events.add(event);
//        events.add(event2);

        request.setAttribute("events", events);

        System.out.println(user);
        getServletContext().getRequestDispatcher("/home.jsp").forward(request, response);
    }
}