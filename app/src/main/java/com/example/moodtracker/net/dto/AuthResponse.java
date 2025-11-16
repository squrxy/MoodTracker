package com.example.moodtracker.net.dto;

public class AuthResponse {
    public boolean success;
    public String token;
    public String userId;
    public boolean isGuest;
    public String uuid;     // мы его тоже отдаём в PHP — пригодится
    public String message;  // текст ошибки с сервера
}
