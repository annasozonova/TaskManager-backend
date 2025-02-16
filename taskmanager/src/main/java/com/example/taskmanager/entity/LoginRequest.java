package com.example.taskmanager.entity;

/**
 * This class represents the login request data containing the username and password.
 * It is used to authenticate a user in the system.
 */
public class LoginRequest {
    private String username;
    private String password;

    /**
     * Returns a string representation of the LoginRequest object.
     * The password is protected and not shown in the returned string.
     *
     * @return string representation of the LoginRequest object
     */
    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }

    // Getters and Setters

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

