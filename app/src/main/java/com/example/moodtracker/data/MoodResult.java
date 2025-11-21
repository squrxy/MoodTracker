package com.example.moodtracker.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.moodtracker.net.dto.MoodDto;

import java.util.Collections;
import java.util.List;

/**
 * Стейт загрузки/успеха/ошибки для MoodRepository.
 */
public class MoodResult {

    public final boolean isLoading;
    @NonNull public final List<MoodDto> data;
    @Nullable public final String errorMessage;
    public final boolean fromCache;

    private MoodResult(boolean isLoading,
                       @NonNull List<MoodDto> data,
                       @Nullable String errorMessage,
                       boolean fromCache) {
        this.isLoading = isLoading;
        this.data = data;
        this.errorMessage = errorMessage;
        this.fromCache = fromCache;
    }

    public static MoodResult loading(@NonNull List<MoodDto> cached) {
        return new MoodResult(true, Collections.unmodifiableList(cached), null, true);
    }

    public static MoodResult success(@NonNull List<MoodDto> data, boolean fromCache) {
        return new MoodResult(false, Collections.unmodifiableList(data), null, fromCache);
    }

    public static MoodResult error(@Nullable String errorMessage, @NonNull List<MoodDto> cached) {
        return new MoodResult(false, Collections.unmodifiableList(cached), errorMessage, !cached.isEmpty());
    }
}
