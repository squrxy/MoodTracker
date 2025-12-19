package com.example.moodtracker.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.moodtracker.auth.SessionManager;
import com.example.moodtracker.data.SettingsRepository;
import com.example.moodtracker.net.dto.SettingsDto;
import com.example.moodtracker.ui.state.UiState;

public class SettingsViewModel extends ViewModel {

    private final SettingsRepository repository;
    private final SessionManager sessionManager;

    private final MutableLiveData<UiState<SettingsDto>> settingsState =
            new MutableLiveData<>(UiState.idle(null));

    public SettingsViewModel(@NonNull SettingsRepository repository,
                             @NonNull SessionManager sessionManager) {
        this.repository = repository;
        this.sessionManager = sessionManager;
    }

    public LiveData<UiState<SettingsDto>> getSettingsState() {
        return settingsState;
    }

    public void loadSettings() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            settingsState.setValue(UiState.error(null, "No user session"));
            return;
        }
        settingsState.setValue(UiState.loading(current()));
        repository.loadSettings(userId, new SettingsRepository.SettingsCallback<SettingsDto>() {
            @Override
            public void onSuccess(SettingsDto data) {
                settingsState.postValue(UiState.success(data));
            }

            @Override
            public void onError(String message) {
                settingsState.postValue(UiState.error(current(), message));
            }
        });
    }

    public void saveSettings(boolean notificationsEnabled, String notifyTime, String defaultPeriod) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            settingsState.setValue(UiState.error(current(), "No user session"));
            return;
        }
        settingsState.setValue(UiState.loading(current()));
        repository.updateSettings(userId, notificationsEnabled, notifyTime, defaultPeriod,
                new SettingsRepository.SettingsCallback<SettingsDto>() {
                    @Override
                    public void onSuccess(SettingsDto data) {
                        settingsState.postValue(UiState.success(data));
                    }

                    @Override
                    public void onError(String message) {
                        settingsState.postValue(UiState.error(current(), message));
                    }
                });
    }

    private SettingsDto current() {
        UiState<SettingsDto> state = settingsState.getValue();
        if (state != null) return state.getData();
        return null;
    }
}
