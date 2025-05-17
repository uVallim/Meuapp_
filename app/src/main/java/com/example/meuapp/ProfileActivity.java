package com.example.meuapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.meuapp.api.ApiClient;
import com.example.meuapp.api.AuthService;
import com.example.meuapp.model.ApiResponse;
import com.example.meuapp.model.UserProfileResponse; // Certifique-se que esta classe e seus campos (nome, email) existem
import com.example.meuapp.model.UpdateUserRequest; // Certifique-se que esta classe e seus campos (nome, email, senha) existem

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private EditText caixaNome, caixaEmail, caixaPassword;
    private Button botaoSalvar;

    private AuthService authService;

    // NOTA: ProgressDialog é depreciado. Considere usar um ProgressBar no layout ou um DialogFragment.
    private ProgressDialog progressDialog;

    // Constantes para SharedPreferences (DEVEM SER IDÊNTICAS em Login, Central e Profile)
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String USER_NAME_KEY = "user_name"; // Mantido para consistência
    private static final String USER_EMAIL_KEY = "user_email"; // Mantido para consistência

    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initComponents();

        authService = ApiClient.getClient().create(AuthService.class);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processando...");
        progressDialog.setCancelable(false);

        // Carregar dados atuais do usuário para preencher os campos (exceto senha)
        loadUserProfileData();

        botaoSalvar.setOnClickListener(view -> saveAccountChanges());
    }

    private void initComponents() {
        caixaNome = findViewById(R.id.CaixaNome);         // Verifique os IDs no seu XML de Profile
        caixaEmail = findViewById(R.id.CaixaEmail);       // Verifique os IDs no seu XML de Profile
        caixaPassword = findViewById(R.id.CaixaPassword); // Campo para a NOVA senha. Verifique o ID.
        botaoSalvar = findViewById(R.id.BotaoSalvar);     // Verifique o ID no seu XML de Profile
    }

    private void loadUserProfileData() {
        String token = getJwtToken();
        if (token == null) {
            showToast("Sessão não encontrada. Faça login novamente.");
            redirectToLogin();
            return;
        }

        progressDialog.setMessage("Carregando perfil...");
        progressDialog.show();

        // --- LOG PARA DEPURAR O TOKEN ENVIADO ---
        Log.d(TAG, "loadUserProfileData: Tentando carregar perfil com token: " + token);
        // ---------------------------------------

        // Supondo que você adicionou um método como `getCurrentUserProfile` na sua AuthService
        // e um modelo `UserProfileResponse` com os campos `nome` e `email` que correspondem à API.
        // Exemplo de endpoint: @GET("api/users/me")
        authService.getCurrentUserProfile("Bearer " + token).enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse profile = response.body();
                    // Verificar se os TextViews e os dados não são nulos antes de setar
                    if (caixaNome != null && profile.getNome() != null) caixaNome.setText(profile.getNome());
                    if (caixaEmail != null && profile.getEmail() != null) caixaEmail.setText(profile.getEmail());

                    // Nunca preencha o campo de senha com uma senha existente por segurança
                    if (caixaPassword != null) caixaPassword.setText("");

                    Log.d(TAG, "loadUserProfileData: Dados do perfil carregados com sucesso.");
                } else {
                    Log.e(TAG, "loadUserProfileData: Erro ao carregar dados do perfil: " + response.code());
                    // --- LOG PARA DEPURAR ERRO BODY ---
                    String errorBodyString = null;
                    try {
                        if (response.errorBody() != null) errorBodyString = response.errorBody().string();
                    } catch (IOException e) {
                        Log.e(TAG, "loadUserProfileData: Erro ao ler errorBody", e);
                    }
                    Log.e(TAG, "loadUserProfileData: Error Body: " + (errorBodyString != null ? errorBodyString : "N/A"));
                    // ---------------------------------

                    // Se o token for inválido aqui (401/403), redirecionar para login
                    if (response.code() == 401 || response.code() == 403) {
                        showToast("Sessão expirada ao carregar perfil. Faça login novamente.");
                        clearJwtToken(); // Limpa apenas o token (nome/email podem ser lidos na Central)
                        redirectToLogin();
                    } else {
                        showToast("Erro ao carregar dados do perfil. Tente novamente.");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "loadUserProfileData: Falha ao carregar perfil", t);
                String errorMessage = "Falha na conexão ao carregar perfil.";
                if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "Tempo limite de conexão esgotado.";
                } else if (t instanceof java.net.ConnectException) {
                    errorMessage = "Não foi possível conectar ao servidor.";
                } else if (t.getMessage() != null) {
                    errorMessage += ": " + t.getMessage();
                }
                showToast(errorMessage);
            }
        });
    }


    private void saveAccountChanges() {
        String novoNome = caixaNome != null ? caixaNome.getText().toString().trim() : "";
        String novoEmail = caixaEmail != null ? caixaEmail.getText().toString().trim() : "";
        String novaSenha = caixaPassword != null ? caixaPassword.getText().toString().trim() : "";


        String token = getJwtToken();
        if (token == null) {
            showToast("Sessão expirada. Faça login novamente.");
            redirectToLogin();
            return;
        }

        // Validações locais
        if (!novoEmail.isEmpty() && !isEmailValid(novoEmail)) {
            showToast("Formato do novo e-mail inválido.");
            if (caixaEmail != null) caixaEmail.setError("E-mail inválido");
            if (caixaEmail != null) caixaEmail.requestFocus();
            return;
        }

        // Use sua validação de senha aqui.
        if (!novaSenha.isEmpty() && !isPasswordValid(novaSenha)) {
            // A mensagem de erro específica virá de isPasswordValid
            if (caixaPassword != null) caixaPassword.setError("Senha inválida");
            if (caixaPassword != null) caixaPassword.requestFocus();
            return;
        }


        UpdateUserRequest request = new UpdateUserRequest();
        boolean hasChanges = false;

        // Adicionar ao request apenas os campos que não estão vazios
        // O ideal seria comparar com os valores originais carregados em loadUserProfileData
        // para enviar apenas o que realmente mudou.
        if (!novoNome.isEmpty()) {
            request.setNome(novoNome);
            hasChanges = true;
        }
        if (!novoEmail.isEmpty()) {
            request.setEmail(novoEmail);
            hasChanges = true;
        }
        if (!novaSenha.isEmpty()) {
            request.setSenha(novaSenha); // Backend deve criptografar antes de salvar
            hasChanges = true;
        }

        if (!hasChanges) {
            showToast("Nenhuma alteração detectada para salvar.");
            return;
        }

        progressDialog.setMessage("Salvando alterações...");
        progressDialog.show();

        String authHeader = "Bearer " + token;
        // --- LOG PARA DEPURAR O TOKEN ENVIADO ---
        Log.d(TAG, "saveAccountChanges: Tentando salvar alterações com token: " + token);
        // ---------------------------------------

        // Certifique-se que sua interface AuthService tem um método updateAccount assim:
        // @PUT("seu/endpoint/de/atualizacao") // ou @POST, @PATCH
        // Call<ApiResponse> updateAccount(@Header("Authorization") String authToken, @Body UpdateUserRequest request);
        authService.updateAccount(authHeader, request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    showToast("Informações atualizadas com sucesso!");
                    // Limpar campo de senha após salvar (mesmo que a API retorne sucesso)
                    if (caixaPassword != null) caixaPassword.setText("");

                    // Opcional: Recarregar os dados do perfil caso o nome ou email tenham mudado
                    // para atualizar a tela, ou se o backend retornar o novo token no response.
                    // Se o backend retorna um NOVO token após a atualização (com email atualizado no payload),
                    // você deve salvar este novo token aqui para evitar problemas de sessão.
                    // Ex: if (response.body() != null && response.body().getToken() != null) {
                    //         saveJwtToken(response.body().getToken()); // Implemente saveJwtToken se necessário
                    // }
                    if (request.getNome() != null || request.getEmail() != null) {
                        loadUserProfileData(); // Recarrega os dados atualizados para a tela
                    }

                    // Opcional: finish(); // Fechar activity ou redirecionar para CentralActivity
                } else {
                    // --- LOG PARA DEPURAR ERRO BODY ---
                    String errorBodyString = null;
                    try {
                        if (response.errorBody() != null) errorBodyString = response.errorBody().string();
                    } catch (IOException e) {
                        Log.e(TAG, "saveAccountChanges: Erro ao ler errorBody", e);
                    }
                    Log.e(TAG, "saveAccountChanges: Erro na API: " + response.code() + " - Body: " + (errorBodyString != null ? errorBodyString : "N/A"));
                    // ---------------------------------

                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Log.e(TAG, "saveAccountChanges: Falha na requisição de atualização", t);
                String errorMessage = "Falha na conexão ao atualizar. Verifique sua internet.";
                if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "Tempo limite de conexão esgotado.";
                } else if (t instanceof java.net.ConnectException) {
                    errorMessage = "Não foi possível conectar ao servidor.";
                } else if (t.getMessage() != null){
                    errorMessage = "Erro de rede: " + t.getMessage();
                }
                showToast(errorMessage);
            }
        });
    }

    // Método genérico para lidar com erros da API (pode ser reutilizado)
    private void handleApiError(Response<?> response) {
        String errorBodyString = null;
        if (response.errorBody() != null) {
            try {
                errorBodyString = response.errorBody().string(); // Ler o corpo do erro uma vez
            } catch (IOException e) {
                Log.e(TAG, "handleApiError: Erro ao ler o corpo do erro da API", e);
            }
        }

        Log.e(TAG, "handleApiError: Erro na API: " + response.code() + " - Body: " + (errorBodyString != null ? errorBodyString : "N/A"));

        String errorMessage = "Erro desconhecido (Código: " + response.code() + ")";
        Gson gson = new Gson();

        // Tenta parsear uma estrutura de erro comum (ex: { "message": "mensagem de erro" })
        if (errorBodyString != null && !errorBodyString.trim().isEmpty()) {
            try {
                ApiResponse errorResponse = gson.fromJson(errorBodyString, ApiResponse.class);
                if (errorResponse != null && errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty()) {
                    errorMessage = errorResponse.getMessage();
                }
            } catch (JsonSyntaxException e) {
                Log.w(TAG, "handleApiError: Não foi possível parsear o JSON do corpo do erro: " + e.getMessage());
            }
        }

        // Mensagens específicas por código de erro, se o parse genérico falhar ou não for suficiente
        switch (response.code()) {
            case 400: // Bad Request
                if (errorMessage.startsWith("Erro desconhecido")) errorMessage = "Dados inválidos fornecidos.";
                break;
            case 401: // Unauthorized
            case 403: // Forbidden
                errorMessage = "Sessão expirada ou não autorizada. Faça login novamente.";
                clearJwtToken(); // Limpa apenas o token
                redirectToLogin();
                break;
            case 404: // Not Found
                if (errorMessage.startsWith("Erro desconhecido")) errorMessage = "Recurso não encontrado.";
                break;
            case 409: // Conflict
                if (errorMessage.startsWith("Erro desconhecido")) errorMessage = "Conflito de dados (ex: e-mail já em uso).";
                break;
            case 500: // Internal Server Error
                if (errorMessage.startsWith("Erro desconhecido")) errorMessage = "Erro interno no servidor. Tente mais tarde.";
                break;
            default:
                // Mantém a mensagem parseada ou o fallback inicial
                break;
        }
        showToast(errorMessage);
    }


    private String getJwtToken() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString(TOKEN_KEY, null);
        Log.d(TAG, "getJwtToken() chamado. Token retornado: " + (token != null ? "presente" : "nulo"));
        return token;
    }

    // Método para limpar APENAS o token (nome/email podem ser lidos na Central)
    private void clearJwtToken() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(TOKEN_KEY);
        editor.apply();
        Log.i(TAG, "Token JWT removido de SharedPreferences.");
    }

    private void redirectToLogin() {
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Log.i(TAG, "Redirecionando para LoginActivity.");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Implemente sua lógica de validação de senha aqui
    private boolean isPasswordValid(String password) {
        if (password == null || password.isEmpty()) {
            return true; // Se a senha não for alterada, é válida (não está sendo definida)
        }
        // Exemplo: Mínimo 8 caracteres
        if (password.length() < 8) {
            showToast("A nova senha deve ter pelo menos 8 caracteres.");
            return false;
        }
        // Adicione outras regras: maiúsculas, minúsculas, números, símbolos, etc.
        // if (!password.matches(".*[A-Z].*")) {
        //     showToast("A senha deve conter pelo menos uma letra maiúscula.");
        //     return false;
        // }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    // Opcional: Método para salvar APENAS o token (se a API de atualização retornar um novo)
    // private void saveJwtToken(String newToken) {
    //     SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    //     preferences.edit().putString(TOKEN_KEY, newToken).apply();
    //     Log.i(TAG, "Novo Token JWT salvo após atualização de perfil.");
    // }
}
