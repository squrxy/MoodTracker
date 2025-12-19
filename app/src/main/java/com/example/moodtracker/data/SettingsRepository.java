package com.example.moodtracker.data;

import androidx.annotation.NonNull;

import com.example.moodtracker.net.ApiService;
import com.example.moodtracker.net.dto.SettingsDto;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsRepository {

    public interface SettingsCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    private final ApiService api;

    public SettingsRepository(ApiService api) {
        this.api = api;
    }

    public void loadSettings(String userId, SettingsCallback<SettingsDto> callback) {
        api.getSettings(userId).enqueue(new Callback<SettingsDto>() {
            @Override
            public void onResponse(@NonNull Call<SettingsDto> call,
                                   @NonNull Response<SettingsDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(readErrorBody(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<SettingsDto> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateSettings(String userId,
                               boolean notificationsEnabled,
                               String notifyTime,
                               String defaultPeriod,
                               String displayName,
                               String about,
                               SettingsCallback<SettingsDto> callback) {
        api.updateSettings(userId, notificationsEnabled ? 1 : 0, notifyTime, defaultPeriod, displayName, about)
                .enqueue(new Callback<SettingsDto>() {
                    @Override
                    public void onResponse(@NonNull Call<SettingsDto> call,
                                           @NonNull Response<SettingsDto> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError(readErrorBody(response));
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<SettingsDto> call, @NonNull Throwable t) {
                        callback.onError(t.getMessage());
                    }
                });
    }

    private String readErrorBody(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                return response.errorBody().string();
            }
        } catch (IOException ignored) { }
        return "Unknown error";
    }
}
