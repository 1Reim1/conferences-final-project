package com.my.conferences.email;

@FunctionalInterface
interface EmailModifier {
    void modify(Email email);
}