package com.example.meuapp.api;

import com.example.meuapp.model.LoginRequest;
import com.example.meuapp.model.LoginResponse;
import com.example.meuapp.model.ApiResponse;
import com.example.meuapp.model.User;
import com.example.meuapp.model.UpdateUserRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.DELETE;
import retrofit2.http.PUT;
import retrofit2.http.Header;
public interface AuthService {
    @POST("/api/auth/cadastro")
    Call<ApiResponse> registerUser(@Body User user);

    @POST("/api/auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    @DELETE("/api/auth/delete-account")
    Call<ApiResponse> deleteAccount(@Header("Authorization") String authHeader);

    @PUT("/api/auth/update-account")
    Call<ApiResponse> updateAccount(
            @Header("Authorization") String authHeader,
            @Body UpdateUserRequest request
    );
}