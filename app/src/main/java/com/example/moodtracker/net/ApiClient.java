package com.example.moodtracker.net;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static Retrofit retrofit;

    // ЭМУЛЯТОР:
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";
    // РЕАЛЬНЫЙ ТЕЛЕФОН (когда тестируешь его):
    // private static final String BASE_URL = "http://192.168.1.14:8080/api/";

    public static Retrofit get() {
        if (retrofit == null) {

            // Логирование (по желанию, удобно для отладки)
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BODY);

            Interceptor acceptJsonInterceptor = chain -> {
                Request request = chain.request().newBuilder()
                        .header("Accept", "application/json")
                        .build();
                return chain.proceed(request);
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(acceptJsonInterceptor)
                    .addInterceptor(log)                  // можно убрать, если не нужны логи
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    // Явно фиксируем HTTP/1.1 – OpenServer любит так больше
                    .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient() // сервер иногда отдаёт строки/HTML при ошибках
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
