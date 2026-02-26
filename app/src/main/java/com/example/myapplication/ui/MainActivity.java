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

        // Dil değişiminden geliyorsa yumuşak fade-in uygula
        if (savedInstanceState != null || getIntent().getBooleanExtra("lang_change", false)) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }

        setupCardAnimations();
        setupClickListeners();
        updateLanguageToggle();
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

        // Dil Toggle
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
     * Dil değiştirme akışı:
     * 1. Butonu devre dışı bırak
     * 2. UI'ı fade-out et (200ms)
     * 3. Activity'yi kapat + yeni dille yeniden başlat (fade geçişli)
     * 4. setApplicationLocales çağır
     *
     * Siyah ekran asla görünmez çünkü:
     * - finish + startActivity arasında fade animasyonu
     * - Yeni Activity windowBackground ile uygulamanın arka plan rengini gösterir
     */
    private void toggleLanguage() {
        binding.btnLanguage.setEnabled(false);
        String currentLang = getCurrentLanguage();
        String newLang = "tr".equals(currentLang) ? "en" : "tr";

        // UI fade out
        binding.getRoot().animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    // Önce dili ayarla
                    AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(newLang)
                    );

                    // Activity'yi yumuşak geçişle yeniden başlat
                    Intent intent = getIntent();
                    intent.putExtra("lang_change", true);
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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
