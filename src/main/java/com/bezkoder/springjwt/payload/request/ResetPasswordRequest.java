package com.bezkoder.springjwt.payload.request;

import jakarta.validation.constraints.NotBlank;

public class ResetPasswordRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String verificationCode;

    @NotBlank
    private String newPassword;

    public ResetPasswordRequest() {
    }

    public ResetPasswordRequest(String username, String verificationCode, String newPassword) {
        this.username = username;
        this.verificationCode = verificationCode;
        this.newPassword = newPassword;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}