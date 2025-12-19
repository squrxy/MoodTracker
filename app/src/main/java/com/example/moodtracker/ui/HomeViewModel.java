package com.example.moodtracker.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.moodtracker.auth.SessionManager;
import com.example.moodtracker.data.MoodRepository;
import com.example.moodtracker.net.dto.MoodDto;
import com.example.moodtracker.ui.state.MoodStats;
import com.example.moodtracker.ui.state.UiState;

import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MoodRepository repository;
    private final SessionManager sessionManager;

    private final MutableLiveData<UiState<MoodStats>> statsState =
            new MutableLiveData<>(UiState.idle(MoodStats.empty()));

    public HomeViewModel(@NonNull MoodRepository repository,
                         @NonNull SessionManager sessionManager) {
        this.repository = repository;
        this.sessionManager = sessionManager;
    }

    public LiveData<UiState<MoodStats>> getStatsState() {
        return statsState;
    }

    public void loadStats() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            statsState.setValue(UiState.error(null, "No user session"));
            return;
        }
        statsState.setValue(UiState.loading(currentStats()));
        repository.loadMoods(userId, new MoodRepository.RepositoryCallback<List<MoodDto>>() {
            @Override
            public void onSuccess(List<MoodDto> data) {
                statsState.postValue(UiState.success(MoodStats.from(data)));
            }

            @Override
            public void onError(String message) {
                statsState.postValue(UiState.error(currentStats(), message));
            }
        });
    }

    private MoodStats currentStats() {
        UiState<MoodStats> state = statsState.getValue();
        if (state != null && state.getData() != null) return state.getData();
        return MoodStats.empty();
    }
}
