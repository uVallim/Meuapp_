package com.example.meuapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class IntroActivity extends AppCompatActivity {
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_intro);

        // Pegando os botões pelo ID
        Button botaoComecar = findViewById(R.id.BotaoComecar);
        @SuppressLint("WrongViewCast") Button btnPerfil = findViewById(R.id.btnPerfil);
        @SuppressLint("WrongViewCast") Button btnVoltar = findViewById(R.id.btnVoltar);

        // Botão para abrir MapaActivity
        botaoComecar.setOnClickListener(view -> {
            Intent intent = new Intent(IntroActivity.this, MapaActivity.class);
            startActivity(intent);
        });

        // Botão para abrir CentralActivity
        btnPerfil.setOnClickListener(view -> {
            Intent intent = new Intent(IntroActivity.this, CentralActivity.class);
            startActivity(intent);
        });
        btnVoltar.setOnClickListener(view -> {
            Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Aplicando o padding baseado nas barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
