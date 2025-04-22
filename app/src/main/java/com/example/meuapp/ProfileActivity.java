package com.example.meuapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    // Declaração dos componentes da interface
    private EditText caixaNome, caixaEmail, caixaPassword;
    private Button botaoSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Certifique-se de que o layout correto está sendo setado

        // Inicializando os elementos do layout
        caixaNome = findViewById(R.id.CaixaNome);
        caixaEmail = findViewById(R.id.CaixaEmail);
        caixaPassword = findViewById(R.id.CaixaPassword);
        botaoSalvar = findViewById(R.id.BotaoSalvar);

        // Carregar as informações do cadastro
        carregarPerfil();

        // Adicionar funcionalidade ao botão Salvar
        botaoSalvar.setOnClickListener(view -> {
            // Lógica para salvar as alterações feitas
            String nome = caixaNome.getText().toString();
            String email = caixaEmail.getText().toString();
            String senha = caixaPassword.getText().toString();

            // Salvar as informações alteradas
            salvarPerfil(nome, email, senha);

            // Mostrar mensagem de sucesso
            Toast.makeText(ProfileActivity.this, "Informações salvas com sucesso!", Toast.LENGTH_SHORT).show();
        });
    }

    // Método para carregar as informações salvas
    private void carregarPerfil() {
        // Recupera as informações do SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);

        String nome = sharedPreferences.getString("nome", "");
        String email = sharedPreferences.getString("email", "");
        String senha = sharedPreferences.getString("senha", "");

        // Preenche os EditTexts com as informações salvas
        caixaNome.setText(nome);
        caixaEmail.setText(email);
        caixaPassword.setText(senha);
    }

    // Método para salvar as informações alteradas
    private void salvarPerfil(String nome, String email, String senha) {
        // Salva as informações no SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("nome", nome);
        editor.putString("email", email);
        editor.putString("senha", senha);

        editor.apply(); // Commit as mudanças
    }
}
