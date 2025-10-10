package com.example.moodtracker.net;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static Retrofit retrofit;

    // Если ты на реальном устройстве — поменяй на IP своего ПК
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";

    public static Retrofit get() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
