package com.example.meuapp;

// Importações padrão do seu código
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// --- Novas importações para a funcionalidade de exclusão ---
import android.app.AlertDialog; // Para o diálogo de confirmação
import android.app.ProgressDialog; // Para o diálogo de progresso
import android.content.Context; // Para usar SharedPreferences
import android.content.DialogInterface; // Para o listener do diálogo
import android.content.SharedPreferences; // Para obter/limpar o token
import android.util.Log; // Para logs de debug
import android.view.View; // Para o OnClickListener
import android.widget.Toast; // Para mensagens Toast

// --- Importações da API e Retrofit ---
import com.example.meuapp.api.ApiClient;
import com.example.meuapp.api.AuthService; // Assumindo que deleteAccount está aqui
import com.example.meuapp.model.ApiResponse; // Assumindo que a resposta usa ApiResponse

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
// ---------------------------------------------------------


public class CentralActivity extends AppCompatActivity {

    private TextView textQualSeuDestino, textEditarPerfil, textExcluirSuaConta, textSairDaConta;
    private ImageButton imageButtonSair;

    // --- Novas variáveis para a funcionalidade de exclusão ---
    private AuthService authService; // Serviço para chamadas API
    private ProgressDialog progressDialog; // Diálogo de progresso
    private static final String PREFS_NAME = "AuthPrefs"; // Nome do arquivo SharedPreferences
    private static final String TOKEN_KEY = "jwt_token"; // Chave para o token
    // -------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_central);

        // Initialize components
        initComponents();

        // --- Inicializar serviço API e diálogo de progresso ---
        authService = ApiClient.getClient().create(AuthService.class); // Inicializa o serviço Retrofit
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Processando..."); // Mensagem inicial, será alterada para exclusão
        progressDialog.setCancelable(false); // Não permitir cancelar
        // ----------------------------------------------------


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

        // --- MODIFICADO: textExcluirSuaConta AGORA mostra o diálogo de confirmação ---
        textExcluirSuaConta.setOnClickListener(v ->
                showDeleteConfirmationDialog()); // Chama o método do diálogo
        // --------------------------------------------------------------------------

        // Lógica original para "Sair da Conta" e ImageButton Sair
        textSairDaConta.setOnClickListener(v -> {
            clearJwtToken(); // Limpa o token ao "sair"
            redirectToLogin(); // Redireciona para a tela de login
        });

        imageButtonSair.setOnClickListener(v -> finish()); // Fecha a activity
    }

    // --- NOVOS MÉTODOS PARA A FUNCIONALIDADE DE EXCLUSÃO ---

    // Método para mostrar o dialog de confirmação
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão") // Título do dialog
                .setMessage("Tem certeza que deseja excluir sua conta? Esta ação não pode ser desfeita.") // Mensagem de confirmação
                .setPositiveButton("Excluir", new DialogInterface.OnClickListener() { // Botão Positivo (Excluir)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.setMessage("Excluindo conta..."); // Muda a mensagem do progresso
                        deleteAccount(); // Chama a função para excluir a conta se o usuário confirmar
                    }
                })
                .setNegativeButton("Cancelar", null) // Botão Negativo (Cancelar), null dismiss o dialog
                .setIcon(android.R.drawable.ic_dialog_alert) // Ícone de alerta (opcional)
                .show(); // Mostra o dialog
    }

    // Método para obter o Token JWT armazenado (copiado do exemplo anterior)
    private String getJwtToken() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(TOKEN_KEY, null); // Retorna o token ou null se não existir
    }

    // Método para limpar o Token JWT armazenado localmente (copiado do exemplo anterior)
    private void clearJwtToken() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(TOKEN_KEY);
        editor.apply(); // Use apply para salvar assincronamente
    }

    // Método que faz a chamada para a API de exclusão (copiado do exemplo anterior)
    private void deleteAccount() {
        String token = getJwtToken(); // Obtém o token armazenado

        if (token == null) {
            showToast("Erro: Token de autenticação não encontrado. Faça login novamente.");
            redirectToLogin();
            return;
        }

        String authHeader = "Bearer " + token;

        progressDialog.show(); // Mostra o dialog de progresso

        authService.deleteAccount(authHeader).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressDialog.dismiss(); // Esconde o dialog de progresso

                if (response.isSuccessful()) {
                    showToast("Conta excluída com sucesso!");
                    clearJwtToken(); // Limpa o token armazenado localmente
                    redirectToLogin(); // Redireciona para a tela de login
                } else {
                    // Lidar com erros do backend (ex: 401, 404, 500)
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Erro desconhecido";
                        Log.e("CentralActivity", "Erro ao excluir conta: " + response.code() + " - " + errorBody);

                        String errorMessage = "Erro ao excluir conta. Código: " + response.code();
                        if (response.code() == 401 || response.code() == 403) {
                            errorMessage = "Sessão expirada ou não autorizada. Faça login novamente.";
                            clearJwtToken(); // Limpa o token inválido
                            redirectToLogin(); // Redireciona para login
                        } else if (response.code() == 404) {
                            errorMessage = "Usuário não encontrado no servidor."; // Raro, mas possível
                        } else {
                            // Lógica similar para tentar obter mensagem do corpo do erro se ApiResponse puder
                            errorMessage = "Erro no servidor ao excluir."; // Fallback
                        }

                        showToast(errorMessage);

                    } catch (Exception e) {
                        Log.e("CentralActivity", "Erro ao ler errorBody ou processar erro", e);
                        showToast("Erro ao processar resposta do servidor.");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressDialog.dismiss(); // Esconde o dialog de progresso
                Log.e("CentralActivity", "Falha na requisição de exclusão", t);

                String errorMessage = "Falha na conexão ao excluir conta. Tente novamente.";
                if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "Tempo limite de conexão esgotado. Verifique sua internet.";
                } else if (t instanceof java.net.ConnectException) {
                    errorMessage = "Não foi possível conectar ao servidor.";
                }
                showToast(errorMessage);
            }
        });
    }

    // Método para redirecionar para a tela de login (copiado do exemplo anterior)
    private void redirectToLogin() {
        Intent intent = new Intent(CentralActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpa a pilha de activities
        startActivity(intent);
        finish(); // Finaliza a CentralActivity
    }

    // Método auxiliar para mostrar Toast (copiado do exemplo anterior)
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Garante que o diálogo de progresso seja fechado quando a Activity for destruída
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    // -----------------------------------------------------------
}