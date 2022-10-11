package com.my.conferences.controllers.utils;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

public class EmailUtil {

    private EmailUtil() {

    }

    public static void sendEmail(List<String> recipientEmails, String subject, String content) throws MessagingException {
        String fromEmail = "balacknafs@gmail.com";
        String password = "PassBissness4$";

        Properties properties = new Properties();

        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", true);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(String.join(",", recipientEmails)));
        message.setSubject(subject);
        message.setContent(content, "text/html;charset=utf-8");
        message.saveChanges();
        Transport.send(message);
    }
}
