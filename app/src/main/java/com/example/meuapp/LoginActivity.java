package com.example.meuapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meuapp.MainActivity;
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

        // Inicializa os componentes
        iniciarComponentes();

        // Inicializa o serviço de autenticação
        authService = ApiClient.getClient().create(AuthService.class);

        // Adiciona clique no botão de login
        buttonLogin.setOnClickListener(view -> loginUser());

        // Adiciona clique para a tela de cadastro
        textViewSignUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, Cadastro.class);
            startActivity(intent);
        });

        // Adiciona clique para "Esqueceu a senha?"
        textViewForgotPassword.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Função de recuperação de senha em breve!", Toast.LENGTH_SHORT).show();
        });
    }

    private void iniciarComponentes() {
        editTextEmail = findViewById(R.id.CaixaDeTextoEmail);
        editTextPassword = findViewById(R.id.CaixaDeTextoSenha);
        buttonLogin = findViewById(R.id.BotaoSalvar);
        textViewSignUpLink = findViewById(R.id.TextInscreverse);
        textViewForgotPassword = findViewById(R.id.TextEsqueceuSenha);
    }
    // Função para aplicar a cifra de César (mesma do Cadastro.java)
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
        // Aplica a cifra de César com chave 3 (igual no cadastro)
        String senhaCriptografada = cifraDeCesar(password, 3);

        LoginRequest request = new LoginRequest(email, senhaCriptografada);
        authService.loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Salvar o token em SharedPreferences
                    String token = response.body().getToken();
                    SharedPreferences preferences = getSharedPreferences("app", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("jwt_token", token);
                    editor.apply();  // Salva o token persistente

                    Toast.makeText(LoginActivity.this, "Login bem-sucedido", Toast.LENGTH_SHORT).show();
                    buttonLogin.setOnClickListener(v->{Intent intent = new Intent(LoginActivity.this, IntroActivity.class);
                        startActivity(intent);});
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Erro no login. Verifique suas credenciais.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Falha na conexão. Tente novamente.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}