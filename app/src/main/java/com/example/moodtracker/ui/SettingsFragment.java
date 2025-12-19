package com.example.moodtracker.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.TextUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.moodtracker.R;
import com.example.moodtracker.auth.SessionManager;
import com.example.moodtracker.data.SettingsRepository;
import com.example.moodtracker.net.ApiClient;
import com.example.moodtracker.net.ApiService;
import com.example.moodtracker.net.dto.SettingsDto;
import com.example.moodtracker.ui.state.UiState;
import com.example.moodtracker.ui.SettingsViewModelFactory;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/** Экран "Настройки": сохранение предпочитаемого периода и времени уведомлений */
public class SettingsFragment extends Fragment {

    private MaterialSwitch swNotifications;
    private TextInputEditText etNotifyTime;
    private MaterialAutoCompleteTextView actPeriod;
    private TextInputLayout tilNotifyTime;
    private TextInputEditText etDisplayName;
    private TextInputEditText etAbout;
    private ProgressBar progress;

    private SettingsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        swNotifications = v.findViewById(R.id.swNotifications);
        etNotifyTime = v.findViewById(R.id.etNotifyTime);
        actPeriod = v.findViewById(R.id.actPeriod);
        tilNotifyTime = v.findViewById(R.id.tilNotifyTime);
        etDisplayName = v.findViewById(R.id.etDisplayName);
        etAbout = v.findViewById(R.id.etAbout);
        progress = v.findViewById(R.id.progressSettings);

        SessionManager sessionManager = new SessionManager(requireContext());
        sessionManager.ensureLocalGuest(); // гарантируем user_id даже для гостя
        SettingsRepository repository = new SettingsRepository(ApiClient.get().create(ApiService.class));
        SettingsViewModelFactory factory = new SettingsViewModelFactory(repository, sessionManager);
        viewModel = new ViewModelProvider(this, factory).get(SettingsViewModel.class);

        MaterialButton btnSave = v.findViewById(R.id.btnSaveSettings);
        btnSave.setOnClickListener(view -> saveSettings());

        actPeriod.setSimpleItems(getResources().getStringArray(R.array.settings_period_options));

        observeSettings();
        viewModel.loadSettings();
        return v;
    }

    private void observeSettings() {
        viewModel.getSettingsState().observe(getViewLifecycleOwner(), state -> {
            boolean isLoading = state.getStatus() == UiState.Status.LOADING;
            progress.setVisibility(isLoading ? View.VISIBLE : View.GONE);

            SettingsDto data = state.getData();
            if (data != null) {
                bindSettings(data);
            }

            if (state.getStatus() == UiState.Status.ERROR && state.getError() != null) {
                Toast.makeText(requireContext(), state.getError(), Toast.LENGTH_LONG).show();
            } else if (state.getStatus() == UiState.Status.SUCCESS) {
                Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindSettings(@NonNull SettingsDto dto) {
        swNotifications.setChecked(dto.notifications_enabled);
        if (!TextUtils.isEmpty(dto.notify_time)) {
            etNotifyTime.setText(dto.notify_time);
        }
        if (!TextUtils.isEmpty(dto.default_period)) {
            actPeriod.setText(dto.default_period, false);
        }
        if (!TextUtils.isEmpty(dto.display_name)) {
            etDisplayName.setText(dto.display_name);
        }
        if (!TextUtils.isEmpty(dto.about)) {
            etAbout.setText(dto.about);
        }
    }

    private void saveSettings() {
        String notifyTime = etNotifyTime.getText() != null ? etNotifyTime.getText().toString().trim() : "";
        String defaultPeriod = actPeriod.getText() != null ? actPeriod.getText().toString().trim() : "";
        String displayName = etDisplayName.getText() != null ? etDisplayName.getText().toString().trim() : "";
        String about = etAbout.getText() != null ? etAbout.getText().toString().trim() : "";

        if (TextUtils.isEmpty(defaultPeriod)) {
            // Бэкенд ожидает значение по умолчанию — подставляем первый пункт
            String[] options = getResources().getStringArray(R.array.settings_period_options);
            if (options.length > 0) {
                defaultPeriod = options[0];
                actPeriod.setText(defaultPeriod, false);
            }
        }

        if (TextUtils.isEmpty(notifyTime)) {
            tilNotifyTime.setError(getString(R.string.settings_time_error));
            return;
        } else {
            tilNotifyTime.setError(null);
        }

        viewModel.saveSettings(swNotifications.isChecked(), notifyTime, defaultPeriod, displayName, about);
    }
}
