package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityMainBinding;

/**
 * Ana Ekran - 4 modül kartını, geçmiş butonunu ve dil toggle'ını gösterir.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Dil değişimi sonrası yumuşak fade-in
        binding.getRoot().setAlpha(0f);
        binding.getRoot().animate()
                .alpha(1f)
                .setDuration(300)
                .setStartDelay(50)
                .start();

        setupCardAnimations();
        setupClickListeners();
        updateLanguageToggle();
    }

    /**
     * recreate() yerine finish + startActivity kullanarak siyah ekranı önler.
     */
    @Override
    public void recreate() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(getIntent());
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void setupCardAnimations() {
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
        binding.cardCleanup.setOnClickListener(v ->
                animateCardClick(v, () -> startActivity(new Intent(this, CleanupActivity.class))));

        binding.cardRemoveBg.setOnClickListener(v ->
                animateCardClick(v, () -> startActivity(new Intent(this, RemoveBackgroundActivity.class))));

        binding.cardUpscale.setOnClickListener(v ->
                animateCardClick(v, () -> startActivity(new Intent(this, UpscaleActivity.class))));

        binding.cardReplaceBg.setOnClickListener(v ->
                animateCardClick(v, () -> startActivity(new Intent(this, ReplaceBackgroundActivity.class))));

        binding.cardHistory.setOnClickListener(v ->
                animateCardClick(v, () -> startActivity(new Intent(this, HistoryActivity.class))));

        // Dil Toggle — tek tıkla TR↔EN geçişi
        binding.btnLanguage.setOnClickListener(v -> toggleLanguage());
    }

    private void updateLanguageToggle() {
        String currentLang = getCurrentLanguage();
        if ("tr".equals(currentLang)) {
            binding.btnLanguage.setText("\uD83C\uDDF9\uD83C\uDDF7 TR");
        } else {
            binding.btnLanguage.setText("\uD83C\uDDEC\uD83C\uDDE7 EN");
        }
    }

    /**
     * Önce UI'ı fade-out yapar, sonra dili değiştirir.
     * Böylece kullanıcı siyah ekran görmez, yumuşak geçiş yaşar.
     */
    private void toggleLanguage() {
        binding.btnLanguage.setEnabled(false);
        String currentLang = getCurrentLanguage();
        String newLang = "tr".equals(currentLang) ? "en" : "tr";

        // Fade out → dil değiştir
        binding.getRoot().animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(newLang)
                    );
                })
                .start();
    }

    private String getCurrentLanguage() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (!locales.isEmpty()) {
            return locales.get(0).getLanguage();
        }
        return java.util.Locale.getDefault().getLanguage();
    }

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
