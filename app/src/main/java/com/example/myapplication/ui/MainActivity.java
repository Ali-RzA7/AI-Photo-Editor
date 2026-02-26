package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.databinding.ActivityMainBinding;

/**
 * Ana Ekran - 4 modül kartını ve geçmiş butonunu gösterir.
 * Her kart dokunma animasyonu ile ilgili Activity'ye yönlendirir.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupCardAnimations();
        setupClickListeners();
    }

    private void setupCardAnimations() {
        // Açılış animasyonları
        View[] cards = {binding.cardCleanup, binding.cardRemoveBg, binding.cardUpscale, binding.cardReplaceBg, binding.cardHistory};
        for (int i = 0; i < cards.length; i++) {
            cards[i].setAlpha(0f);
            cards[i].setTranslationY(100f);
            cards[i].animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setStartDelay(200L + (i * 150L))
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void setupClickListeners() {
        binding.cardCleanup.setOnClickListener(v -> {
            animateCardClick(v, () -> {
                startActivity(new Intent(this, CleanupActivity.class));
            });
        });

        binding.cardRemoveBg.setOnClickListener(v -> {
            animateCardClick(v, () -> {
                startActivity(new Intent(this, RemoveBackgroundActivity.class));
            });
        });

        binding.cardUpscale.setOnClickListener(v -> {
            animateCardClick(v, () -> {
                startActivity(new Intent(this, UpscaleActivity.class));
            });
        });

        binding.cardReplaceBg.setOnClickListener(v -> {
            animateCardClick(v, () -> {
                startActivity(new Intent(this, ReplaceBackgroundActivity.class));
            });
        });

        binding.cardHistory.setOnClickListener(v -> {
            animateCardClick(v, () -> {
                startActivity(new Intent(this, HistoryActivity.class));
            });
        });
    }

    /**
     * Kart dokunma animasyonu (scale down → scale up → action).
     */
    private void animateCardClick(View view, Runnable action) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .withEndAction(action)
                        .start())
                .start();
    }
}
