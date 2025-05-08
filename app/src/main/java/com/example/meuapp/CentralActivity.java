package com.example.meuapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class CentralActivity extends AppCompatActivity {

    private TextView textQualSeuDestino, textEditarPerfil, textExcluirSuaConta, textSairDaConta;
    private ImageButton imageButtonSair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_central);

        // Initialize components
        initComponents();

        // Set system window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set click listeners
        setClickListeners();
    }

    private void initComponents() {
        textQualSeuDestino = findViewById(R.id.TextQualSeuDestino);
        textEditarPerfil = findViewById(R.id.TextEditarPerfil);
        textExcluirSuaConta = findViewById(R.id.TextExcluirSuaConta);
        textSairDaConta = findViewById(R.id.SairDaConta);
        imageButtonSair = findViewById(R.id.imageButtonSair);
    }

    private void setClickListeners() {
        // Set click listeners using lambda expressions
        textQualSeuDestino.setOnClickListener(v ->
                startActivity(new Intent(this, MapaActivity.class)));

        textEditarPerfil.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        textExcluirSuaConta.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        textSairDaConta.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        imageButtonSair.setOnClickListener(v -> finish());
    }
}