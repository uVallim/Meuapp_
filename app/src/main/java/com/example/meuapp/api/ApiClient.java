package com.example.meuapp.api;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Configurar o OkHttpClient para aumentar o timeout
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)  // Tempo para estabelecer a conexão
                    .writeTimeout(60, TimeUnit.SECONDS)    // Tempo de escrita
                    .readTimeout(60, TimeUnit.SECONDS)     // Tempo de leitura
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://r3l2g7-3001.csb.app/")  // Base URL
                    .client(okHttpClient)  // Passa o OkHttpClient customizado
                    .addConverterFactory(GsonConverterFactory.create())  // Conversão de JSON para objetos Java
                    .build();
        }
        return retrofit;
    }
}
