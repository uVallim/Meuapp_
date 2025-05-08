package com.example.meuapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meuapp.api.ApiClient;
import com.example.meuapp.api.AuthService;
import com.example.meuapp.model.LoginRequest;
import com.example.meuapp.model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private AppCompatButton buttonLogin;
    private TextView textViewSignUpLink, textViewForgotPassword;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Ajuste de padding para suportar as barras do sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        iniciarComponentes();
        authService = ApiClient.getClient().create(AuthService.class);

        buttonLogin.setOnClickListener(view -> loginUser());

        textViewSignUpLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, Cadastro.class));
        });

        textViewForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Função de recuperação de senha em breve!", Toast.LENGTH_SHORT).show();
        });
    }

    private void iniciarComponentes() {
        editTextEmail = findViewById(R.id.CaixaDeTextoEmail);
        editTextPassword = findViewById(R.id.CaixaDeTextoSenha);
        buttonLogin = findViewById(R.id.BotaoSalvar);
        textViewSignUpLink = findViewById(R.id.TextInscreverse);
        textViewForgotPassword = findViewById(R.id.TextEsqueceuSenha);
    }

    private String cifraDeCesar(String input, int chave) {
        StringBuilder resultado = new StringBuilder();
        for (char c : input.toCharArray()) {
            resultado.append((char) (c + chave));
        }
        return resultado.toString();
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        String senhaCriptografada = cifraDeCesar(password, 3);
        LoginRequest request = new LoginRequest(email, senhaCriptografada);

        Log.d("LOGIN", "Iniciando autenticação para: " + email);

        authService.loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    Log.d("LOGIN", "Token recebido: " + token);

                    // Salvar token
                    SharedPreferences preferences = getSharedPreferences("app", MODE_PRIVATE);
                    preferences.edit().putString("jwt_token", token).apply();

                    // Redirecionar imediatamente
                    Log.d("LOGIN", "Redirecionando para IntroActivity");
                    startActivity(new Intent(LoginActivity.this, InicioActivity.class));
                    finish();
                } else {
                    Log.e("LOGIN", "Erro na resposta: " + response.code());
                    Toast.makeText(LoginActivity.this,
                            "Credenciais inválidas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("LOGIN", "Falha na requisição: " + t.getMessage());
                Toast.makeText(LoginActivity.this,
                        "Erro de conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }
}