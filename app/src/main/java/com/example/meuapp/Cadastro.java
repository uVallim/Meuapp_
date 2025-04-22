package com.example.meuapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.meuapp.api.ApiClient;
import com.example.meuapp.api.AuthService;
import com.example.meuapp.model.User;
import com.example.meuapp.model.ApiResponse;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Cadastro extends AppCompatActivity {
    private EditText editTextName, editTextEmail, editTextPhone,
            editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textViewLoginLink;
    private AuthService authService;
    private ProgressDialog progressDialog;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         // Pelo menos 1 dígito
                    "(?=.*[a-zA-Z])" +      // Pelo menos 1 letra
                    "(?=.*[!@#$%^&*])" +    // Pelo menos 1 caractere especial
                    "(?=\\S+$)" +          // Sem espaços em branco
                    ".{8,}" +               // Pelo menos 8 caracteres
                    "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        // Inicialização dos componentes
        initViews();
        setupClickListeners();

        authService = ApiClient.getClient().create(AuthService.class);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cadastrando...");
        progressDialog.setCancelable(false);
    }

    private void initViews() {
        editTextName = findViewById(R.id.DigiteSeuNome);
        editTextEmail = findViewById(R.id.DigiteSeuEmail);
        editTextPhone = findViewById(R.id.digiteSeuTelefone);
        editTextPassword = findViewById(R.id.DigiteSuaSenha);
        editTextConfirmPassword = findViewById(R.id.ConfimeSuaSenha);
        buttonRegister = findViewById(R.id.BotaoLogin);
        textViewLoginLink = findViewById(R.id.TextLogin);
    }

    private void setupClickListeners() {
        // Redirecionamento para login
        textViewLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(Cadastro.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }
    // Cifra de César simples (criptografa)
    private String cifraDeCesar(String input, int chave) {
        StringBuilder resultado = new StringBuilder();
        for (char c : input.toCharArray()) {
            resultado.append((char) (c + chave));
        }
        return resultado.toString();
    }

    private boolean isPhoneValid(String phone) {
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        return digitsOnly.length() >= 10 && digitsOnly.length() <= 11;
    }

    private void registerUser() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Validações dos campos
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Preencha todos os campos");
            return;
        }

        if (!isEmailValid(email)) {
            showToast("Por favor, insira um e-mail válido");
            return;
        }

        if (!isPhoneValid(phone)) {
            showToast("Por favor, insira um telefone válido com DDD");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showToast("As senhas não coincidem");
            return;
        }

        if (!isPasswordValid(password)) {
            showToast("A senha deve ter pelo menos 8 caracteres, incluindo letras, números e caracteres especiais (!@#$%^&*)");
            return;
        }

        // Mostra o dialog de progresso
        progressDialog.show();
        // Criptografando a senha com cifra de César antes de enviar
        String senhaCriptografada = cifraDeCesar(password, 3);
        //User user = new User(fullName, email, password, "passenger", phone);

        User user = new User(name, email, senhaCriptografada, "passenger", phone);
        Log.d("Cadastro", "Método signUpUser() chamado");
        // Log para depuração: verificar os dados que estamos enviando
        Log.d("Cadastro", "Enviando dados de cadastro: " + user.toString());
        // Cria o objeto User
        //User user = new User(name, email, password, "passenger", phone);

        // Log para depuração
        Log.d("Cadastro", "Dados de cadastro: " + user.toString());

        // Faz a chamada à API
        authService.registerUser(user).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressDialog.dismiss();

                if (response.isSuccessful()) {
                    handleSuccessResponse(response);
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressDialog.dismiss();
                handleFailure(t);
            }
        });
    }

    private void handleSuccessResponse(Response<ApiResponse> response) {
        ApiResponse apiResponse = response.body();
        if (apiResponse != null && apiResponse.isSuccess()) {
            showToast("Cadastro realizado com sucesso!");
            redirectToLogin();
        } else {
            String errorMsg = apiResponse != null ? apiResponse.getMessage() : "Resposta vazia do servidor";
            Log.e("Cadastro", "Erro no cadastro: " + errorMsg);
            showToast(errorMsg);
        }
    }

    private void handleErrorResponse(Response<ApiResponse> response) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Sem corpo de erro";
            Log.e("Cadastro", "Erro HTTP: " + response.code() + " - " + errorBody);

            switch (response.code()) {
                case 400:
                    showToast("Dados inválidos enviados");
                    break;
                case 409:
                    showToast("E-mail já cadastrado");
                    break;
                case 500:
                    showToast("Erro interno no servidor");
                    break;
                default:
                    showToast("Erro no servidor: " + response.code());
            }
        } catch (IOException e) {
            Log.e("Cadastro", "Erro ao ler errorBody", e);
            showToast("Erro ao processar resposta");
        }
    }

    private void handleFailure(Throwable t) {
        Log.e("Cadastro", "Falha na requisição: " + t.getMessage(), t);

        String errorMessage;
        if (t instanceof SocketTimeoutException) {
            errorMessage = "Tempo de conexão esgotado. Verifique sua internet";
        } else if (t instanceof ConnectException) {
            errorMessage = "Não foi possível conectar ao servidor";
        } else {
            errorMessage = "Falha na conexão";
        }

        showToast(errorMessage + ". Tente novamente.");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(Cadastro.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}