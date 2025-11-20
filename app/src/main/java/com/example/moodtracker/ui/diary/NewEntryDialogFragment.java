package com.example.moodtracker.ui.diary;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.moodtracker.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;
import java.util.List;

public class NewEntryDialogFragment extends DialogFragment {

    public interface OnNewEntryListener {
        void onNewEntrySaved(int emotionId, String emoji, String title, String note);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_new_entry, null, false);

        TextView emoji1 = view.findViewById(R.id.emoji1);
        TextView emoji2 = view.findViewById(R.id.emoji2);
        TextView emoji3 = view.findViewById(R.id.emoji3);
        TextView emoji4 = view.findViewById(R.id.emoji4);
        TextView emoji5 = view.findViewById(R.id.emoji5);

        List<TextView> emojiViews = Arrays.asList(emoji1, emoji2, emoji3, emoji4, emoji5);

        TextInputLayout tilNote = view.findViewById(R.id.tilNote);
        TextInputEditText etNote = view.findViewById(R.id.etNote);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialButton btnSave = view.findViewById(R.id.btnSave);

        final TextView[] selected = new TextView[1];

        // по умолчанию выберем первую эмоцию (Joy)
        highlightSelected(emojiViews, emoji1);
        selected[0] = emoji1;

        for (TextView tv : emojiViews) {
            tv.setOnClickListener(v -> {
                highlightSelected(emojiViews, tv);
                selected[0] = tv;
            });
        }

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";
            if (TextUtils.isEmpty(note)) {
                tilNote.setError("Please write something");
                return;
            } else {
                tilNote.setError(null);
            }

            if (selected[0] == null) {
                Toast.makeText(requireContext(), "Choose your mood", Toast.LENGTH_SHORT).show();
                return;
            }

            String emoji = selected[0].getText().toString();
            int emotionId = mapEmojiToEmotionId(emoji);
            String title = mapEmojiToTitle(emoji);

            if (emotionId <= 0) {
                Toast.makeText(requireContext(), "Unknown emotion", Toast.LENGTH_SHORT).show();
                return;
            }

            if (getParentFragment() instanceof OnNewEntryListener) {
                ((OnNewEntryListener) getParentFragment()).onNewEntrySaved(emotionId, emoji, title, note);
            } else if (getActivity() instanceof OnNewEntryListener) {
                ((OnNewEntryListener) getActivity()).onNewEntrySaved(emotionId, emoji, title, note);
            }

            dismiss();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.transparent);

                WindowManager.LayoutParams params = window.getAttributes();
                DisplayMetrics dm = getResources().getDisplayMetrics();
                params.width = (int) (dm.widthPixels * 0.9f);
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                params.gravity = Gravity.CENTER;
                params.dimAmount = 0.35f;
                window.setAttributes(params);

                window.setWindowAnimations(R.style.DialogCenterFadeAnimation);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    window.setBackgroundBlurRadius(40);
                }
            }
        }
    }

    private void highlightSelected(List<TextView> all, TextView selected) {
        for (TextView tv : all) {
            if (tv == selected) {
                tv.setBackgroundResource(R.drawable.bg_emoji_choice);
            } else {
                tv.setBackground(null);
            }
        }
    }

    private int mapEmojiToEmotionId(String emoji) {
        switch (emoji) {
            case "😄": return 1; // Joy
            case "😢": return 2; // Sadness
            case "😡": return 3; // Anger
            case "😨": return 4; // Fear
            case "😐": return 5; // Neutral
            default:   return 0;
        }
    }

    private String mapEmojiToTitle(String emoji) {
        switch (emoji) {
            case "😄": return "Joy";
            case "😢": return "Sadness";
            case "😡": return "Anger";
            case "😨": return "Fear";
            case "😐": return "Neutral";
            default:   return "Mood";
        }
    }

    public static NewEntryDialogFragment newInstance() {
        return new NewEntryDialogFragment();
    }
}
