package com.example.meuapp.model;

import com.google.gson.annotations.SerializedName;
public class ApiResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("token") // Exemplo: se o backend reemitir token em alguma operação
    private String token;
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    private boolean success;

    public ApiResponse() { }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
