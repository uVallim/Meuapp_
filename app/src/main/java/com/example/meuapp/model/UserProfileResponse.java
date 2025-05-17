package com.example.meuapp.model;

import com.google.gson.annotations.SerializedName;

public class UserProfileResponse {
    @SerializedName("nome") // Corresponda ao nome do campo no JSON da API
    private String nome;

    @SerializedName("email") // Corresponda ao nome do campo no JSON da API
    private String email;

    // Getters
    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    // Setters (opcional, mais útil para testes ou criação manual)
    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}