package com.my.conferences.entity;

import com.my.conferences.db.DBException;

import java.io.Serializable;
import java.util.regex.Pattern;

public class User implements Serializable {
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VALID_NAME_REGEX = Pattern.compile("^[a-zA-Z]{3,}$");
    private int id;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private Role role;

    private String language;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email.trim();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName.trim();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String passHash) {
        this.password = passHash.trim();
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = validateLanguage(language);
    }

    public void validateNames() throws DBException {
        if (!VALID_NAME_REGEX.matcher(firstName).find())
            throw new DBException("First name is bad");
        if (!VALID_NAME_REGEX.matcher(lastName).find())
            throw new DBException("Last name is bad");
    }

    public void validateEmailAndPassword() throws DBException {
        validateEmailAndPassword(email, password);
    }

    public static void validateEmailAndPassword(String email, String password) throws DBException {
        if (!VALID_EMAIL_ADDRESS_REGEX.matcher(email).find())
            throw new DBException("Email is bad");
        if (password.length() < 6) {
            throw new DBException("Password is bad");
        }
    }

    public static String validateLanguage(String language) {
        if (!language.equals("en") && !language.equals("uk")) {
            return "en";
        }

        return language;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role=" + role +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (!email.equals(user.email)) return false;
        if (!firstName.equals(user.firstName)) return false;
        if (!lastName.equals(user.lastName)) return false;
        if (!password.equals(user.password)) return false;
        return role == user.role;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + email.hashCode();
        result = 31 * result + firstName.hashCode();
        result = 31 * result + lastName.hashCode();
        result = 31 * result + password.hashCode();
        result = 31 * result + role.hashCode();
        return result;
    }

    public enum Role implements Serializable {
        USER,
        SPEAKER,
        MODERATOR,
    }
}
