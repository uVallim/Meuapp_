package com.example.meuapp;

// Importações padrão da sua Activity
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

// --- Importações necessárias para a funcionalidade de API ---
import android.app.ProgressDialog; // Para o diálogo de progresso
import android.content.Context; // Para usar SharedPreferences
import android.content.Intent; // Para redirecionar (se necessário em caso de 401)
import android.util.Log; // Para logs de debug
import android.view.View; // Para o OnClickListener

// Importações da API e Modelos
import com.example.meuapp.api.ApiClient; // Para obter a instância do serviço API
import com.example.meuapp.api.AuthService; // Sua interface de serviço API
import com.example.meuapp.model.ApiResponse; // Assumindo resposta genérica do backend
import com.example.meuapp.model.UpdateUserRequest; // Sua classe de modelo para a requisição de atualização

import retrofit2.Call; // Retrofit Call
import retrofit2.Callback; // Retrofit Callback
import retrofit2.Response; // Retrofit Response
// ----------------------------------------------------------

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    // Declaração dos componentes da interface (usando os nomes do seu código)
    private EditText caixaNome, caixaEmail, caixaPassword;
    private Button botaoSalvar;

    // --- Variáveis necessárias para a funcionalidade de API ---
    private AuthService authService; // Instância do serviço API
    private ProgressDialog progressDialog; // Diálogo de progresso
    // Constantes para SharedPreferences (para o token JWT)
    private static final String PREFS_NAME = "AuthPrefs"; // Mesmo nome usado em Login/CentralActivity
    private static final String TOKEN_KEY = "jwt_token"; // Mesma chave usada em Login/CentralActivity
    // -------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // Layout da sua tela de perfil

        // Inicializando os elementos do layout
        initComponents(); // Encontra os EditTexts e Button

        // --- Inicializar serviço API e diálogo de progresso ---
        authService = ApiClient.getClient().create(AuthService.class); // Inicializa o serviço Retrofit
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Salvando alterações..."); // Mensagem do progresso
        progressDialog.setCancelable(false); // Não permitir cancelar
        // ----------------------------------------------------


        // TODO: Opcional: Carregar dados atuais do usuário para preencher os campos (exceto senha)
        // Isso geralmente exige um novo endpoint no backend (ex: GET /api/users/me com authMiddleware)
        // e uma chamada API aqui no onCreate para buscar os dados e preencher caixaNome e caixaEmail.
        // A lógica carregarPerfil() baseada em SharedPreferences NÃO é segura para senha e pode estar desatualizada para nome/email.
        // **Remova a chamada a carregarPerfil() baseada em SharedPreferences:**
        // carregarPerfil(); // <-- REMOVER ESTA LINHA

        // Adicionar funcionalidade ao botão Salvar
        botaoSalvar.setOnClickListener(view -> {
            // Lógica para salvar as alterações feitas
            // **Chame o novo método que usa a API em vez de salvarPerfil:**
            saveAccountChanges(); // <-- CHAMAR O NOVO MÉTODO AQUI

            // **Remova o Toast de sucesso local:**
            // Toast.makeText(ProfileActivity.this, "Informações salvas com sucesso!", Toast.LENGTH_SHORT).show(); // <-- REMOVER ESTA LINHA
        });

        // **Remova completamente os métodos carregarPerfil e salvarPerfil baseados em SharedPreferences.**
    }

    private void initComponents() {
        // Encontrando os elementos do layout pelos IDs
        caixaNome = findViewById(R.id.CaixaNome); // Verifique se os IDs batem com seu XML
        caixaEmail = findViewById(R.id.CaixaEmail); // Verifique se os IDs batem com seu XML
        caixaPassword = findViewById(R.id.CaixaPassword); // Campo para a NOVA senha. Verifique se o ID bate.
        botaoSalvar = findViewById(R.id.BotaoSalvar); // Verifique se o ID bate com seu XML
    }


    // --- NOVO MÉTODO QUE CHAMA A API PARA SALVAR AS ALTERAÇÕES ---
    private void saveAccountChanges() {
        // 1. Obter valores dos campos
        String novoNome = caixaNome.getText().toString().trim();
        String novoEmail = caixaEmail.getText().toString().trim();
        String novaSenha = caixaPassword.getText().toString().trim(); // Obter o texto da NOVA senha digitada

        // 2. Obter o token JWT armazenado
        String token = getJwtToken(); // Método para obter o token (copiado da CentralActivity)

        // Validar que o token existe
        if (token == null) {
            showToast("Sessão expirada. Faça login novamente.");
            // Opcional: Redirecionar para login, implemente redirectToLogin() se necessário
            // redirectToLogin();
            return;
        }

        // 3. Validar campos preenchidos localmente (opcional, mas recomendado antes de enviar)
        // Se o email foi preenchido, validar o formato
        if (!novoEmail.isEmpty() && !isEmailValid(novoEmail)) { // Implemente isEmailValid() se não tiver
            showToast("Formato do novo e-mail inválido.");
            return;
        }
        // Se a senha foi preenchida, validar a força/formato (use a mesma lógica de cadastro, mas para a nova senha)
        if (!novaSenha.isEmpty() /* && !isPasswordValid(novaSenha) */) { // Implemente isPasswordValid() se não tiver
            // showToast("A nova senha não atende aos requisitos.");
            // return;
        }


        // 4. Criar o objeto de requisição de atualização
        UpdateUserRequest request = new UpdateUserRequest();

        // 5. Definir APENAS os campos que foram preenchidos (não vazios)
        // Retrofit/Gson, por padrão, não incluirão campos que não foram definidos (serão null) no JSON
        if (!novoNome.isEmpty()) {
            request.setNome(novoNome);
        }
        if (!novoEmail.isEmpty()) {
            request.setEmail(novoEmail);
        }
        if (!novaSenha.isEmpty()) {
            // TODO: Validar novaSenha localmente antes de definir se usar validação complexa
            request.setSenha(novaSenha); // Envia a nova senha em texto plano, o backend A CRIPTOGRAFA
        }

        // Verificar se pelo menos um campo foi definido na requisição
        if (request.getNome() == null && request.getEmail() == null && request.getSenha() == null) {
            showToast("Preencha os campos que deseja alterar.");
            return;
        }


        // 6. Fazer a chamada para a API usando Retrofit
        String authHeader = "Bearer " + token; // Formato do cabeçalho de autorização
        progressDialog.show(); // Mostrar progresso

        authService.updateAccount(authHeader, request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressDialog.dismiss(); // Esconder progresso

                if (response.isSuccessful()) {
                    showToast("Informações atualizadas com sucesso!");
                    // Opcional: Limpar o campo de senha após sucesso
                    caixaPassword.setText("");
                    // TODO: Se o email foi alterado, o token JWT PODE precisar ser reemitido pelo backend
                    // na resposta de sucesso para manter o payload do token atualizado para futuras requisições.
                    // Se o backend não reemite, o cliente deve estar ciente que o email no token payload pode estar desatualizado.
                    // Opcional: Fechar a activity ou redirecionar
                    // finish();
                } else {
                    // 7. Lidar com erros do backend (400, 401, 409, 500, etc.)
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Erro desconhecido";
                        Log.e("ProfileActivity", "Erro ao atualizar conta: " + response.code() + " - " + errorBody);

                        String errorMessage = "Erro ao atualizar. Código: " + response.code();

                        if (response.code() == 400) {
                            errorMessage = "Dados inválidos fornecidos.";
                        } else if (response.code() == 401 || response.code() == 403) {
                            errorMessage = "Sessão expirada ou não autorizada. Faça login novamente.";
                            // TODO: Implementar clearJwtToken() e redirectToLogin() se quiser essa lógica aqui
                            // clearJwtToken();
                            // redirectToLogin();
                        } else if (response.code() == 409) {
                            errorMessage = "Este e-mail já está em uso por outro usuário.";
                        } else {
                            // Tentar obter mensagem de erro específica do corpo da resposta se a API retornar JSON de erro
                            // ApiResponse errorResponse = new Gson().fromJson(errorBody, ApiResponse.class); // Exemplo se usar Gson
                            // if (errorResponse != null && errorResponse.getMessage() != null) {
                            //     errorMessage = "Erro: " + errorResponse.getMessage();
                            // } else {
                            errorMessage = "Erro no servidor ao atualizar."; // Mensagem fallback
                            // }
                        }

                        showToast(errorMessage);

                    } catch (Exception e) {
                        Log.e("ProfileActivity", "Erro ao ler errorBody ou processar erro", e);
                        showToast("Erro ao processar resposta do servidor.");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressDialog.dismiss(); // Esconder progresso
                Log.e("ProfileActivity", "Falha na requisição de atualização", t);

                // 8. Lidar com erros de conexão (timeout, sem internet, etc.)
                String errorMessage = "Falha na conexão ao atualizar informações. Tente novamente.";
                if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "Tempo limite de conexão esgotado. Verifique sua internet.";
                } else if (t instanceof java.net.ConnectException) {
                    errorMessage = "Não foi possível conectar ao servidor.";
                } else {
                    errorMessage = "Erro de rede: " + t.getMessage();
                }
                showToast(errorMessage);
            }
        });
    }

    // Método para obter o Token JWT (Copie/adapte da CentralActivity ou LoginActivity)
    private String getJwtToken() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(TOKEN_KEY, null);
    }

    // Método auxiliar para mostrar Toast (Copie/adapte da CentralActivity ou LoginActivity)
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // TODO: Implementar isEmailValid() e isPasswordValid() se quiser validação local aqui.
    // TODO: Implementar clearJwtToken() e redirectToLogin() se quiser que erros 401/403 nesta tela
    // levem o usuário de volta ao login limpando o token.

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Garante que o diálogo de progresso seja fechado quando a Activity for destruída
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    // **REMOVA COMPLETAMENTE ESTES MÉTODOS SEGUINTES, ELES NÃO SÃO NECESSÁRIOS COM A API:**
    /*
    // Método para carregar as informações salvas (NÃO USE COM API PARA CARREGAR PERFIL)
    private void carregarPerfil() {
        SharedPreferences sharedPreferences = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
        String nome = sharedPreferences.getString("nome", "");
        String email = sharedPreferences.getString("email", "");
        String senha = sharedPreferences.getString("senha", ""); // Não salve nem carregue senha assim!

        caixaNome.setText(nome);
        caixaEmail.setText(email);
        caixaPassword.setText(senha); // NÃO PREENCHA CAMPO DE SENHA COM SENHA SALVA!
    }

    // Método para salvar as informações alteradas (NÃO USE COM API PARA SALVAR PERFIL)
    private void salvarPerfil(String nome, String email, String senha) {
        SharedPreferences sharedPreferences = getSharedPreferences("UsuarioPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("nome", nome);
        editor.putString("email", email);
        editor.putString("senha", senha); // Não salve senha assim!

        editor.apply();
    }
    */
    // -----------------------------------------------------------------------------
}