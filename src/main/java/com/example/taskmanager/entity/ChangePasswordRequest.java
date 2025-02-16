package com.example.taskmanager.entity;

import jakarta.validation.constraints.NotBlank;

/**
 * This class is used for handling the request data for changing a user's password.
 * It contains the current password and the new password fields.
 */
public class ChangePasswordRequest {

    @NotBlank
    private String currentPassword;

    @NotBlank
    private String newPassword;

    // Getters and Setters

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
