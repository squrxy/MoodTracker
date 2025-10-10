package com.example.moodtracker.auth;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class SessionManager {
    private static final String PREF = "session_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_IS_GUEST = "is_guest";

    private final SharedPreferences sp;

    public SessionManager(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, String userId, boolean isGuest) {
        sp.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USER_ID, userId)
                .putBoolean(KEY_IS_GUEST, isGuest)
                .apply();
    }

    public boolean hasSession() {
        return sp.contains(KEY_USER_ID);
    }

    public boolean isGuest() { return sp.getBoolean(KEY_IS_GUEST, false); }
    public String getUserId() { return sp.getString(KEY_USER_ID, null); }

    /** Локальный guest, если API ещё не готов */
    public void ensureLocalGuest() {
        if (!hasSession()) {
            String gid = UUID.randomUUID().toString();
            saveSession(gid, gid, true);
        }
    }

    public void clear() { sp.edit().clear().apply(); }
}
