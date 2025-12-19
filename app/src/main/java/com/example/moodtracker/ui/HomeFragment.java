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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.moodtracker.R;
import com.example.moodtracker.auth.SessionManager;
import com.example.moodtracker.data.MoodRepository;
import com.example.moodtracker.net.ApiClient;
import com.example.moodtracker.net.ApiService;
import com.example.moodtracker.ui.HomeViewModelFactory;
import com.example.moodtracker.ui.state.MoodStats;
import com.example.moodtracker.ui.state.UiState;
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
    private TextView tvMoodTitle, homeError;
    private View cardPercents, tvMoodLabel, chartContainer;
    private NestedScrollView scroll;
    private ProgressBar homeProgress;

    private HomeViewModel viewModel;
    private boolean introPlayed = false;
    private final Runnable lottieLoopRunnable = new Runnable() {
        @Override
        public void run() {
            lottieEmoji.playAnimation();
        }
    };

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
        homeProgress = v.findViewById(R.id.homeProgress);
        homeError = v.findViewById(R.id.homeError);

        chartContainer.post(() -> {
            int w = chartContainer.getWidth();
            ViewGroup.LayoutParams lp = chartContainer.getLayoutParams();
            lp.height = w;
            chartContainer.setLayoutParams(lp);
        });

        SessionManager sessionManager = new SessionManager(requireContext());
        MoodRepository repository = new MoodRepository(ApiClient.get().create(ApiService.class));
        HomeViewModelFactory factory = new HomeViewModelFactory(repository, sessionManager);
        viewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
        observeStats(v);

        hookBounceOnScroll();

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadStats();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        lottieEmoji.removeCallbacks(lottieLoopRunnable);
        lottieEmoji.removeAllAnimatorListeners();
        lottieEmoji.cancelAnimation();
    }

    private void observeStats(@NonNull View root) {
        viewModel.getStatsState().observe(getViewLifecycleOwner(), state -> {
            boolean isLoading = state.getStatus() == UiState.Status.LOADING;
            homeProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);

            if (state.getStatus() == UiState.Status.ERROR && state.getError() != null) {
                homeError.setVisibility(View.VISIBLE);
                homeError.setText(state.getError());
            } else {
                homeError.setVisibility(View.GONE);
            }

            MoodStats stats = state.getData();
            if (stats != null && stats.hasData) {
                chartContainer.setVisibility(View.VISIBLE);
                cardPercents.setVisibility(View.VISIBLE);
                setupChart(root, stats);
                applyEmojiAnimation(stats.dominantId);
                if (!introPlayed) {
                    playIntroAnimations();
                    introPlayed = true;
                }
            } else {
                chartContainer.setVisibility(View.INVISIBLE);
                cardPercents.setVisibility(View.GONE);
                tvMoodTitle.setText("No mood data yet");
                tvMoodLabel.setVisibility(View.VISIBLE);
                applyEmojiAnimation(0);
            }
        });
    }

    private void setupChart(@NonNull View root, @NonNull MoodStats stats) {
        float joy = stats.joy;
        float sadness = stats.sadness;
        float anger = stats.anger;
        float fear = stats.fear;
        float neutral = stats.neutral;

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(joy, "Joy"));
        entries.add(new PieEntry(sadness, "Sadness"));
        entries.add(new PieEntry(anger, "Anger"));
        entries.add(new PieEntry(fear, "Fear"));
        entries.add(new PieEntry(neutral, "Neutral"));

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pieChartGlowOuter.setRenderEffect(RenderEffect.createBlurEffect(44f, 44f, Shader.TileMode.CLAMP));
            pieChartGlowInner.setRenderEffect(RenderEffect.createBlurEffect(28f, 28f, Shader.TileMode.CLAMP));
        }

        tvMoodTitle.setText(stats.dominantTitle + " " + stats.dominantEmoji);
        bindRow(root.findViewById(R.id.rowJoy),     requireContext().getColor(R.color.yellowJoy),  "Joy",     formatPercent(joy));
        bindRow(root.findViewById(R.id.rowSad),     requireContext().getColor(R.color.blueSad),    "Sadness", formatPercent(sadness));
        bindRow(root.findViewById(R.id.rowAnger),   requireContext().getColor(R.color.redAnger),   "Anger",   formatPercent(anger));
        bindRow(root.findViewById(R.id.rowFear),    requireContext().getColor(R.color.purpleFear), "Fear",    formatPercent(fear));
        bindRow(root.findViewById(R.id.rowNeutral), requireContext().getColor(R.color.grayNeutral),"Neutral", formatPercent(neutral));
    }

    private String formatPercent(float value) {
        return String.format(Locale.getDefault(), "%.0f%%", value);
    }

    private void applyEmojiAnimation(int dominantEmotionId) {
        int animationRes = mapEmotionToAnimation(dominantEmotionId);
        lottieEmoji.setAnimation(animationRes);
        lottieEmoji.setProgress(0f);
        startLottieWithPause();
    }

    private int mapEmotionToAnimation(int emotionId) {
        switch (emotionId) {
            case 1:
                return R.raw.emoji_laugh;
            case 2:
                return R.raw.emoji_sad;
            case 3:
                return R.raw.emoji_anger;
            case 4:
                return R.raw.emoji_fear;
            case 5:
                return R.raw.emoji_neutral;
            default:
                return R.raw.emoji_laugh;
        }
    }

    private List<Integer> withAlpha(List<Integer> colors, float alpha) {
        List<Integer> list = new ArrayList<>();
        for (Integer c : colors) {
            int a = Math.round(255 * alpha);
            int color = Color.argb(a, Color.red(c), Color.green(c), Color.blue(c));
            list.add(color);
        }
        return list;
    }

    private void bindRow(View row, int color, String label, String percent) {
        View dot = row.findViewById(R.id.dot);
        TextView tvLabel = row.findViewById(R.id.label);
        TextView tvPercent = row.findViewById(R.id.value);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        dot.setBackground(drawable);

        tvLabel.setText(label);
        tvPercent.setText(percent);
    }

    private void playIntroAnimations() {
        // 1) появление карточки процентов снизу + лёгкая пружинка
        cardPercents.setTranslationY(80f);
        cardPercents.setAlpha(0f);

        cardPercents.animate()
                .translationY(0f)
                .alpha(1f)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .setDuration(750)
                .start();

        // 2) текст над диаграммой плавно появляется сверху
        tvMoodLabel.setTranslationY(-20f);
        tvMoodLabel.setAlpha(0f);
        tvMoodLabel.animate()
                .translationY(0f)
                .alpha(1f)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(700)
                .start();

        // 3) название эмоции с небольшой задержкой
        tvMoodTitle.setAlpha(0f);
        tvMoodTitle.animate()
                .alpha(1f)
                .setStartDelay(200)
                .setDuration(700)
                .start();

        // 4) Lottie: плавное появление + scale up + повтор с паузой
        lottieEmoji.setScaleX(0.85f);
        lottieEmoji.setScaleY(0.85f);
        lottieEmoji.setAlpha(0f);
        lottieEmoji.setRepeatCount(0);
        lottieEmoji.setRepeatMode(LottieDrawable.RESTART);
        lottieEmoji.removeAllAnimatorListeners();
        lottieEmoji.removeCallbacks(lottieLoopRunnable);

        AnimatorSet emojiIn = new AnimatorSet();
        emojiIn.playTogether(
                ObjectAnimator.ofFloat(lottieEmoji, View.ALPHA, 0f, 1f),
                ObjectAnimator.ofFloat(lottieEmoji, View.SCALE_X, 0.85f, 1f),
                ObjectAnimator.ofFloat(lottieEmoji, View.SCALE_Y, 0.85f, 1f)
        );
        emojiIn.setInterpolator(new OvershootInterpolator(2f));
        emojiIn.setDuration(850);

        emojiIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startLottieWithPause();
            }
        });
        emojiIn.start();
    }

    private void startLottieWithPause() {
        lottieEmoji.removeCallbacks(lottieLoopRunnable);
        lottieEmoji.removeAllAnimatorListeners();
        lottieEmoji.addAnimatorListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                lottieEmoji.removeAllAnimatorListeners();
                lottieEmoji.postDelayed(lottieLoopRunnable, LOTTIE_PAUSE_MS);
            }
        });
        lottieEmoji.playAnimation();
    }

    private void hookBounceOnScroll() {
        scroll.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            float overscroll = Math.max(0, -v.getScrollY());
            float scale = 1f + overscroll / 600f;
            scale = Math.min(scale, 1.08f);
            chartContainer.setScaleX(scale);
            chartContainer.setScaleY(scale);
        });
    }
}
