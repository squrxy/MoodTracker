package com.example.moodtracker.ui.diary;

public class DiaryEntry {
    public final long id;
    public final int emotionId;
    public final String emoji;
    public final String title;
    public final String dateText;
    public final String note;

    public DiaryEntry(long id,
                      int emotionId,
                      String emoji,
                      String title,
                      String dateText,
                      String note) {
        this.id = id;
        this.emotionId = emotionId;
        this.emoji = emoji;
        this.title = title;
        this.dateText = dateText;
        this.note = note;
    }
}
