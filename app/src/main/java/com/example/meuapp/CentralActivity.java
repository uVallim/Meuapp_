package com.example.meuapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CentralActivity extends AppCompatActivity {

    private TextView textQualSeuDestino;
    private TextView textEditarPerfil;
    private TextView textExcluirSuaConta;
    private TextView textSairDaConta;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_central);

        // Aplicando padding com base nas barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa os componentes
        IniciarComponentes();

        // Adiciona clique nos botões
        textQualSeuDestino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CentralActivity.this, MapaActivity.class);
                startActivity(intent);
            }
        });

        textEditarPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CentralActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        textExcluirSuaConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CentralActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        textSairDaConta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CentralActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    // Método para inicializar os componentes
    private void IniciarComponentes() {
        textQualSeuDestino = findViewById(R.id.TextQualSeuDestino);
        textEditarPerfil = findViewById(R.id.TextEditarPerfil);
        textExcluirSuaConta = findViewById(R.id.TextExcluirSuaConta);
        textSairDaConta = findViewById(R.id.SairDaConta);
    }
}
