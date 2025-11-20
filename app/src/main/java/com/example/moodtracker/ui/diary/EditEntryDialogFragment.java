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

public class EditEntryDialogFragment extends DialogFragment {

    public interface OnEntryEditListener {
        void onEntryUpdated(long id, int emotionId, String emoji, String title, String note);
        void onEntryDeleted(long id);
    }

    private static final String ARG_ID = "id";
    private static final String ARG_EMOTION_ID = "emotion_id";
    private static final String ARG_EMOJI = "emoji";
    private static final String ARG_TITLE = "title";
    private static final String ARG_NOTE = "note";
    private static final String ARG_DATE = "date";

    public static EditEntryDialogFragment newInstance(DiaryEntry entry) {
        EditEntryDialogFragment f = new EditEntryDialogFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_ID, entry.id);
        b.putInt(ARG_EMOTION_ID, entry.emotionId);
        b.putString(ARG_EMOJI, entry.emoji);
        b.putString(ARG_TITLE, entry.title);
        b.putString(ARG_NOTE, entry.note);
        b.putString(ARG_DATE, entry.dateText);
        f.setArguments(b);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_entry, null, false);

        Bundle args = getArguments();
        if (args == null) args = new Bundle();

        long id = args.getLong(ARG_ID);
        int emotionId = args.getInt(ARG_EMOTION_ID);
        String emoji = args.getString(ARG_EMOJI, "😊");
        String title = args.getString(ARG_TITLE, "Mood");
        String note = args.getString(ARG_NOTE, "");
        String dateText = args.getString(ARG_DATE, "");

        TextView tvTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvDate = view.findViewById(R.id.tvDialogDate);

        TextView emoji1 = view.findViewById(R.id.emoji1);
        TextView emoji2 = view.findViewById(R.id.emoji2);
        TextView emoji3 = view.findViewById(R.id.emoji3);
        TextView emoji4 = view.findViewById(R.id.emoji4);
        TextView emoji5 = view.findViewById(R.id.emoji5);
        List<TextView> emojiViews = Arrays.asList(emoji1, emoji2, emoji3, emoji4, emoji5);

        TextInputLayout tilNote = view.findViewById(R.id.tilNote);
        TextInputEditText etNote = view.findViewById(R.id.etNote);

        MaterialButton btnDelete = view.findViewById(R.id.btnDelete);
        MaterialButton btnCancel = view.findViewById(R.id.btnCancel);
        MaterialButton btnSave = view.findViewById(R.id.btnSave);

        tvTitle.setText("Edit " + title);
        tvDate.setText(dateText);
        etNote.setText(note);

        final TextView[] selected = new TextView[1];

        // выбираем смайл по emotionId (tag)
        for (TextView tv : emojiViews) {
            Object tag = tv.getTag();
            int idTag = 0;
            if (tag != null) {
                try { idTag = Integer.parseInt(String.valueOf(tag)); } catch (Exception ignored) {}
            }
            if (idTag == emotionId) {
                selected[0] = tv;
            }
            tv.setOnClickListener(v -> {
                highlightSelected(emojiViews, tv);
                selected[0] = tv;
            });
        }
        // если не нашли по id, берём первую
        if (selected[0] == null) selected[0] = emoji1;
        highlightSelected(emojiViews, selected[0]);

        long finalId = id;

        btnDelete.setOnClickListener(v -> {
            if (getParentFragment() instanceof OnEntryEditListener) {
                ((OnEntryEditListener) getParentFragment()).onEntryDeleted(finalId);
            } else if (getActivity() instanceof OnEntryEditListener) {
                ((OnEntryEditListener) getActivity()).onEntryDeleted(finalId);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());

        btnSave.setOnClickListener(v -> {
            String newNote = etNote.getText() != null ? etNote.getText().toString().trim() : "";
            if (TextUtils.isEmpty(newNote)) {
                tilNote.setError("Please write something");
                return;
            } else {
                tilNote.setError(null);
            }

            TextView sel = selected[0];
            Object tag = sel.getTag();
            int newEmotionId = 0;
            if (tag != null) {
                try { newEmotionId = Integer.parseInt(String.valueOf(tag)); } catch (Exception ignored) {}
            }
            if (newEmotionId <= 0) {
                Toast.makeText(requireContext(), "Unknown emotion id", Toast.LENGTH_SHORT).show();
                return;
            }

            String newEmoji = sel.getText().toString();
            String newTitle = mapEmotionIdToTitle(newEmotionId);

            if (getParentFragment() instanceof OnEntryEditListener) {
                ((OnEntryEditListener) getParentFragment())
                        .onEntryUpdated(finalId, newEmotionId, newEmoji, newTitle, newNote);
            } else if (getActivity() instanceof OnEntryEditListener) {
                ((OnEntryEditListener) getActivity())
                        .onEntryUpdated(finalId, newEmotionId, newEmoji, newTitle, newNote);
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

    private String mapEmotionIdToTitle(int id) {
        switch (id) {
            case 1: return "Joy";
            case 2: return "Sadness";
            case 3: return "Anger";
            case 4: return "Fear";
            case 5: return "Neutral";
            default: return "Mood";
        }
    }
}
