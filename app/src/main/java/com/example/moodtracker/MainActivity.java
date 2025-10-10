package com.example.moodtracker;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;

import com.example.moodtracker.ui.DiaryFragment;
import com.example.moodtracker.ui.HomeFragment;
import com.example.moodtracker.ui.SettingsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.transition.platform.MaterialFadeThrough;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private View fragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // рисуем за системные бары
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        // сами делаем бары прозрачными + правильные иконки
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        WindowInsetsControllerCompat c = new WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        c.setAppearanceLightStatusBars(false);     // тёмные иконки? false = светлые (для тёмной темы)
        c.setAppearanceLightNavigationBars(false); // тоже светлые иконки

        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_nav);
        fragmentContainer = findViewById(R.id.fragment_container);
        View root = findViewById(android.R.id.content);

        // инсетсы: даём паддинги контейнеру и навбару
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            fragmentContainer.setPadding(sys.left, sys.top, sys.right, 0);
            bottomNav.setPadding(sys.left, 0, sys.right, sys.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        if (savedInstanceState == null) {
            switchFragment(new HomeFragment());
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment f;
            int id = item.getItemId();
            if (id == R.id.nav_home) f = new HomeFragment();
            else if (id == R.id.nav_diary) f = new DiaryFragment();
            else f = new SettingsFragment();
            switchFragment(f);
            return true;
        });
    }

    private void switchFragment(@NonNull Fragment fragment) {
        MaterialFadeThrough fade = new MaterialFadeThrough();
        fade.setDuration(350); // плавнее
        fragment.setEnterTransition(fade);
        fragment.setExitTransition(fade);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
