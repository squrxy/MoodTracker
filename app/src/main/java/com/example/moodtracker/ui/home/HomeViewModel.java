package com.example.moodtracker.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.moodtracker.data.MoodRepository;
import com.example.moodtracker.data.MoodResult;

public class HomeViewModel extends ViewModel {

    private final MoodRepository repository;
    private final MutableLiveData<MoodResult> moodState = new MutableLiveData<>();

    public HomeViewModel(@NonNull MoodRepository repository) {
        this.repository = repository;
    }

    public LiveData<MoodResult> getMoodState() {
        return moodState;
    }

    public void loadMoods() {
        repository.loadMoods(moodState);
    }
}
