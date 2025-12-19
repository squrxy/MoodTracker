package com.example.moodtracker.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.moodtracker.auth.SessionManager;
import com.example.moodtracker.data.SettingsRepository;

public class SettingsViewModelFactory implements ViewModelProvider.Factory {

    private final SettingsRepository repository;
    private final SessionManager sessionManager;

    public SettingsViewModelFactory(SettingsRepository repository, SessionManager sessionManager) {
        this.repository = repository;
        this.sessionManager = sessionManager;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            //noinspection unchecked
            return (T) new SettingsViewModel(repository, sessionManager);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
