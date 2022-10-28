package com.my.conferences.entity;

public class VerificationCode {

    private User user;
    private String code;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code.trim();
    }
}
