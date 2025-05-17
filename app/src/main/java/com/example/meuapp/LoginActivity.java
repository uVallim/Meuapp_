package com.example.meuapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.meuapp.api.ApiClient;
import com.example.meuapp.api.AuthService;
import com.example.meuapp.model.LoginRequest;
import com.example.meuapp.model.LoginResponse; // Certifique-se que esta classe tem getToken(), getUserName() e getUserEmail()

// Importações para lidar com erro body
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.example.meuapp.model.ApiResponse; // Assumindo que ApiResponse pode ser usado para parsear erros

import java.io.IOException; // Importação necessária para leitura de erro body

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private AppCompatButton buttonLogin;
    private TextView textViewSignUpLink, textViewForgotPassword;
    private AuthService authService;

    // Constantes para SharedPreferences (UNIFICADAS)
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String USER_NAME_KEY = "user_name";
    private static final String USER_EMAIL_KEY = "user_email";

    // Chaves para passar via Intent (Mantidas para consistência, embora não usadas para Login -> Inicio)
    public static final String EXTRA_USERNAME = "com.example.meuapp.EXTRA_USERNAME";
    public static final String EXTRA_USER_EMAIL = "com.example.meuapp.EXTRA_USER_EMAIL";

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        iniciarComponentes();
        authService = ApiClient.getClient().create(AuthService.class);

        // Removido o login automático aqui, o usuário sempre precisa clicar para logar
        buttonLogin.setOnClickListener(view -> loginUser());

        textViewSignUpLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, Cadastro.class)); // Supondo que Cadastro.class existe
        });

        textViewForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, "Função de recuperação de senha em breve!", Toast.LENGTH_SHORT).show();
        });
    }

    private void iniciarComponentes() {
        // Certifique-se que os IDs correspondem ao seu XML de Login
        editTextEmail = findViewById(R.id.CaixaDeTextoEmail);
        editTextPassword = findViewById(R.id.CaixaDeTextoSenha);
        buttonLogin = findViewById(R.id.BotaoSalvar);
        textViewSignUpLink = findViewById(R.id.TextInscreverse);
        textViewForgotPassword = findViewById(R.id.TextEsqueceuSenha);
    }

    // Cifra de César (AVISO: EXTREMAMENTE INSEGURO)
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

        Log.d(TAG, "Iniciando autenticação para: " + email);

        authService.loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    String token = loginResponse.getToken();
                    // Assumindo que LoginResponse tem métodos getUserName() e getUserEmail()
                    String userName = loginResponse.getUserName();
                    String userEmail = loginResponse.getUserEmail();

                    Log.d(TAG, "Login bem-sucedido. Token recebido.");
                    if (userName != null) Log.d(TAG, "Nome do usuário recebido: " + userName);
                    else Log.w(TAG, "Nome do usuário não recebido na resposta de login.");
                    if (userEmail != null) Log.d(TAG, "Email do usuário recebido: " + userEmail);
                    else Log.w(TAG, "Email do usuário não recebido na resposta de login.");

                    // Salvar token, nome E email em SharedPreferences
                    saveAuthData(token, userName, userEmail);

                    // Redirecionar para InicioActivity (Conforme o fluxo Login -> Inicio -> Central)
                    redirectToInicioActivity();

                } else {
                    Log.e(TAG, "Erro na resposta de login: " + response.code());

                    String errorMessage = "Credenciais inválidas";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            Log.e(TAG, "Erro Body: " + errorBodyString);
                            Gson gson = new Gson();
                            ApiResponse apiError = gson.fromJson(errorBodyString, ApiResponse.class);
                            if (apiError != null && apiError.getMessage() != null && !apiError.getMessage().isEmpty()) {
                                errorMessage = apiError.getMessage();
                            }
                        } catch (IOException | JsonSyntaxException e) {
                            Log.e(TAG, "Erro ao ler ou parsear corpo do erro: ", e);
                        }
                    }
                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Falha na requisição de login", t);
                String errorMessage = "Erro de conexão";
                if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "Tempo limite de conexão esgotado. Verifique sua internet.";
                } else if (t instanceof java.net.ConnectException) {
                    errorMessage = "Não foi possível conectar ao servidor.";
                } else if (t.getMessage() != null) {
                    errorMessage = "Erro de rede: " + t.getMessage();
                }
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para salvar o token, nome E o email em SharedPreferences
    private void saveAuthData(String token, String userName, String userEmail) {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN_KEY, token); // Salva o token

        if (userName != null) {
            editor.putString(USER_NAME_KEY, userName); // Salva o nome do usuário
        } else {
            editor.remove(USER_NAME_KEY); // Remove a chave se o nome for nulo
        }

        if (userEmail != null) {
            editor.putString(USER_EMAIL_KEY, userEmail); // Salva o email
        } else {
            editor.remove(USER_EMAIL_KEY); // Remove a chave se o email for nulo
        }

        editor.apply(); // Salva de forma assíncrona
        Log.i(TAG, "Token JWT, nome e email salvos em SharedPreferences.");
    }

    // Método para redirecionar para InicioActivity
    private void redirectToInicioActivity() {
        // Certifique-se de que InicioActivity é o nome correto da sua próxima tela
        Intent intent = new Intent(LoginActivity.this, InicioActivity.class);
        // Configura flags para limpar a pilha de atividades
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finaliza LoginActivity
        Log.i(TAG, "Redirecionando para InicioActivity.");
    }

    // Método para ler o token (Mantido para consistência ou uso futuro, mas não usado no fluxo de login principal)
    private String getJwtToken() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(TOKEN_KEY, null);
    }
}
