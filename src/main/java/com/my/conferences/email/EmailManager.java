package com.my.conferences.email;

import com.my.conferences.controllers.EventServlet;
import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;
import com.my.conferences.entity.VerificationCode;
import com.my.conferences.util.PropertiesUtil;
import org.apache.log4j.Logger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Class for sending emails
 */
public class EmailManager {

    private static final Logger logger = Logger.getLogger(EventServlet.class);
    private final ScheduledExecutorService executorService;
    private final Properties TEMPLATES;
    private final Properties TEMPLATES_UK;
    private static final String SUBJECT_PROPERTY = ".subject";
    private static final String CONTENT_PROPERTY = ".content";
    private final String appUrl;
    private final String fromEmail;
    private final String nickname;
    private final Session session;
    private final List<Email> emailList;
    private final Object EMAIL_LIST_MUTEX = new Object();

    public EmailManager(Properties config) {
        emailList = new LinkedList<>();
        executorService = Executors.newSingleThreadScheduledExecutor();
        // load properties from config
        appUrl = config.getProperty("app.url");
        fromEmail = config.getProperty("email");
        nickname = config.getProperty("nickname");
        // load email templates
        try {
            TEMPLATES = PropertiesUtil.loadFromResources("emails.properties");
            TEMPLATES_UK = PropertiesUtil.loadFromResources("emails_uk.properties");
        } catch (IOException e) {
            throw new RuntimeException("Email templates loading exception", e);
        }
        // create session
        String password = config.getProperty("password");
        session = Session.getInstance(config, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });
        startService();
    }

    private void startService() {
        executorService.scheduleAtFixedRate(this::sendAll, 0, 5, TimeUnit.SECONDS);
    }

    public void stopService() {
        executorService.shutdown();
    }

    public void sendTitleChanged(Event event, String prevTitle) {
        List<User> recipients = getRecipientsFromEvent(event);
        addEmail("event.title_changed", recipients, email -> email.content = email.content
                .replace("{{previous_title}}", prevTitle)
                .replace("{{title}}", event.getTitle())
                .replace("{{event_address}}", appUrl + "/event?id=" + event.getId()));
    }

    public void sendDescriptionChanged(Event event) {
        List<User> recipients = getRecipientsFromEvent(event);
        addEmail("event.description_changed", recipients, email -> email.content = email.content
                .replace("{{title}}", event.getTitle())
                .replace("{{event_address}}", appUrl + "/event?id=" + event.getId()));
    }

    public void sendDateChanged(Event event, Date prevDate) {
        String pattern = "dd-MM-yyyy HH:mm";
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        List<User> recipients = getRecipientsFromEvent(event);
        addEmail("event.date_changed", recipients, email -> email.content = email.content
                .replace("{{previous_date}}", df.format(prevDate))
                .replace("{{date}}", df.format(event.getDate()))
                .replace("{{title}}", event.getTitle())
                .replace("{{event_address}}", appUrl + "/event?id=" + event.getId()));
    }

    public void sendPlaceChanged(Event event, String prevPlace) {
        List<User> recipients = getRecipientsFromEvent(event);
        addEmail("event.place_changed", recipients, email -> email.content = email.content
                .replace("{{previous_place}}", prevPlace)
                .replace("{{place}}", event.getPlace())
                .replace("{{title}}", event.getTitle())
                .replace("{{event_address}}", appUrl + "/event?id=" + event.getId()));
    }

    public void sendReportOfferedByModerator(Report report, Event event) {
        List<User> recipients = new ArrayList<>();
        recipients.add(report.getSpeaker());
        addEmail("report.offered_by_moderator", recipients, getEmailModifierForReport(report, event));
    }

    public void sendReportOfferedBySpeaker(Report report, Event event) {
        List<User> recipients = new ArrayList<>();
        recipients.add(event.getModerator());
        addEmail("report.offered_by_speaker", recipients, getEmailModifierForReport(report, event));
    }

    public void sendAddedNewReport(Report report, Event event) {
        List<User> recipients = getRecipientsFromEvent(event);
        addEmail("report.added_new", recipients, getEmailModifierForReport(report, event));
    }

    public void sendReportConfirmedByModerator(Report report, Event event) {
        List<User> recipients = new ArrayList<>();
        recipients.add(report.getSpeaker());
        addEmail("report.confirmed_by_moderator", recipients, getEmailModifierForReport(report, event));
    }

    public void sendReportConfirmedBySpeaker(Report report, Event event) {
        List<User> recipients = new ArrayList<>();
        recipients.add(event.getModerator());
        addEmail("report.confirmed_by_speaker", recipients, getEmailModifierForReport(report, event));
    }

    public void sendReportCancelledByModerator(Report report, Event event) {
        List<User> recipients = new ArrayList<>();
        recipients.add(report.getSpeaker());
        addEmail("report.cancelled_by_moderator", recipients, getEmailModifierForReport(report, event));
    }

    public void sendReportCancelledBySpeaker(Report report, Event event) {
        List<User> recipients = new ArrayList<>();
        recipients.add(event.getModerator());
        addEmail("report.cancelled_by_speaker", recipients, getEmailModifierForReport(report, event));
    }

    public void sendConfirmedReportCancelled(Report report, Event event) {
        List<User> recipients = getRecipientsFromEvent(event);
        addEmail("report.confirmed_cancelled", recipients, getEmailModifierForReport(report, event));
    }

    public void sendReportTopicChanged(Report report, Event event, String prevTopic) {
        List<User> recipients = new ArrayList<>();
        recipients.add(report.getSpeaker());
        EmailModifier emailModifier = getEmailModifierForReport(report, event);
        addEmail("report.topic_changed", recipients, email -> {
            emailModifier.modify(email);
            email.content = email.content.replace("{{prev_topic}}", prevTopic);
        });
    }

    public void sendConfirmedReportTopicChanged(Report report, Event event, String prevTopic) {
        List<User> recipients = getRecipientsFromEvent(event);
        EmailModifier emailModifier = getEmailModifierForReport(report, event);
        addEmail("report.confirmed_topic_changed", recipients, email -> {
            emailModifier.modify(email);
            email.content = email.content.replace("{{prev_topic}}", prevTopic);
        });
    }

    public void sendVerificationCode(VerificationCode verificationCode) {
        List<User> recipients = new ArrayList<>();
        recipients.add(verificationCode.getUser());
        addEmail("verification_code", recipients, email -> email.content = email.content.replace("{{code}}", verificationCode.getCode()));
    }

    private List<User> getRecipientsFromEvent(Event event) {
        List<User> recipients = new ArrayList<>(event.getParticipants().size() + 1);
        recipients.addAll(event.getParticipants());
        recipients.addAll(event.getReports().stream().map(Report::getSpeaker).collect(Collectors.toList()));
        recipients.add(event.getModerator());
        return recipients;
    }

    private EmailModifier getEmailModifierForReport(Report report, Event event) {
        return email -> email.content = email.content
                .replace("{{topic}}", report.getTopic())
                .replace("{{title}}", event.getTitle())
                .replace("{{event_address}}", appUrl + "/event?id=" + event.getId());
    }

    private void addEmail(String emailTemplateProperty, List<User> recipients, EmailModifier emailModifier) {
        Email email = new Email(
                TEMPLATES.getProperty(emailTemplateProperty + SUBJECT_PROPERTY),
                TEMPLATES.getProperty(emailTemplateProperty + CONTENT_PROPERTY)
        );
        Email emailUk = new Email(
                TEMPLATES_UK.getProperty(emailTemplateProperty + SUBJECT_PROPERTY),
                TEMPLATES_UK.getProperty(emailTemplateProperty + CONTENT_PROPERTY)
        );
        // prepare emails to sending
        emailModifier.modify(email);
        emailModifier.modify(emailUk);
        // add recipients to relevant emails
        for (User recipient : recipients) {
            if (recipient.getLanguage().equals("uk")) {
                emailUk.recipientEmails.add(recipient.getEmail());
            } else {
                email.recipientEmails.add(recipient.getEmail());
            }
        }
        // add emails to the list
        synchronized (EMAIL_LIST_MUTEX) {
            emailList.add(email);
            emailList.add(emailUk);
        }
    }

    private void sendAll() {
        while (emailList.size() > 0) {
            Email email;
            synchronized (EMAIL_LIST_MUTEX) {
                email = emailList.remove(0);
            }
            sendEmail(email);
        }
    }

    private void sendEmail(Email email) {
        if (email.recipientEmails.isEmpty())
            return;

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, nickname));
            message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(String.join(",", email.recipientEmails)));
            message.setSubject(email.subject);
            message.setContent(email.content, "text/html;charset=utf-8");
            message.saveChanges();
            Transport.send(message);
            logger.debug(String.format("Email '%s' sent", email.subject));
        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("Email was not sent", e);
        }
    }
}
