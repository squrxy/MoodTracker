package com.example.moodtracker.ui.state;

import com.example.moodtracker.net.dto.MoodDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoodStats {
    public final float joy;
    public final float sadness;
    public final float anger;
    public final float fear;
    public final float neutral;
    public final String dominantEmoji;
    public final String dominantTitle;
    public final boolean hasData;

    private MoodStats(float joy, float sadness, float anger, float fear, float neutral,
                      String dominantEmoji, String dominantTitle, boolean hasData) {
        this.joy = joy;
        this.sadness = sadness;
        this.anger = anger;
        this.fear = fear;
        this.neutral = neutral;
        this.dominantEmoji = dominantEmoji;
        this.dominantTitle = dominantTitle;
        this.hasData = hasData;
    }

    public static MoodStats empty() {
        return new MoodStats(0, 0, 0, 0, 0, "😊", "Joyful", false);
    }

    public static MoodStats from(List<MoodDto> moods) {
        if (moods == null || moods.isEmpty()) {
            return empty();
        }

        Map<Integer, Integer> counts = new HashMap<>();
        for (MoodDto dto : moods) {
            int key = (int) dto.emotion_id;
            counts.put(key, counts.getOrDefault(key, 0) + 1);
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
                dominantEmoji, dominantTitle, true);
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
}
