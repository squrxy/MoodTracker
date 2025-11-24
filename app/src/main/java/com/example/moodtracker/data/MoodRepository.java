package com.example.moodtracker.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.moodtracker.auth.SessionManager;
import com.example.moodtracker.net.ApiClient;
import com.example.moodtracker.net.ApiService;
import com.example.moodtracker.net.dto.MoodDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Репозиторий, который отдаёт реальные записи настроения либо из API, либо из локального кеша.
 */
public class MoodRepository {

    private final ApiService api;
    private final SessionManager sessionManager;
    private final List<MoodDto> cache = new ArrayList<>();

    public MoodRepository(@NonNull Context context) {
        sessionManager = new SessionManager(context);
        api = ApiClient.get().create(ApiService.class);
    }

    public void loadMoods(@NonNull MutableLiveData<MoodResult> target) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            target.postValue(MoodResult.error("No user session", snapshotCache()));
            return;
        }

        target.postValue(MoodResult.loading(snapshotCache()));
        api.getMoods(userId).enqueue(new Callback<List<MoodDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<MoodDto>> call,
                                   @NonNull Response<List<MoodDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cache.clear();
                    cache.addAll(response.body());
                    target.postValue(MoodResult.success(snapshotCache(), false));
                } else if (!cache.isEmpty()) {
                    target.postValue(MoodResult.success(snapshotCache(), true));
                } else {
                    target.postValue(MoodResult.error("Failed to load moods", snapshotCache()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MoodDto>> call,
                                  @NonNull Throwable t) {
                if (!cache.isEmpty()) {
                    target.postValue(MoodResult.success(snapshotCache(), true));
                } else {
                    target.postValue(MoodResult.error(t.getMessage(), snapshotCache()));
                }
            }
        });
    }

    @NonNull
    private List<MoodDto> snapshotCache() {
        return Collections.unmodifiableList(new ArrayList<>(cache));
    }
}
