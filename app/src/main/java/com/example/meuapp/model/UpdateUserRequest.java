package com.example.meuapp.model;

import com.google.gson.annotations.SerializedName;

public class UpdateUserRequest {

    @SerializedName("nome")
    private String nome;

    @SerializedName("email")
    private String email;

    @SerializedName("senha") // Campo para a nova senha
    private String senha;

    // Construtor vazio é útil para Gson e Retrofit
    public UpdateUserRequest() {
    }

    // Construtor opcional se quiser criar o objeto passando todos os valores de uma vez.
    // Se for usá-lo, descomente-o.
    /*
    public UpdateUserRequest(String nome, String email, String senha) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }
    */

    // Getters e Setters (essenciais para Retrofit/Gson serializar/desserializar)
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }


}