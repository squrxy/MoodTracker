package com.example.moodtracker.net;

import com.example.moodtracker.net.dto.AuthResponse;
import com.example.moodtracker.net.dto.LoginRequest;
import com.example.moodtracker.net.dto.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("auth/login")    Call<AuthResponse> login(@Body LoginRequest body);
    @POST("auth/register") Call<AuthResponse> register(@Body RegisterRequest body);
    @POST("auth/guest")    Call<AuthResponse> guest(); // или GET — под твой API
}
