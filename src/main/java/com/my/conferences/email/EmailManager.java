package com.my.conferences.email;

import com.my.conferences.entity.Event;
import com.my.conferences.entity.Report;
import com.my.conferences.entity.User;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EmailManager {
    private static EmailManager instance;
    private final ScheduledExecutorService executorService;
    private final Properties TEMPLATES;
    private final Properties TEMPLATES_UK;
    private final static String SUBJECT_PROPERTY = ".subject";
    private final static String CONTENT_PROPERTY = ".content";
    private final String APP_URL = "http://localhost:8080/conferences";
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

    public void sendTitleChanged(Event event, String prevTitle) {
        List<User> recipients = getRecipientsFromEvent(event);
        addEmail("event.title_changed", recipients, email -> email.content = email.content
                .replace("{{previous_title}}", prevTitle)
                .replace("{{title}}", event.getTitle())
                .replace("{{event_address}}", APP_URL + "/event?id=" + event.getId()));
    }

    public void sendDescriptionChanged(Event event) {
        List<User> recipients = getRecipientsFromEvent(event);
        addEmail("event.description_changed", recipients, email -> email.content = email.content
                .replace("{{title}}", event.getTitle())
                .replace("{{event_address}}", APP_URL + "/event?id=" + event.getId()));
    }

    public void sendDateChanged(Event event, Date prevDate) {
        String pattern = "dd-MM-yyyy HH:mm";
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        List<User> recipients = getRecipientsFromEvent(event);
        addEmail("event.date_changed", recipients, email -> email.content = email.content
                .replace("{{previous_date}}", df.format(prevDate))
                .replace("{{date}}", df.format(event.getDate()))
                .replace("{{title}}", event.getTitle())
                .replace("{{event_address}}", APP_URL + "/event?id=" + event.getId()));
    }

    public void sendPlaceChanged(Event event, String prevPlace) {
        List<User> recipients = getRecipientsFromEvent(event);
        addEmail("event.place_changed", recipients, email -> email.content = email.content
                .replace("{{previous_place}}", prevPlace)
                .replace("{{place}}", event.getPlace())
                .replace("{{title}}", event.getTitle())
                .replace("{{event_address}}", APP_URL + "/event?id=" + event.getId()));
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
        Consumer<Email> emailModifier = getEmailModifierForReport(report, event);
        addEmail("report.topic_changed", recipients, email -> {
            emailModifier.accept(email);
            email.content = email.content.replace("{{prev_topic}}", prevTopic);
        });
    }

    public void sendConfirmedReportTopicChanged(Report report, Event event, String prevTopic) {
        List<User> recipients = getRecipientsFromEvent(event);
        Consumer<Email> emailModifier = getEmailModifierForReport(report, event);
        addEmail("report.confirmed_topic_changed", recipients, email -> {
            emailModifier.accept(email);
            email.content = email.content.replace("{{prev_topic}}", prevTopic);
        });
    }

    private List<User> getRecipientsFromEvent(Event event) {
        List<User> recipients = new ArrayList<>(event.getParticipants().size() + 1);
        recipients.addAll(event.getParticipants());
        recipients.addAll(event.getReports().stream().map(Report::getSpeaker).collect(Collectors.toList()));
        recipients.add(event.getModerator());
        return recipients;
    }

    private Consumer<Email> getEmailModifierForReport(Report report, Event event) {
        return email -> email.content = email.content
                .replace("{{topic}}", report.getTopic())
                .replace("{{title}}", event.getTitle())
                .replace("{{event_address}}", APP_URL + "/event?id=" + event.getId());
    }

    private void addEmail(String emailTemplateProperty, List<User> recipients, Consumer<Email> emailModifier) {
        Email email = new Email(
                TEMPLATES.getProperty(emailTemplateProperty + SUBJECT_PROPERTY),
                TEMPLATES.getProperty(emailTemplateProperty + CONTENT_PROPERTY)
        );
        Email emailUk = new Email(
                TEMPLATES_UK.getProperty(emailTemplateProperty + SUBJECT_PROPERTY),
                TEMPLATES_UK.getProperty(emailTemplateProperty + CONTENT_PROPERTY)
        );

        emailModifier.accept(email);
        emailModifier.accept(emailUk);

        for (User recipient : recipients) {
            if (recipient.getLanguage().equals("uk"))
                emailUk.recipientEmails.add(recipient.getEmail());
            else
                email.recipientEmails.add(recipient.getEmail());
        }

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
        Set<String> recipientEmails;
        String subject;
        String content;

        Email(String subject, String content) {
            this.subject = subject;
            this.content = content;
            recipientEmails = new HashSet<>();
        }
    }
}
