package com.example.moodtracker.ui.diary;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.moodtracker.auth.SessionManager;
import com.example.moodtracker.data.MoodRepository;

public class DiaryViewModelFactory implements ViewModelProvider.Factory {

    private final MoodRepository repository;
    private final SessionManager sessionManager;

    public DiaryViewModelFactory(@NonNull MoodRepository repository,
                                 @NonNull SessionManager sessionManager) {
        this.repository = repository;
        this.sessionManager = sessionManager;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DiaryViewModel.class)) {
            return modelClass.cast(new DiaryViewModel(repository, sessionManager));
        }
        throw new IllegalArgumentException("Unknown ViewModel class " + modelClass.getName());
    }
}
