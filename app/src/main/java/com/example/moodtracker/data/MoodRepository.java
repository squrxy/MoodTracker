package com.example.moodtracker.data;

import androidx.annotation.NonNull;

import com.example.moodtracker.net.ApiService;
import com.example.moodtracker.net.dto.MoodDto;
import com.example.moodtracker.net.dto.SimpleResponse;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoodRepository {

    public interface RepositoryCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    private final ApiService api;

    public MoodRepository(ApiService api) {
        this.api = api;
    }

    public void loadMoods(String userId, RepositoryCallback<List<MoodDto>> callback) {
        api.getMoods(userId).enqueue(new Callback<List<MoodDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<MoodDto>> call,
                                   @NonNull Response<List<MoodDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load moods: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MoodDto>> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createMood(String userId, int emotionId, String note,
                           RepositoryCallback<MoodDto> callback) {
        api.createMood(userId, emotionId, note).enqueue(new Callback<MoodDto>() {
            @Override
            public void onResponse(@NonNull Call<MoodDto> call,
                                   @NonNull Response<MoodDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(readErrorBody(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<MoodDto> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateMood(long id, String userId, int emotionId, String note,
                           RepositoryCallback<MoodDto> callback) {
        api.updateMood(id, userId, emotionId, note).enqueue(new Callback<MoodDto>() {
            @Override
            public void onResponse(@NonNull Call<MoodDto> call,
                                   @NonNull Response<MoodDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(readErrorBody(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<MoodDto> call, @NonNull Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteMood(long id, String userId, RepositoryCallback<Boolean> callback) {
        api.deleteMood(id, userId).enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(@NonNull Call<SimpleResponse> call,
                                   @NonNull Response<SimpleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body().success);
                } else {
                    callback.onError("Failed to delete mood: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<SimpleResponse> call, @NonNull Throwable t) {
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
