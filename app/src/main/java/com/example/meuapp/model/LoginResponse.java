package com.example.meuapp.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private boolean success; // ou status, isSuccess, etc.
    private String token;
    private String message;
    private String email;
    private String name;

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
    public String getUserName() {
        return userName;
    }
    public String getUserEmail() {
        return userEmail;
    }

    @SerializedName("user_name") // Exemplo: se o backend retorna "user_name"
    private String userName;

    @SerializedName("user_email") // Exemplo: se o backend retorna "user_email"
    private String userEmail;

}
