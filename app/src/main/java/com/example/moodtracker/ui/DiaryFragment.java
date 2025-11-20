package com.example.moodtracker.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodtracker.R;
import com.example.moodtracker.auth.SessionManager;
import com.example.moodtracker.net.ApiClient;
import com.example.moodtracker.net.ApiService;
import com.example.moodtracker.net.dto.MoodDto;
import com.example.moodtracker.net.dto.SimpleResponse;
import com.example.moodtracker.ui.diary.DiaryAdapter;
import com.example.moodtracker.ui.diary.DiaryEntry;
import com.example.moodtracker.ui.diary.EditEntryDialogFragment;
import com.example.moodtracker.ui.diary.NewEntryDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiaryFragment extends Fragment
        implements NewEntryDialogFragment.OnNewEntryListener,
        EditEntryDialogFragment.OnEntryEditListener {

    private RecyclerView rvDiary;
    private View emptyState;
    private FloatingActionButton fabAdd;
    private DiaryAdapter adapter;

    private ApiService api;
    private SessionManager session;

    private final SimpleDateFormat serverFmt =
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private final SimpleDateFormat displayFmt =
            new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_diary, container, false);

        rvDiary = v.findViewById(R.id.rvDiary);
        emptyState = v.findViewById(R.id.emptyState);
        fabAdd = v.findViewById(R.id.fabAddEntry);

        rvDiary.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DiaryAdapter(entry -> {
            // по нажатию на запись открываем диалог просмотра/редактирования
            EditEntryDialogFragment dialog = EditEntryDialogFragment.newInstance(entry);
            dialog.show(getChildFragmentManager(), "EditEntryDialog");
        });
        rvDiary.setAdapter(adapter);

        session = new SessionManager(requireContext());
        api = ApiClient.get().create(ApiService.class);

        fabAdd.setOnClickListener(view -> {
            NewEntryDialogFragment dialog = NewEntryDialogFragment.newInstance();
            dialog.show(getChildFragmentManager(), "NewEntryDialog");
        });

        loadMoods();

        return v;
    }

    // ---------- Загрузка списка записей ----------

    private void loadMoods() {
        String userId = session.getUserId();
        if (userId == null) {
            emptyState.setVisibility(View.VISIBLE);
            rvDiary.setVisibility(View.GONE);
            return;
        }

        api.getMoods(userId).enqueue(new Callback<List<MoodDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<MoodDto>> call,
                                   @NonNull Response<List<MoodDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<DiaryEntry> list = new ArrayList<>();
                    for (MoodDto dto : response.body()) {
                        list.add(mapDtoToEntry(dto));
                    }
                    adapter.setItems(list);
                    updateEmptyState();
                } else {
                    Toast.makeText(requireContext(),
                            "Failed to load moods", Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MoodDto>> call,
                                  @NonNull Throwable t) {
                Toast.makeText(requireContext(),
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }

    private DiaryEntry mapDtoToEntry(MoodDto dto) {
        String dateText = dto.created_at;
        if (dateText != null) {
            try {
                Date d = serverFmt.parse(dto.created_at);
                if (d != null) dateText = displayFmt.format(d);
            } catch (ParseException ignored) {}
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

    private void updateEmptyState() {
        if (adapter.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvDiary.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvDiary.setVisibility(View.VISIBLE);
        }
    }

    // ---------- Создание новой записи ----------

    @Override
    public void onNewEntrySaved(int emotionId, String emoji, String title, String note) {
        String userId = session.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "No user session", Toast.LENGTH_SHORT).show();
            return;
        }

        api.createMood(userId, emotionId, note).enqueue(new Callback<MoodDto>() {
            @Override
            public void onResponse(@NonNull Call<MoodDto> call,
                                   @NonNull Response<MoodDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DiaryEntry entry = mapDtoToEntry(response.body());
                    adapter.addItem(entry);
                    updateEmptyState();
                    rvDiary.smoothScrollToPosition(0);
                } else {
                    String err = "";
                    try {
                        if (response.errorBody() != null) {
                            err = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}

                    Toast.makeText(requireContext(),
                            "Failed to save entry: code=" + response.code() + " " + err,
                            Toast.LENGTH_LONG).show();
                }
                // синхронизируемся с сервером на всякий случай
                loadMoods();
            }

            @Override
            public void onFailure(@NonNull Call<MoodDto> call,
                                  @NonNull Throwable t) {
                Toast.makeText(requireContext(),
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                loadMoods();
            }
        });
    }

    // ---------- Обновление записи из диалога EditEntry ----------

    @Override
    public void onEntryUpdated(long id, int emotionId, String emoji, String title, String note) {
        String userId = session.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "No user session", Toast.LENGTH_SHORT).show();
            return;
        }

        api.updateMood(id, userId, emotionId, note).enqueue(new Callback<MoodDto>() {
            @Override
            public void onResponse(@NonNull Call<MoodDto> call,
                                   @NonNull Response<MoodDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DiaryEntry updated = mapDtoToEntry(response.body());
                    adapter.updateItem(updated);
                    updateEmptyState();
                } else {
                    String err = "";
                    try {
                        if (response.errorBody() != null) {
                            err = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}

                    Toast.makeText(requireContext(),
                            "Failed to update entry: code=" + response.code() + " " + err,
                            Toast.LENGTH_LONG).show();
                    loadMoods();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MoodDto> call,
                                  @NonNull Throwable t) {
                Toast.makeText(requireContext(),
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                loadMoods();
            }
        });
    }

    // ---------- Удаление записи ----------

    @Override
    public void onEntryDeleted(long id) {
        String userId = session.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "No user session", Toast.LENGTH_SHORT).show();
            return;
        }

        api.deleteMood(id, userId).enqueue(new Callback<SimpleResponse>() {
            @Override
            public void onResponse(@NonNull Call<SimpleResponse> call,
                                   @NonNull Response<SimpleResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    adapter.removeItem(id);
                    updateEmptyState();
                } else {
                    Toast.makeText(requireContext(),
                            "Failed to delete entry",
                            Toast.LENGTH_LONG).show();
                    loadMoods();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SimpleResponse> call,
                                  @NonNull Throwable t) {
                Toast.makeText(requireContext(),
                        "Network error: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
                loadMoods();
            }
        });
    }
}
