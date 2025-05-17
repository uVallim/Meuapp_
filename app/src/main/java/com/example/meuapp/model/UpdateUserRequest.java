package com.example.meuapp.model;

public class UpdateUserRequest {

    // Estes nomes de campo (nome, email, senha) devem bater EXATAMENTE
    // com as chaves que o seu backend espera no corpo da requisição JSON.
    private String nome;
    private String email;
    private String senha; // Campo para a nova senha

    public UpdateUserRequest() {
    }

    // Construtor opcional se quiser criar o objeto passando todos os valores de uma vez
    // public UpdateUserRequest(String nome, String email, String senha) {
    //     this.nome = nome;
    //     this.email = email;
    //     this.senha = senha;
    // }


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

    // Exemplo se você quisesse que o campo em Java se chamasse 'password'
    // mas no JSON fosse 'senha'. Precisaria da anotação @SerializedName.
    /*
    // Importe com.google.gson.annotations.SerializedName;
    @SerializedName("senha") // Diz ao Gson para usar "senha" no JSON
    private String password; // Nome do campo em Java

    public String getPassword() { // Getter para o campo Java
        return password;
    }

    public void setPassword(String password) { // Setter para o campo Java
        this.password = password;
    }
    */
}