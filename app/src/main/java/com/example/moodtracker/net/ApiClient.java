package com.example.moodtracker.net;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
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

            // Интерцептор, который добавляет Connection: close
            Interceptor connectionCloseInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request newReq = chain.request().newBuilder()
                            .header("Connection", "close")
                            .build();
                    return chain.proceed(newReq);
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(connectionCloseInterceptor)
                    .addInterceptor(log)                  // можно убрать, если не нужны логи
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    // Явно фиксируем HTTP/1.1 – OpenServer любит так больше
                    .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
