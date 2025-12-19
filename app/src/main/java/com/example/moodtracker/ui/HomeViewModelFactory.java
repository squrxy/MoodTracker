package com.example.moodtracker.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.moodtracker.auth.SessionManager;
import com.example.moodtracker.data.MoodRepository;

public class HomeViewModelFactory implements ViewModelProvider.Factory {

    private final MoodRepository repository;
    private final SessionManager sessionManager;

    public HomeViewModelFactory(@NonNull MoodRepository repository,
                                @NonNull SessionManager sessionManager) {
        this.repository = repository;
        this.sessionManager = sessionManager;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return modelClass.cast(new HomeViewModel(repository, sessionManager));
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
