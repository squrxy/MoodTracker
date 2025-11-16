package com.example.moodtracker.net;

import com.example.moodtracker.net.dto.AuthResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    @FormUrlEncoded
    @POST("auth/login.php")
    Call<AuthResponse> login(@Field("email") String email,
                             @Field("password") String password);

    @FormUrlEncoded
    @POST("auth/register.php")
    Call<AuthResponse> register(@Field("username") String username,
                                @Field("email") String email,
                                @Field("password") String password);

    @POST("auth/guest.php")
    Call<AuthResponse> guest();
}
