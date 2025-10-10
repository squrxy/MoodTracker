package com.example.moodtracker.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.moodtracker.R;
import com.google.android.material.materialswitch.MaterialSwitch;

/** Экран "Настройки": простая заглушка */
public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        MaterialSwitch sw = v.findViewById(R.id.swNotifications);
        sw.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(getContext(), isChecked ? "Notifications ON" : "Notifications OFF", Toast.LENGTH_SHORT).show()
        );
        return v;
    }
}
