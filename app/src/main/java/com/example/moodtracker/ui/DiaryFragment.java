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

import java.util.ArrayList;
import java.util.List;

/** Экран "Дневник": список записей + FAB "добавить" */
public class DiaryFragment extends Fragment {

    private RecyclerView rv;
    private DiaryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_diary, container, false);

        rv = v.findViewById(R.id.rvDiary);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DiaryAdapter(dummy());
        rv.setAdapter(adapter);

        v.findViewById(R.id.fabAdd).setOnClickListener(view ->
                Toast.makeText(getContext(), "Add new mood (TODO)", Toast.LENGTH_SHORT).show()
        );

        return v;
    }

    // Демоданные
    private List<DiaryItem> dummy() {
        List<DiaryItem> list = new ArrayList<>();
        list.add(new DiaryItem("😂 Joy", "Coffee with friends"));
        list.add(new DiaryItem("😐 Neutral", "Regular work day"));
        list.add(new DiaryItem("😔 Sadness", "Missed a call"));
        return list;
    }

    // ---- Adapter/Holder ----
    static class DiaryItem {
        final String mood; final String note;
        DiaryItem(String m, String n){ mood=m; note=n; }
    }

    static class DiaryAdapter extends RecyclerView.Adapter<DiaryVH> {
        final List<DiaryItem> items;
        DiaryAdapter(List<DiaryItem> items){ this.items = items; }

        @NonNull @Override public DiaryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diary, parent, false);
            return new DiaryVH(v);
        }
        @Override public void onBindViewHolder(@NonNull DiaryVH h, int pos) { h.bind(items.get(pos)); }
        @Override public int getItemCount(){ return items.size(); }
    }

    static class DiaryVH extends RecyclerView.ViewHolder {
        private final android.widget.TextView tvMood, tvNote;
        DiaryVH(@NonNull View itemView) {
            super(itemView);
            tvMood = itemView.findViewById(R.id.tvMood);
            tvNote = itemView.findViewById(R.id.tvNote);
        }
        void bind(DiaryItem item) {
            tvMood.setText(item.mood);
            tvNote.setText(item.note);
        }
    }
}
