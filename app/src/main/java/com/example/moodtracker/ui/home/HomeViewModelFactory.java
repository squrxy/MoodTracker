package com.example.moodtracker.ui.home;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.moodtracker.data.MoodRepository;

public class HomeViewModelFactory implements ViewModelProvider.Factory {

    private final Context appContext;

    public HomeViewModelFactory(@NonNull Context context) {
        this.appContext = context.getApplicationContext();
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return modelClass.cast(new HomeViewModel(new MoodRepository(appContext)));
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
