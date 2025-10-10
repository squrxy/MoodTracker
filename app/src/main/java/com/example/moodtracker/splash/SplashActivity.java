package com.example.moodtracker.splash;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.example.moodtracker.MainActivity;
import com.example.moodtracker.R;
import com.example.moodtracker.auth.AuthActivity;
import com.example.moodtracker.auth.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final long SHOW_TIME_MS = 1500;  // “флэш” 1.5с
    private LottieAnimationView lottie;
    private SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        session = new SessionManager(this);
        lottie = findViewById(R.id.lottieSplash);

        // имитация warm-up: тут можно инициализировать Room/кэш/шрифты
        new Handler(Looper.getMainLooper()).postDelayed(this::zoomOutAndRoute, SHOW_TIME_MS);
    }

    private void zoomOutAndRoute() {
        // “раздуваем” Lottie на весь экран + fade
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator sx = ObjectAnimator.ofFloat(lottie, "scaleX", 1f, 6f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(lottie, "scaleY", 1f, 6f);
        ObjectAnimator a  = ObjectAnimator.ofFloat(lottie, "alpha", 1f, 0f);
        set.setDuration(550);
        set.playTogether(sx, sy, a);
        set.start();

        // определяем, куда идти
        set.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animation) {
                if (session.hasSession()) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                } else {
                    startActivity(new Intent(SplashActivity.this, AuthActivity.class));
                }
                finish();
            }
        });
    }
}
