package com.example.meuapp.model;

public class LoginResponse {
    private boolean success; // ou status, isSuccess, etc.
    private String token;
    private String message;

    // Getters e Setters
    public boolean isSuccess() {
        return success;
    }

    // Ou se o campo tiver outro nome:
    public boolean getStatus() {
        return success;
    }

    public String getToken() {
        return token;
    }
}
