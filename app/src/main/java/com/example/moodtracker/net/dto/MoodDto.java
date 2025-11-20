package com.example.moodtracker.net.dto;

public class MoodDto {
    public long id;
    public long user_id;
    public long emotion_id;
    public String note;
    public String created_at;

    public String name;   // из emotions_meta.name
    public String icon;   // из emotions_meta.icon (эмодзи)
    public String color;  // из emotions_meta.color (#hex)
}
