package com.my.conferences.email;

import java.util.HashSet;
import java.util.Set;

class Email {
    Set<String> recipientEmails;
    String subject;
    String content;

    Email(String subject, String content) {
        this.subject = subject;
        this.content = content;
        recipientEmails = new HashSet<>();
    }
}