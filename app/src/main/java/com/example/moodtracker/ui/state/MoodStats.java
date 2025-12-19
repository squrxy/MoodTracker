package com.example.moodtracker.ui.state;

import com.example.moodtracker.net.dto.MoodDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class MoodStats {
    public final float joy;
    public final float sadness;
    public final float anger;
    public final float fear;
    public final float neutral;
    public final int dominantId;
    public final String dominantEmoji;
    public final String dominantTitle;
    public final boolean hasData;
    public final List<DailyBreakdown> weeklyBreakdown;

    private MoodStats(float joy, float sadness, float anger, float fear, float neutral,
                      int dominantId, String dominantEmoji, String dominantTitle, boolean hasData,
                      List<DailyBreakdown> weeklyBreakdown) {
        this.joy = joy;
        this.sadness = sadness;
        this.anger = anger;
        this.fear = fear;
        this.neutral = neutral;
        this.dominantId = dominantId;
        this.dominantEmoji = dominantEmoji;
        this.dominantTitle = dominantTitle;
        this.hasData = hasData;
        this.weeklyBreakdown = weeklyBreakdown;
    }

    public static MoodStats empty() {
        return new MoodStats(0, 0, 0, 0, 0, 1, "😊", "Joyful", false, new ArrayList<>());
    }

    public static MoodStats from(List<MoodDto> moods) {
        if (moods == null || moods.isEmpty()) {
            return empty();
        }

        Map<Integer, Integer> counts = new HashMap<>();
        Map<LocalDate, int[]> weekly = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);

        for (MoodDto dto : moods) {
            int key = (int) dto.emotion_id;
            counts.put(key, counts.getOrDefault(key, 0) + 1);

            LocalDate date = parseDate(dto.created_at);
            if (date != null && (date.isEqual(today) || date.isEqual(weekAgo) || (date.isAfter(weekAgo) && date.isBefore(today)))) {
                int[] arr = weekly.getOrDefault(date, new int[]{0, 0, 0, 0, 0});
                int idx = Math.max(0, Math.min(4, key - 1));
                arr[idx] += 1;
                weekly.put(date, arr);
            }
        }

        int total = moods.size();
        float joy = percent(counts.getOrDefault(1, 0), total);
        float sadness = percent(counts.getOrDefault(2, 0), total);
        float anger = percent(counts.getOrDefault(3, 0), total);
        float fear = percent(counts.getOrDefault(4, 0), total);
        float neutral = percent(counts.getOrDefault(5, 0), total);

        int dominantId = dominantEmotion(counts);
        String dominantEmoji = mapEmotionToEmoji(dominantId);
        String dominantTitle = mapEmotionToTitle(dominantId);

        return new MoodStats(joy, sadness, anger, fear, neutral,
                dominantId, dominantEmoji, dominantTitle, true, computeWeekly(weekly, today));
    }

    private static List<DailyBreakdown> computeWeekly(Map<LocalDate, int[]> weekly, LocalDate today) {
        List<DailyBreakdown> days = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            int[] counts = weekly.getOrDefault(day, new int[]{0, 0, 0, 0, 0});
            int total = counts[0] + counts[1] + counts[2] + counts[3] + counts[4];
            float joy = percent(counts[0], total);
            float sadness = percent(counts[1], total);
            float anger = percent(counts[2], total);
            float fear = percent(counts[3], total);
            float neutral = percent(counts[4], total);
            String label = day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.getDefault());
            days.add(new DailyBreakdown(label, joy, sadness, anger, fear, neutral, total));
        }
        return days;
    }

    private static LocalDate parseDate(String createdAt) {
        if (createdAt == null) return null;
        String trimmed = createdAt.length() >= 10 ? createdAt.substring(0, 10) : createdAt;
        DateTimeFormatter[] patterns = new DateTimeFormatter[]{
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy")
        };
        for (DateTimeFormatter fmt : patterns) {
            try {
                return LocalDate.parse(trimmed, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private static float percent(int count, int total) {
        if (total == 0) return 0f;
        return (count * 100f) / total;
    }

    private static int dominantEmotion(Map<Integer, Integer> counts) {
        int maxEmotion = 1;
        int maxCount = -1;
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            int c = entry.getValue();
            if (c > maxCount) {
                maxEmotion = entry.getKey();
                maxCount = c;
            }
        }
        return maxEmotion;
    }

    private static String mapEmotionToEmoji(int emotionId) {
        switch (emotionId) {
            case 1: return "😄";
            case 2: return "😢";
            case 3: return "😡";
            case 4: return "😨";
            case 5: return "😐";
            default: return "😊";
        }
    }

    private static String mapEmotionToTitle(int emotionId) {
        switch (emotionId) {
            case 1: return "Joy";
            case 2: return "Sadness";
            case 3: return "Anger";
            case 4: return "Fear";
            case 5: return "Neutral";
            default: return "Mood";
        }
    }

    public static class DailyBreakdown {
        public final String label;
        public final float joy;
        public final float sadness;
        public final float anger;
        public final float fear;
        public final float neutral;
        public final int total;

        DailyBreakdown(String label, float joy, float sadness, float anger, float fear, float neutral, int total) {
            this.label = label;
            this.joy = joy;
            this.sadness = sadness;
            this.anger = anger;
            this.fear = fear;
            this.neutral = neutral;
            this.total = total;
        }
    }
}
