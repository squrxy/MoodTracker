package com.example.moodtracker.net;

import com.example.moodtracker.net.dto.AuthResponse;
import com.example.moodtracker.net.dto.MoodDto;
import com.example.moodtracker.net.dto.SettingsDto;
import com.example.moodtracker.net.dto.SimpleResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    // ---------- AUTH ----------
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

    // ---------- MOODS ----------

    @FormUrlEncoded
    @POST("moods/list.php")
    Call<List<MoodDto>> getMoods(@Field("user_id") String userId);

    @FormUrlEncoded
    @POST("moods/create.php")
    Call<MoodDto> createMood(@Field("user_id") String userId,
                             @Field("emotion_id") int emotionId,
                             @Field("note") String note);

    @FormUrlEncoded
    @POST("moods/update.php")
    Call<MoodDto> updateMood(@Field("id") long id,
                             @Field("user_id") String userId,
                             @Field("emotion_id") int emotionId,
                             @Field("note") String note);

    @FormUrlEncoded
    @POST("moods/delete.php")
    Call<SimpleResponse> deleteMood(@Field("id") long id,
                                    @Field("user_id") String userId);

    // ---------- SETTINGS ----------

    @FormUrlEncoded
    @POST("settings/get.php")
    Call<SettingsDto> getSettings(@Field("user_id") String userId);

    @FormUrlEncoded
    @POST("settings/update.php")
    Call<SettingsDto> updateSettings(@Field("user_id") String userId,
                                     @Field("notifications_enabled") int notificationsEnabled,
                                     @Field("notify_time") String notifyTime,
                                     @Field("default_period") String defaultPeriod,
                                     @Field("display_name") String displayName,
                                     @Field("about") String about);
}
