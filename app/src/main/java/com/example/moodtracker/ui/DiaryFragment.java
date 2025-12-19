package com.example.moodtracker.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodtracker.R;
import com.example.moodtracker.auth.SessionManager;
import com.example.moodtracker.data.MoodRepository;
import com.example.moodtracker.net.ApiClient;
import com.example.moodtracker.net.ApiService;
import com.example.moodtracker.ui.diary.DiaryAdapter;
import com.example.moodtracker.ui.diary.DiaryEntry;
import com.example.moodtracker.ui.diary.DiaryViewModel;
import com.example.moodtracker.ui.diary.DiaryViewModelFactory;
import com.example.moodtracker.ui.diary.EditEntryDialogFragment;
import com.example.moodtracker.ui.diary.NewEntryDialogFragment;
import com.example.moodtracker.ui.state.UiState;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class DiaryFragment extends Fragment
        implements NewEntryDialogFragment.OnNewEntryListener,
        EditEntryDialogFragment.OnEntryEditListener {

    private RecyclerView rvDiary;
    private View emptyState;
    private FloatingActionButton fabAdd;
    private DiaryAdapter adapter;
    private ProgressBar progress;

    private DiaryViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_diary, container, false);

        rvDiary = v.findViewById(R.id.rvDiary);
        emptyState = v.findViewById(R.id.emptyState);
        fabAdd = v.findViewById(R.id.fabAddEntry);
        progress = v.findViewById(R.id.progress);

        rvDiary.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new DiaryAdapter(entry -> {
            EditEntryDialogFragment dialog = EditEntryDialogFragment.newInstance(entry);
            dialog.show(getChildFragmentManager(), "EditEntryDialog");
        });
        rvDiary.setAdapter(adapter);

        SessionManager session = new SessionManager(requireContext());
        MoodRepository repository = new MoodRepository(ApiClient.get().create(ApiService.class));
        DiaryViewModelFactory factory = new DiaryViewModelFactory(repository, session);
        viewModel = new ViewModelProvider(this, factory).get(DiaryViewModel.class);
        observeMoods();

        fabAdd.setOnClickListener(view -> {
            NewEntryDialogFragment dialog = NewEntryDialogFragment.newInstance();
            dialog.show(getChildFragmentManager(), "NewEntryDialog");
        });

        viewModel.loadMoods();

        return v;
    }

    private void observeMoods() {
        viewModel.getMoodsState().observe(getViewLifecycleOwner(), this::renderState);
    }

    private void renderState(UiState<List<DiaryEntry>> state) {
        boolean isLoading = state.getStatus() == UiState.Status.LOADING;
        progress.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        List<DiaryEntry> data = state.getData();
        if (data != null) {
            int previousCount = adapter.getItemCount();
            adapter.setItems(data);
            if (data.size() > previousCount) {
                rvDiary.smoothScrollToPosition(0);
            }
            updateEmptyState();
        }

        if (state.getStatus() == UiState.Status.ERROR && state.getError() != null) {
            Toast.makeText(requireContext(), state.getError(), Toast.LENGTH_LONG).show();
        }
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

    @Override
    public void onNewEntrySaved(int emotionId, String emoji, String title, String note) {
        viewModel.createMood(emotionId, note);
    }

    @Override
    public void onEntryUpdated(long id, int emotionId, String emoji, String title, String note) {
        viewModel.updateMood(id, emotionId, note);
    }

    @Override
    public void onEntryDeleted(long id) {
        viewModel.deleteMood(id);
    }
}
