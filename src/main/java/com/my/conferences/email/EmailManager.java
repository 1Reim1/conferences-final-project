package com.my.conferences.email;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.User;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EmailManager {
    private static EmailManager instance;
    private final ScheduledExecutorService executorService;
    private final Properties TEMPLATES;
    private final Properties TEMPLATES_UK;
    private final String fromEmail;
    private final Session session;
    private final List<Email> emailList;
    private final Object EMAIL_LIST_MUTEX = new Object();

    public static synchronized EmailManager getInstance() {
        if (instance == null) {
            instance = new EmailManager();
        }

        return instance;
    }

    private EmailManager() {
        TEMPLATES = new Properties();
        TEMPLATES_UK = new Properties();
        loadEmailTemplates();

        emailList = new LinkedList<>();
        fromEmail = "balacknafs@gmail.com";
        String password = "bcnhfmkrfiuhcxjz";

        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this::sendAll, 0, 5, TimeUnit.SECONDS);
    }

    public void stopService() {
        executorService.shutdown();
    }

    public void sendTitleChanged(Event event, String lang) {
        Properties templates = getTemplates(lang);
        Email email = new Email(
                templates.getProperty("event.title_changed.subject"),
                templates.getProperty("event.title_changed.content")
        );
        email.recipientEmails.add(event.getModerator().getEmail());
        email.recipientEmails.addAll(event.getParticipants().stream().map(User::getEmail).collect(Collectors.toList()));
        email.recipientEmails.addAll(event.getReports().stream().map(r -> r.getSpeaker().getEmail()).collect(Collectors.toList()));
        addEmail(email);
    }

    private Properties getTemplates(String lang) {
        if (lang.equals("uk"))
            return TEMPLATES_UK;

        return TEMPLATES;
    }

    private void loadEmailTemplates() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try (InputStream templatesStream = loader.getResourceAsStream("emails.properties");
             InputStream templatesUkStream = loader.getResourceAsStream("emails_uk.properties");
             InputStreamReader templatesReader = new InputStreamReader(templatesStream, StandardCharsets.UTF_8);
             InputStreamReader templatesUkReader = new InputStreamReader(templatesUkStream, StandardCharsets.UTF_8)) {

            TEMPLATES.load(templatesReader);
            TEMPLATES_UK.load(templatesUkReader);
        } catch (IOException e) {
            throw new RuntimeException("Email templates loading exception", e);
        }
    }

    private void addEmail(Email email) {
        synchronized (EMAIL_LIST_MUTEX) {
            emailList.add(email);
        }
    }

    private void sendAll() {
        while (emailList.size() > 0) {
            synchronized (EMAIL_LIST_MUTEX) {
                sendEmail(emailList.remove(0));
            }
        }
    }

    private void sendEmail(Email email) {
        if (email.recipientEmails.isEmpty())
            return;

        System.out.println(email.subject);

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, "Conferences"));
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(String.join(",", email.recipientEmails)));
            message.setSubject(email.subject);
            message.setContent(email.content, "text/html;charset=utf-8");
            message.saveChanges();
            Transport.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private static class Email {
        List<String> recipientEmails;
        String subject;
        String content;

        Email(String subject, String content) {
            this.subject = subject;
            this.content = content;
            recipientEmails = new ArrayList<>();
        }
    }
}
