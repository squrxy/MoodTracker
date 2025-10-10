package com.example.moodtracker.net.dto;

public class AuthResponse {
    public boolean success;
    public String token;     // если пока нет токена — пришли guestId
    public String userId;    // строковый id (uuid) или числовой — неважно
    public boolean isGuest;
    public String message;
}
