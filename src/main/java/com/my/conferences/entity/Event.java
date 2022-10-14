package com.my.conferences.entity;

import com.my.conferences.db.DBException;

import java.util.Date;
import java.util.List;

public class Event {
    private int id;
    private String title;
    private String description;
    private Date date;
    private String place;
    private User moderator;
    private List<Report> reports;
    private List<User> participants;
    private boolean hidden;
    private int statistics;
    private String language;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description.trim();
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place.trim();
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    public List<User> getParticipants() {
        return participants;
    }

    public void setParticipants(List<User> participants) {
        this.participants = participants;
    }

    public User getModerator() {
        return moderator;
    }

    public void setModerator(User moderator) {
        this.moderator = moderator;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public int getStatistics() {
        return statistics;
    }

    public void setStatistics(int statistics) {
        this.statistics = statistics;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void validate() throws DBException {
        validateTitle(title);
        validateDescription(description);
        validateDate(date);
        validatePlace(place);
    }

    public static void validateTitle(String title) throws DBException {
        if (title.trim().length() < 3)
            throw new DBException("Title min length: 3");
    }

    public static void validateDescription(String description) throws DBException {
        if (description.trim().length() < 20)
            throw new DBException("Description min length: 20");
    }

    public static void validateDate(Date date) throws DBException {
        if (date.compareTo(new Date()) < 0)
            throw new DBException("Required future date");
    }

    public static void validatePlace(String place) throws DBException {
        if (place.trim().length() < 5)
            throw new DBException("Place min length: 5");
    }

    public enum Order {
        DATE,
        REPORTS,
        PARTICIPANTS
    }
}
