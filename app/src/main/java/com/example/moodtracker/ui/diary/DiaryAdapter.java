package com.example.moodtracker.ui.diary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moodtracker.R;

import java.util.ArrayList;
import java.util.List;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {

    public interface OnEntryClickListener {
        void onEntryClick(DiaryEntry entry);
    }

    private final List<DiaryEntry> items = new ArrayList<>();
    private final OnEntryClickListener listener;

    public DiaryAdapter(OnEntryClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<DiaryEntry> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void addItem(DiaryEntry entry) {
        items.add(0, entry);    // новые наверх
        notifyItemInserted(0);
    }

    public void updateItem(DiaryEntry updated) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id == updated.id) {
                items.set(i, updated);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void removeItem(long id) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id == id) {
                items.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diary_entry, parent, false);
        return new DiaryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        DiaryEntry e = items.get(position);
        holder.tvEmoji.setText(e.emoji);
        holder.tvTitle.setText(e.title);
        holder.tvDate.setText(e.dateText);
        holder.tvNote.setText(e.note);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onEntryClick(e);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DiaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvTitle, tvDate, tvNote;
        DiaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvEmoji);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDate  = itemView.findViewById(R.id.tvDate);
            tvNote  = itemView.findViewById(R.id.tvNote);
        }
    }
}
