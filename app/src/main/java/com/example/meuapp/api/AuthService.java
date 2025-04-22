package com.example.meuapp.api;

import com.example.meuapp.model.LoginRequest;
import com.example.meuapp.model.LoginResponse;
import com.example.meuapp.model.ApiResponse;
import com.example.meuapp.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {
    @POST("/api/auth/Cadastro")
    Call<ApiResponse> registerUser(@Body User user);

    @POST("/api/auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);
}