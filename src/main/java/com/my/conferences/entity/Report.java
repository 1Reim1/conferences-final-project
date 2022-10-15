package com.my.conferences.entity;

import com.my.conferences.service.ValidationException;

public class Report {
    private int id;
    private String topic;
    private int eventId;
    private User speaker;
    private User creator;
    private boolean confirmed;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic.trim();
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public User getSpeaker() {
        return speaker;
    }

    public void setSpeaker(User speaker) {
        this.speaker = speaker;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public void validate() throws ValidationException {
        validateTopic(topic);
    }

    public static void validateTopic(String topic) throws ValidationException {
        if (topic.trim().length() < 3)
            throw new ValidationException("Topic length min: 3");
    }
}
