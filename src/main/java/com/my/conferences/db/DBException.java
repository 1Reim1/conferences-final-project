package com.my.conferences.db;

public class DBException extends Exception {

    public DBException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public DBException(String msg) {
        super(msg);
    }
}
