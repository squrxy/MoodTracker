package com.example.moodtracker.ui.diary;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.moodtracker.auth.SessionManager;
import com.example.moodtracker.data.MoodRepository;
import com.example.moodtracker.net.dto.MoodDto;
import com.example.moodtracker.ui.state.UiState;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiaryViewModel extends ViewModel {

    private final MoodRepository repository;
    private final SessionManager sessionManager;

    private final MutableLiveData<UiState<List<DiaryEntry>>> moodsState =
            new MutableLiveData<>(UiState.idle(Collections.emptyList()));

    private final SimpleDateFormat serverFmt =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private final SimpleDateFormat displayFmt =
            new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());

    public DiaryViewModel(@NonNull MoodRepository repository,
                          @NonNull SessionManager sessionManager) {
        this.repository = repository;
        this.sessionManager = sessionManager;
    }

    public LiveData<UiState<List<DiaryEntry>>> getMoodsState() {
        return moodsState;
    }

    public void loadMoods() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            moodsState.setValue(UiState.error(Collections.emptyList(), "No user session"));
            return;
        }

        moodsState.setValue(UiState.loading(currentData()));
        repository.loadMoods(userId, new MoodRepository.RepositoryCallback<List<MoodDto>>() {
            @Override
            public void onSuccess(List<MoodDto> data) {
                List<DiaryEntry> mapped = mapDtosToEntries(data);
                moodsState.postValue(UiState.success(mapped));
            }

            @Override
            public void onError(String message) {
                moodsState.postValue(UiState.error(currentData(), message));
            }
        });
    }

    public void createMood(int emotionId, String note) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            moodsState.setValue(UiState.error(currentData(), "No user session"));
            return;
        }
        moodsState.setValue(UiState.loading(currentData()));
        repository.createMood(userId, emotionId, note, new MoodRepository.RepositoryCallback<MoodDto>() {
            @Override
            public void onSuccess(MoodDto data) {
                List<DiaryEntry> updated = new ArrayList<>(currentData());
                updated.add(0, mapDtoToEntry(data));
                moodsState.postValue(UiState.success(updated));
            }

            @Override
            public void onError(String message) {
                moodsState.postValue(UiState.error(currentData(), message));
            }
        });
    }

    public void updateMood(long id, int emotionId, String note) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            moodsState.setValue(UiState.error(currentData(), "No user session"));
            return;
        }
        moodsState.setValue(UiState.loading(currentData()));
        repository.updateMood(id, userId, emotionId, note, new MoodRepository.RepositoryCallback<MoodDto>() {
            @Override
            public void onSuccess(MoodDto data) {
                List<DiaryEntry> updated = new ArrayList<>(currentData());
                for (int i = 0; i < updated.size(); i++) {
                    if (updated.get(i).id == data.id) {
                        updated.set(i, mapDtoToEntry(data));
                        break;
                    }
                }
                moodsState.postValue(UiState.success(updated));
            }

            @Override
            public void onError(String message) {
                moodsState.postValue(UiState.error(currentData(), message));
            }
        });
    }

    public void deleteMood(long id) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            moodsState.setValue(UiState.error(currentData(), "No user session"));
            return;
        }
        moodsState.setValue(UiState.loading(currentData()));
        repository.deleteMood(id, userId, new MoodRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                if (!Boolean.TRUE.equals(data)) {
                    moodsState.postValue(UiState.error(currentData(), "Delete failed"));
                    return;
                }
                List<DiaryEntry> updated = new ArrayList<>(currentData());
                for (int i = 0; i < updated.size(); i++) {
                    if (updated.get(i).id == id) {
                        updated.remove(i);
                        break;
                    }
                }
                moodsState.postValue(UiState.success(updated));
            }

            @Override
            public void onError(String message) {
                moodsState.postValue(UiState.error(currentData(), message));
            }
        });
    }

    private List<DiaryEntry> mapDtosToEntries(List<MoodDto> dtos) {
        List<DiaryEntry> list = new ArrayList<>();
        for (MoodDto dto : dtos) {
            list.add(mapDtoToEntry(dto));
        }
        return list;
    }

    private DiaryEntry mapDtoToEntry(MoodDto dto) {
        String dateText = dto.created_at;
        if (dateText != null) {
            try {
                Date d = serverFmt.parse(dto.created_at);
                if (d != null) dateText = displayFmt.format(d);
            } catch (ParseException ignored) { }
        }
        String emoji = dto.icon != null ? dto.icon : "😊";
        String title = dto.name != null ? dto.name : "Mood";
        String note = dto.note != null ? dto.note : "";

        return new DiaryEntry(
                dto.id,
                (int) dto.emotion_id,
                emoji,
                title,
                dateText != null ? dateText : "",
                note
        );
    }

    private List<DiaryEntry> currentData() {
        UiState<List<DiaryEntry>> state = moodsState.getValue();
        if (state == null || state.getData() == null) {
            return Collections.emptyList();
        }
        return state.getData();
    }
}
