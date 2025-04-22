package com.example.meuapp.model;

public class User {
    private String nome;  // Alterado de 'username' para 'nome'
    private String email;
    private String senha;  // Alterado de 'password' para 'senha' (igual ao backend)
    private String type;
    private String telefone;  // Alterado de 'phone' para 'telefone'

    public User(String nome, String email, String senha, String type, String telefone) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.type = type;
        this.telefone = telefone;
    }
}