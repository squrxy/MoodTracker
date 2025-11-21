package com.example.moodtracker.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.example.moodtracker.R;
import com.example.moodtracker.data.MoodResult;
import com.example.moodtracker.net.dto.MoodDto;
import com.example.moodtracker.ui.home.HomeViewModel;
import com.example.moodtracker.ui.home.HomeViewModelFactory;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Главный экран:
 * - Два слоя свечения (outer/inner) без щелей + blur => мощный цветной glow
 * - Верхний тонкий "пончик" со скруглёнными краями (щели 3dp), БЕЗ белых границ
 * - Комбо-анимация: расширение (animateY) + вращение (spin)
 * - Плавный "bounce" при скролле
 * - Lottie по центру: появление с баунсом и повтор с паузой
 */
public class HomeFragment extends Fragment {

    private PieChart pieChart, pieChartGlowOuter, pieChartGlowInner;
    private LottieAnimationView lottieEmoji;
    private TextView tvMoodTitle;
    private View cardPercents, tvMoodLabel, chartContainer;
    private NestedScrollView scroll;
    private View stateLoading, stateError;
    private TextView tvErrorMessage;

    private HomeViewModel viewModel;
    private boolean introPlayed = false;
    private boolean chartConfigured = false;

    // пауза между повторами Lottie (мс)
    private static final long LOTTIE_PAUSE_MS = 2000L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        pieChart = v.findViewById(R.id.pieChart);
        pieChartGlowOuter = v.findViewById(R.id.pieChartGlowOuter);
        pieChartGlowInner = v.findViewById(R.id.pieChartGlowInner);
        lottieEmoji = v.findViewById(R.id.lottieEmoji);
        tvMoodTitle = v.findViewById(R.id.tvMoodTitle);
        tvMoodLabel = v.findViewById(R.id.tvMoodLabel);
        cardPercents = v.findViewById(R.id.cardPercents);
        chartContainer = v.findViewById(R.id.chartContainer);
        scroll = v.findViewById(R.id.scroll);
        stateLoading = v.findViewById(R.id.stateLoading);
        stateError = v.findViewById(R.id.stateError);
        tvErrorMessage = v.findViewById(R.id.tvErrorMessage);

        viewModel = new ViewModelProvider(this, new HomeViewModelFactory(requireContext()))
                .get(HomeViewModel.class);

        // делаем контейнер квадратным после измерения
        chartContainer.post(() -> {
            int w = chartContainer.getWidth();
            ViewGroup.LayoutParams lp = chartContainer.getLayoutParams();
            lp.height = w;
            chartContainer.setLayoutParams(lp);
        });

        v.findViewById(R.id.btnRetry).setOnClickListener(view -> viewModel.loadMoods());

        viewModel.getMoodState().observe(getViewLifecycleOwner(), this::renderState);
        viewModel.loadMoods();
        hookBounceOnScroll();

        return v;
    }

    private void renderState(@Nullable MoodResult result) {
        if (result == null) return;

        boolean hasData = result.data != null;
        boolean hasEntries = hasData && !result.data.isEmpty();
        showLoading(result.isLoading && !hasEntries);
        showError(result.errorMessage, hasData);

        if (hasData) {
            applyMoodData(result.data);
            if (!introPlayed && !result.isLoading) {
                playIntroAnimations();
                introPlayed = true;
            }
        } else {
            hideContent();
        }
    }

    private void applyMoodData(@NonNull List<MoodDto> moods) {
        float[] percents = calculatePercents(moods);
        setupChart(percents);
        showContent();
    }

    private float[] calculatePercents(@NonNull List<MoodDto> moods) {
        float[] counts = new float[5];
        for (MoodDto mood : moods) {
            int idx = mapEmotionIdToIndex((int) mood.emotion_id);
            if (idx >= 0) counts[idx]++;
        }
        float total = counts[0] + counts[1] + counts[2] + counts[3] + counts[4];
        if (total == 0f) return new float[]{0f, 0f, 0f, 0f, 0f};

        for (int i = 0; i < counts.length; i++) {
            counts[i] = (counts[i] / total) * 100f;
        }
        return counts;
    }

    private int mapEmotionIdToIndex(int emotionId) {
        switch (emotionId) {
            case 1: return 0; // Joy
            case 2: return 1; // Sadness
            case 3: return 2; // Anger
            case 4: return 3; // Fear
            case 5: return 4; // Neutral
            default: return -1;
        }
    }

    private void setupChart(float[] percents) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(percents[0], "Joy"));
        entries.add(new PieEntry(percents[1], "Sadness"));
        entries.add(new PieEntry(percents[2], "Anger"));
        entries.add(new PieEntry(percents[3], "Fear"));
        entries.add(new PieEntry(percents[4], "Neutral"));

        // ===== верхний чарт (основной) =====
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(6f);

        List<Integer> colors = new ArrayList<>();
        colors.add(requireContext().getColor(R.color.yellowJoy));
        colors.add(requireContext().getColor(R.color.blueSad));
        colors.add(requireContext().getColor(R.color.redAnger));
        colors.add(requireContext().getColor(R.color.purpleFear));
        colors.add(requireContext().getColor(R.color.grayNeutral));
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        Legend legend = pieChart.getLegend();
        legend.setEnabled(false);
        pieChart.setRotationEnabled(false);

        pieChart.setHoleRadius(82f);
        pieChart.setTransparentCircleRadius(82f);
        pieChart.setTransparentCircleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleAlpha(0);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setDrawRoundedSlices(true);
        pieChart.setMinOffset(0f);
        pieChart.setExtraOffsets(6f, 6f, 6f, 6f);

        // --- Анимация “расширение + вращение” (замедленная) ---
        // 1) расширение сегментов (MPAndroidChart: animateY для Pie даёт эффект появления долей)
        pieChart.animateY(1400, Easing.EaseOutBack);
        // 2) одновременное плавное вращение (чуть медленнее)
        pieChart.spin(2400, 0f, 270f, Easing.EaseInOutQuad);
        pieChart.invalidate();

        // ===== нижние слои glow =====
        // OUTER
        PieDataSet glowOuterSet = new PieDataSet(entries, "");
        glowOuterSet.setSliceSpace(0f);
        glowOuterSet.setColors(withAlpha(colors, 0.75f));
        PieData glowOuterData = new PieData(glowOuterSet);
        glowOuterData.setDrawValues(false);

        pieChartGlowOuter.setData(glowOuterData);
        pieChartGlowOuter.getDescription().setEnabled(false);
        pieChartGlowOuter.setDrawEntryLabels(false);
        pieChartGlowOuter.getLegend().setEnabled(false);
        pieChartGlowOuter.setRotationEnabled(false);
        pieChartGlowOuter.setHoleRadius(82f);
        pieChartGlowOuter.setTransparentCircleRadius(98f); // мощнее свечение
        pieChartGlowOuter.setTransparentCircleColor(Color.TRANSPARENT);
        pieChartGlowOuter.setHoleColor(Color.TRANSPARENT);
        pieChartGlowOuter.setDrawRoundedSlices(true);
        pieChartGlowOuter.setMinOffset(0f);
        pieChartGlowOuter.setExtraOffsets(6f, 6f, 6f, 6f);
        pieChartGlowOuter.clearAnimation();
        // синхронизируем “расширение” с верхним
        pieChartGlowOuter.animateY(1400, Easing.EaseOutBack);
        // и лёгкое вращение вместе с верхним
        pieChartGlowOuter.spin(2400, 0f, 270f, Easing.EaseInOutQuad);
        pieChartGlowOuter.invalidate();

        // INNER
        PieDataSet glowInnerSet = new PieDataSet(entries, "");
        glowInnerSet.setSliceSpace(0f);
        glowInnerSet.setColors(withAlpha(colors, 0.45f));
        PieData glowInnerData = new PieData(glowInnerSet);
        glowInnerData.setDrawValues(false);

        pieChartGlowInner.setData(glowInnerData);
        pieChartGlowInner.getDescription().setEnabled(false);
        pieChartGlowInner.setDrawEntryLabels(false);
        pieChartGlowInner.getLegend().setEnabled(false);
        pieChartGlowInner.setRotationEnabled(false);
        pieChartGlowInner.setHoleRadius(82f);
        pieChartGlowInner.setTransparentCircleRadius(90f);
        pieChartGlowInner.setTransparentCircleColor(Color.TRANSPARENT);
        pieChartGlowInner.setHoleColor(Color.TRANSPARENT);
        pieChartGlowInner.setDrawRoundedSlices(true);
        pieChartGlowInner.setMinOffset(0f);
        pieChartGlowInner.setExtraOffsets(6f, 6f, 6f, 6f);
        pieChartGlowInner.clearAnimation();
        pieChartGlowInner.animateY(1400, Easing.EaseOutBack);
        pieChartGlowInner.spin(2400, 0f, 270f, Easing.EaseInOutQuad);
        pieChartGlowInner.invalidate();

        // сильный blur на API 31+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pieChartGlowOuter.setRenderEffect(RenderEffect.createBlurEffect(44f, 44f, Shader.TileMode.CLAMP));
            pieChartGlowInner.setRenderEffect(RenderEffect.createBlurEffect(28f, 28f, Shader.TileMode.CLAMP));
        }

        // текст и проценты
        float top = percents[0];
        String topLabel = "Joyful";
        String[] labels = {"Joy", "Sadness", "Anger", "Fear", "Neutral"};
        boolean hasPositive = percents[0] > 0f;
        for (int i = 1; i < percents.length; i++) {
            if (percents[i] > top) {
                top = percents[i];
                topLabel = labels[i];
            }
            if (percents[i] > 0f) hasPositive = true;
        }
        if (!hasPositive) {
            topLabel = getString(R.string.mood_percentages);
        }
        tvMoodTitle.setText(topLabel);
        bindRow(requireView().findViewById(R.id.rowJoy),     requireContext().getColor(R.color.yellowJoy),  "Joy",     formatPercent(percents[0]));
        bindRow(requireView().findViewById(R.id.rowSad),     requireContext().getColor(R.color.blueSad),    "Sadness", formatPercent(percents[1]));
        bindRow(requireView().findViewById(R.id.rowAnger),   requireContext().getColor(R.color.redAnger),   "Anger",   formatPercent(percents[2]));
        bindRow(requireView().findViewById(R.id.rowFear),    requireContext().getColor(R.color.purpleFear), "Fear",    formatPercent(percents[3]));
        bindRow(requireView().findViewById(R.id.rowNeutral), requireContext().getColor(R.color.grayNeutral),"Neutral", formatPercent(percents[4]));

        chartConfigured = true;
    }

    private String formatPercent(float value) {
        return String.format(Locale.getDefault(), "%.0f%%", value);
    }

    /** лёгкий bounce-эффект при прокрутке (по первому ощутимому скроллу) */
    private void hookBounceOnScroll() {
        if (scroll == null) return;
        scroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            private int lastY = 0;
            private long lastTs = 0L;
            @Override
            public void onScrollChange(@NonNull NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                int dy = Math.abs(scrollY - oldScrollY);
                long now = System.currentTimeMillis();
                // если пользователь активно двинул экран и давно не играли анимацию — подпружиним чарт
                if (dy > 12 && (now - lastTs) > 600) {
                    lastTs = now;
                    chartContainer.animate()
                            .scaleX(0.985f).scaleY(0.985f)
                            .setDuration(120)
                            .withEndAction(() -> chartContainer.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .setDuration(260)
                                    .setInterpolator(new OvershootInterpolator(1.2f))
                                    .start())
                            .start();
                }
                lastY = scrollY;
            }
        });
    }

    /** применить альфу ко всем цветам сразу */
    private List<Integer> withAlpha(List<Integer> base, float alpha) {
        List<Integer> out = new ArrayList<>(base.size());
        int a = Math.min(255, Math.max(0, (int)(alpha * 255)));
        for (int c : base) out.add((c & 0x00FFFFFF) | (a << 24));
        return out;
    }

    private void bindRow(@NonNull View row, int color, @NonNull String label, @NonNull String value) {
        View dot = row.findViewById(R.id.dot);
        ((TextView) row.findViewById(R.id.label)).setText(label);
        ((TextView) row.findViewById(R.id.value)).setText(value);
        GradientDrawable bg = (GradientDrawable) dot.getBackground();
        bg.setColor(color);
    }

    /** плавные появления + Lottie с баунсом и паузой между повторами */
    private void playIntroAnimations() {
        float dy = 18f * getResources().getDisplayMetrics().density;

        for (View v : new View[]{chartContainer, tvMoodLabel, tvMoodTitle, cardPercents}) {
            v.setAlpha(0f);
            v.setTranslationY(dy);
            v.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(420) // чуть медленнее
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
            dy *= 0.85f;
        }

        // Lottie: появление медленнее и с bounce
        lottieEmoji.setAlpha(0f);
        lottieEmoji.setScaleX(0f);
        lottieEmoji.setScaleY(0f);
        lottieEmoji.setSpeed(0.9f);      // скорость самой анимации
        lottieEmoji.playAnimation();     // первый прогон

        ObjectAnimator sx = ObjectAnimator.ofFloat(lottieEmoji, View.SCALE_X, 0f, 1f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(lottieEmoji, View.SCALE_Y, 0f, 1f);
        ObjectAnimator a  = ObjectAnimator.ofFloat(lottieEmoji, View.ALPHA,   0f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.setInterpolator(new OvershootInterpolator(1.6f));
        set.setStartDelay(320);          // позже и медленнее
        set.setDuration(640);
        set.playTogether(sx, sy, a);
        lottieEmoji.post(set::start);

        // Повтор Lottie с паузой: отключаем автолооп и перезапускаем с задержкой
        lottieEmoji.setRepeatCount(0);
        lottieEmoji.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(Animator animation) {
                lottieEmoji.postDelayed(lottieEmoji::playAnimation, LOTTIE_PAUSE_MS);
            }
        });
    }

    private void showLoading(boolean show) {
        if (stateLoading != null) stateLoading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(@Nullable String errorMessage, boolean hasData) {
        if (stateError == null) return;
        if (errorMessage != null && !errorMessage.isEmpty() && !hasData) {
            stateError.setVisibility(View.VISIBLE);
            tvErrorMessage.setText(errorMessage);
        } else {
            stateError.setVisibility(View.GONE);
        }
    }

    private void showContent() {
        for (View v : new View[]{chartContainer, tvMoodLabel, tvMoodTitle, cardPercents}) {
            v.setVisibility(View.VISIBLE);
        }
        if (!chartConfigured) return;
        pieChart.invalidate();
        pieChartGlowOuter.invalidate();
        pieChartGlowInner.invalidate();
    }

    private void hideContent() {
        for (View v : new View[]{chartContainer, tvMoodLabel, tvMoodTitle, cardPercents}) {
            v.setVisibility(View.INVISIBLE);
        }
    }
}
