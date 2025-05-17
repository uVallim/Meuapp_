package com.example.meuapp;

import android.app.AlertDialog;
import android.app.ProgressDialog; // NOTA: ProgressDialog é depreciado. Considere alternativas.
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.meuapp.api.ApiClient;
import com.example.meuapp.api.AuthService;
import com.example.meuapp.model.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CentralActivity extends AppCompatActivity {

    private TextView textQualSeuDestino, textEditarPerfil, textExcluirSuaConta, textSairDaConta;
    private TextView textNomeUsuario; // TextView para o nome do usuário
    private TextView textEmailUsuario; // TextView para o email do usuário
    private ImageButton imageButtonSair;
    private AuthService authService;

    // NOTA: ProgressDialog é depreciado. Considere alternativas modernas.
    private ProgressDialog progressDialog;

    // Constantes para SharedPreferences (DEVEM SER IDÊNTICAS em Login, Central e Profile)
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String USER_NAME_KEY = "user_name"; // Chave para ler o nome
    private static final String USER_EMAIL_KEY = "user_email"; // Chave para ler o email

    private static final String TAG = "CentralActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_central);

        initComponents();
        setupServices();
        setClickListeners();

        // Exibir as informações do usuário lidas do SharedPreferences
        displayUserInfoFromPrefs();

        Log.d(TAG, "Token na CentralActivity (onCreate): " + getJwtToken());

        // Opcional: Carregar dados completos do perfil via API
        // loadUserProfileDataFromApi();
    }

    private void initComponents() {
        textQualSeuDestino = findViewById(R.id.TextQualSeuDestino);
        textEditarPerfil = findViewById(R.id.TextEditarPerfil);
        textExcluirSuaConta = findViewById(R.id.TextExcluirSuaConta);
        textSairDaConta = findViewById(R.id.SairDaConta);
        imageButtonSair = findViewById(R.id.imageButtonSair);

        // Encontrar os TextViews para o nome e email no layout
        textNomeUsuario = findViewById(R.id.TextNomeUsuario); // ID do TextView no XML
        textEmailUsuario = findViewById(R.id.TextEmailUsuario); // ID do TextView no XML
    }

    private void setupServices() {
        authService = ApiClient.getClient().create(AuthService.class);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
    }

    private void setClickListeners() {
        textQualSeuDestino.setOnClickListener(v ->
                startActivity(new Intent(this, MapaActivity.class))); // Verifique se MapaActivity existe

        textEditarPerfil.setOnClickListener(v -> {
            Log.d(TAG, "Tentando editar perfil. Token atual: " + getJwtToken());
            startActivity(new Intent(this, ProfileActivity.class)); // Navega para ProfileActivity
        });

        textExcluirSuaConta.setOnClickListener(v ->
                showDeleteConfirmationDialog());

        // Sair da conta (Logout)
        textSairDaConta.setOnClickListener(v -> {
            Log.d(TAG, "Saindo da conta (logout).");
            clearAuthData(); // Limpa token, nome E email do SharedPreferences
            redirectToLogin("Sessão encerrada com sucesso.");
        });

        imageButtonSair.setOnClickListener(v -> {
            // Este botão só fecha a CentralActivity, não faz logout.
            finish();
        });
    }

    // Método para exibir informações do usuário lidas do SharedPreferences
    private void displayUserInfoFromPrefs() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Extrair o nome e o email usando as chaves definidas
        String userName = prefs.getString(USER_NAME_KEY, null);
        String userEmail = prefs.getString(USER_EMAIL_KEY, null); // Lê o email usando a chave CORRETA

        // --- LOGS PARA DEPURAR A LEITURA ---
        Log.d(TAG, "displayUserInfoFromPrefs: Lendo SharedPreferences.");
        Log.d(TAG, "displayUserInfoFromPrefs: Chave Nome (" + USER_NAME_KEY + ") -> Valor: " + userName);
        Log.d(TAG, "displayUserInfoFromPrefs: Chave Email (" + USER_EMAIL_KEY + ") -> Valor: " + userEmail);
        // ---------------------------------


        // Atualizar o TextView do nome
        if (textNomeUsuario != null) {
            if (userName != null && !userName.isEmpty()) {
                textNomeUsuario.setText("Olá, " + userName + "!");
            } else {
                textNomeUsuario.setText("Olá, Usuário!"); // Padrão se o nome não vier
                Log.w(TAG, "Nome do usuário não encontrado em SharedPreferences.");
            }
        } else {
            Log.e(TAG, "TextView com ID TextNomeUsuario não encontrado no layout.");
        }

        // Atualizar o TextView do email
        if (textEmailUsuario != null) {
            if (userEmail != null && !userEmail.isEmpty()) {
                textEmailUsuario.setText(userEmail);
            } else {
                textEmailUsuario.setText("Email não disponível"); // Padrão se o email não vier
                Log.w(TAG, "Email do usuário não encontrado em SharedPreferences.");
            }
        } else {
            Log.e(TAG, "TextView com ID TextEmailUsuario não encontrado no layout.");
        }
    }


    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja excluir sua conta? Esta ação não pode ser desfeita e todos os seus dados serão perdidos.")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    deleteAccount();
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private String getJwtToken() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString(TOKEN_KEY, null);
        Log.d(TAG, "getJwtToken() chamado. Token retornado: " + (token != null ? "presente" : "nulo"));
        return token;
    }

    // Modificado para limpar token, nome E email do SharedPreferences
    private void clearAuthData() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(TOKEN_KEY); // Remove o token
        editor.remove(USER_NAME_KEY); // Remove o nome
        editor.remove(USER_EMAIL_KEY); // Remove o email
        editor.apply();
        Log.i(TAG, "Dados de autenticação (token, nome e email) removidos de SharedPreferences.");
    }

    private void deleteAccount() {
        String token = getJwtToken();
        if (token == null || token.isEmpty()) {
            showToast("Sessão expirada ou token não encontrado. Faça login novamente.");
            redirectToLogin("Sessão expirada.");
            return;
        }

        progressDialog.setMessage("Excluindo conta...");
        progressDialog.show();

        // Loga o token que está sendo enviado para depuração
        Log.d(TAG, "Tentando excluir conta com token: " + token);

        // Certifique-se que sua interface AuthService tem um método deleteAccount ASSIM:
        // @DELETE("seu/endpoint/de/exclusao") // O método HTTP deve ser DELETE e o path deve ser EXATO
        // Call<ApiResponse> deleteAccount(@Header("Authorization") String authToken);
        // O endpoint correto geralmente é algo como "/api/users/me" ou "/api/account"
        // E O MÉTODO DEVE SER DELETE.

        authService.deleteAccount("Bearer " + token).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    String successMessage = "Conta excluída com sucesso!";
                    if (response.body() != null && response.body().getMessage() != null && !response.body().getMessage().isEmpty()) {
                        successMessage = response.body().getMessage();
                    }
                    handleDeleteSuccess(successMessage);
                } else {
                    // Se o erro for 401 (Não Autorizado) ou 403 (Proibido), indica problema no token
                    if (response.code() == 401 || response.code() == 403) {
                        showToast("Sessão expirada ou não autorizada para exclusão. Faça login novamente.");
                        clearAuthData();
                        redirectToLogin("Sessão expirada.");
                    } else {
                        handleDeleteError(response); // Lida com outros erros (404, 409, 500, etc.)
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                handleNetworkError(t); // Lida com erros de conexão/rede
            }
        });
    }

    private void handleDeleteSuccess(String message) {
        showToast(message);
        clearAuthData(); // Limpa dados após exclusão bem-sucedida
        redirectToLogin("Sua conta foi excluída.");
    }

    private void handleDeleteError(Response<ApiResponse> response) {
        String errorBodyString = null;
        if (response.errorBody() != null) {
            try {
                errorBodyString = response.errorBody().string();
            } catch (IOException e) {
                Log.e(TAG, "Erro ao ler o corpo do erro da API durante exclusão", e);
            }
        }

        Log.e(TAG, "Erro ao excluir conta (HTTP " + response.code() + ") - Body: " + (errorBodyString != null ? errorBodyString : "N/A"));

        String errorMessage = "Erro desconhecido ao excluir conta (Código: " + response.code() + ")";
        Gson gson = new Gson();

        if (errorBodyString != null && !errorBodyString.trim().isEmpty()) {
            try {
                ApiResponse errorResponse = gson.fromJson(errorBodyString, ApiResponse.class);
                if (errorResponse != null && errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty()) {
                    errorMessage = errorResponse.getMessage();
                }
            } catch (JsonSyntaxException e) {
                Log.w(TAG, "Não foi possível parsear o JSON do corpo do erro (exclusão): " + e.getMessage());
            }
        }

        switch (response.code()) {
            case 400:
                if (errorMessage.startsWith("Erro desconhecido")) errorMessage = "Requisição inválida.";
                break;
            case 404: // Not Found - Este é o erro que você está recebendo!
                // -> Provável causa: Endpoint na AuthService está ERRADO ou o TOKEN é inválido/expirado
                // e o backend retorna 404 em vez de 401/403 para este caso.
                if (errorMessage.startsWith("Erro desconhecido")) {
                    errorMessage = "Usuário ou endpoint de exclusão não encontrado.";
                }
                break;
            case 409: // Conflict
                if (errorMessage.startsWith("Erro desconhecido")) errorMessage = "Não foi possível excluir a conta devido a um conflito.";
                break;
            case 500: // Internal Server Error
                if (errorMessage.startsWith("Erro desconhecido")) errorMessage = "Erro interno no servidor. Tente mais tarde.";
                break;
            default:
                break;
        }
        showToast(errorMessage);
    }

    private void handleNetworkError(Throwable t) {
        Log.e(TAG, "Falha na rede ao tentar excluir conta", t);
        String errorMessage = "Falha na conexão ao tentar excluir conta";

        if (t instanceof java.net.SocketTimeoutException) {
            errorMessage = "Tempo limite de conexão esgotado. Verifique sua internet.";
        } else if (t instanceof java.net.ConnectException) {
            errorMessage = "Não foi possível conectar ao servidor.";
        } else if (t.getMessage() != null) {
            errorMessage = "Erro de rede: " + t.getMessage();
        }

        showToast(errorMessage + " Tente novamente.");
    }

    // Método para redirecionar para LoginActivity
    private void redirectToLogin(String messageToLoginScreen) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (messageToLoginScreen != null && !messageToLoginScreen.isEmpty()) {
            // Opcional: Se LoginActivity estiver preparada para receber e mostrar essa mensagem
            intent.putExtra("redirectMessage", messageToLoginScreen);
        }
        startActivity(intent);
        finishAffinity();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    // Opcional: Método para carregar dados completos do perfil via API
    // Útil se você quiser garantir que os dados exibidos na Central estejam sempre atualizados
    // (por exemplo, se o usuário editar o perfil na ProfileActivity e voltar para a Central).
    // private void loadUserProfileDataFromApi() {
    //    String token = getJwtToken();
    //    if (token == null) {
    //        Log.w(TAG, "Token nulo ao tentar carregar perfil via API na CentralActivity.");
    //        return;
    //    }
    //    // Assumindo que AuthService tem getCurrentUserProfile e que UserProfileResponse tem getNome/getEmail
    //    authService.getCurrentUserProfile("Bearer " + token).enqueue(new Callback<UserProfileResponse>() {
    //        @Override
    //        public void onResponse(@NonNull Call<UserProfileResponse> call, @NonNull Response<UserProfileResponse> response) {
    //            if (response.isSuccessful() && response.body() != null) {
    //                UserProfileResponse profile = response.body();
    //                if (textNomeUsuario != null && profile.getNome() != null) {
    //                     textNomeUsuario.setText("Olá, " + profile.getNome() + "!");
    //                }
    //                if (textEmailUsuario != null && profile.getEmail() != null) {
    //                    textEmailUsuario.setText(profile.getEmail());
    //                }
    //                Log.d(TAG, "Dados do perfil atualizados na Central via API.");
    //            } else {
    //                Log.e(TAG, "Erro ao carregar perfil via API (" + response.code() + ")");
    //                // Lidar com 401/403: clearAuthData() e redirectToLogin()
    //            }
    //        }
    //        @Override
    //        public void onFailure(@NonNull Call<UserProfileResponse> call, @NonNull Throwable t) {
    //             Log.e(TAG, "Falha na rede ao carregar perfil via API", t);
    //             // Tratar erro de rede
    //        }
    //    });
    // }

}
