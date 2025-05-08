package com.example.meuapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class InicioActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inicio);

        // Obtendo referências dos componentes
        Button botaoComecar = findViewById(R.id.BotaoComecar);
        ImageButton btnPerfil = findViewById(R.id.btnPerfil);
        ImageButton btnVoltar = findViewById(R.id.btnVoltar);
        View mainView = findViewById(R.id.main);

        // Configuração dos listeners
        if (botaoComecar != null) {
            botaoComecar.setOnClickListener(view -> {
                startActivity(new Intent(InicioActivity.this, MapaActivity.class));
            });
        }

        if (btnPerfil != null) {
            btnPerfil.setOnClickListener(view -> {
                startActivity(new Intent(InicioActivity.this, CentralActivity.class));
            });
        }

        if (btnVoltar != null) {
            btnVoltar.setOnClickListener(view -> {
                startActivity(new Intent(InicioActivity.this, LoginActivity.class));
            });
        }

        // Configuração do WindowInsets
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }
    }
}