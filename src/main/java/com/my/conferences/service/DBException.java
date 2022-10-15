package com.my.conferences.service;

public class DBException extends Exception {

    public DBException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public DBException(String msg) {
        super(msg);
    }
}
