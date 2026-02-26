package com.example.myapplication.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityOnboardingBinding;

/**
 * Onboarding Activity — 3 sayfalık tanıtım.
 * ViewPager2 + nokta belirteçleri + Geç / İleri / Başla butonları.
 * İlk girişte gösterilir, SharedPreferences ile kaydedilir.
 */
public class OnboardingActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ai_photo_editor_prefs";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";

    private ActivityOnboardingBinding binding;
    private int pageCount = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewPager();
        setupDots(0);
        setupButtons();
    }

    private void setupViewPager() {
        int[] icons = {
                android.R.drawable.ic_input_add,      // Sihirli değnek yerine
                android.R.drawable.ic_menu_zoom,       // HD kalite
                android.R.drawable.ic_menu_recent_history // Arşiv/Geçmiş
        };

        String[] titles = {
                getString(R.string.onboarding_title_1),
                getString(R.string.onboarding_title_2),
                getString(R.string.onboarding_title_3)
        };

        String[] descriptions = {
                getString(R.string.onboarding_desc_1),
                getString(R.string.onboarding_desc_2),
                getString(R.string.onboarding_desc_3)
        };

        OnboardingAdapter adapter = new OnboardingAdapter(icons, titles, descriptions);
        binding.viewPager.setAdapter(adapter);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setupDots(position);
                updateButtons(position);
            }
        });
    }

    private void setupDots(int currentPage) {
        binding.layoutDots.removeAllViews();

        for (int i = 0; i < pageCount; i++) {
            ImageView dot = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    i == currentPage ? 28 : 10,
                    10
            );
            params.setMargins(6, 0, 6, 0);
            dot.setLayoutParams(params);

            if (i == currentPage) {
                dot.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
            } else {
                dot.setBackgroundColor(ContextCompat.getColor(this, R.color.text_tertiary_light));
            }

            // Yuvarlak köşeler için
            dot.setBackground(createDotDrawable(i == currentPage));

            binding.layoutDots.addView(dot);
        }
    }

    private android.graphics.drawable.GradientDrawable createDotDrawable(boolean isActive) {
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(5f);
        if (isActive) {
            drawable.setColor(ContextCompat.getColor(this, R.color.primary));
        } else {
            drawable.setColor(ContextCompat.getColor(this, R.color.text_tertiary_light));
        }
        return drawable;
    }

    private void updateButtons(int position) {
        if (position == pageCount - 1) {
            // Son sayfa
            binding.btnNext.setText(getString(R.string.onboarding_start));
            binding.btnSkip.setVisibility(android.view.View.INVISIBLE);
        } else {
            binding.btnNext.setText(getString(R.string.onboarding_next));
            binding.btnSkip.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void setupButtons() {
        // Geç
        binding.btnSkip.setOnClickListener(v -> finishOnboarding());

        // İleri / Başla
        binding.btnNext.setOnClickListener(v -> {
            int current = binding.viewPager.getCurrentItem();
            if (current < pageCount - 1) {
                binding.viewPager.setCurrentItem(current + 1, true);
            } else {
                finishOnboarding();
            }
        });
    }

    private void finishOnboarding() {
        // Bir daha gösterme
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
