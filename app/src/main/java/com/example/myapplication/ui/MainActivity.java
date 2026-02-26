package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.os.LocaleListCompat;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityMainBinding;
import com.example.myapplication.util.QuotaManager;

/**
 * Ana Ekran - 4 modül kartı, geçmiş butonu, dil toggle ve kota göstergesi.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private QuotaManager quotaManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Dil değişiminden geliyorsa yumuşak fade-in
        if (savedInstanceState != null || getIntent().getBooleanExtra("lang_change", false)) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }

        quotaManager = QuotaManager.getInstance(this);

        setupCardAnimations();
        setupClickListeners();
        updateLanguageToggle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateQuotaCard();
    }

    private void setupCardAnimations() {
        View[] cards = {binding.cardQuota, binding.cardCleanup, binding.cardRemoveBg, binding.cardUpscale, binding.cardReplaceBg, binding.cardHistory};
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

    /**
     * Kota kartını günceller.
     * Hak varsa: yeşil tonlu, "X/8" badge
     * Hak bittiyse: turuncu tonlu, geri sayım
     */
    private void updateQuotaCard() {
        quotaManager.checkAndResetDailyQuota();
        int remaining = quotaManager.getRemainingCredits();
        int max = quotaManager.getMaxCredits();

        if (remaining > 0) {
            // Hakkı var — pozitif durum
            binding.txtQuotaIcon.setText("✨");
            binding.txtQuotaTitle.setText(getString(R.string.quota_title));
            binding.txtQuotaSubtitle.setText(getString(R.string.quota_remaining));
            binding.txtQuotaBadge.setText(remaining + "/" + max);
            binding.txtQuotaBadge.setTextColor(ContextCompat.getColor(this, R.color.primary));
            binding.txtQuotaTitle.setTextColor(resolveColorAttr(android.R.attr.textColorPrimary));
            binding.cardQuota.setStrokeColor(ContextCompat.getColor(this, R.color.primary));
        } else {
            // Hakkı bitti — geri sayım
            String timeLeft = quotaManager.getTimeUntilMidnight();
            binding.txtQuotaIcon.setText("⏳");
            binding.txtQuotaTitle.setText(getString(R.string.quota_exhausted_title));
            binding.txtQuotaSubtitle.setText(getString(R.string.quota_exhausted_subtitle, timeLeft));
            binding.txtQuotaBadge.setText("0/" + max);
            binding.txtQuotaBadge.setTextColor(ContextCompat.getColor(this, R.color.text_tertiary_light));
            binding.txtQuotaTitle.setTextColor(ContextCompat.getColor(this, R.color.text_secondary_light));
            binding.cardQuota.setStrokeColor(ContextCompat.getColor(this, R.color.text_tertiary_light));
        }
    }

    private int resolveColorAttr(int attr) {
        android.util.TypedValue typedValue = new android.util.TypedValue();
        getTheme().resolveAttribute(attr, typedValue, true);
        return ContextCompat.getColor(this, typedValue.resourceId);
    }

    private void updateLanguageToggle() {
        String currentLang = getCurrentLanguage();
        if ("tr".equals(currentLang)) {
            binding.btnLanguage.setText("\uD83C\uDDF9\uD83C\uDDF7 TR");
        } else {
            binding.btnLanguage.setText("\uD83C\uDDEC\uD83C\uDDE7 EN");
        }
    }

    private void toggleLanguage() {
        binding.btnLanguage.setEnabled(false);
        String currentLang = getCurrentLanguage();
        String newLang = "tr".equals(currentLang) ? "en" : "tr";

        binding.getRoot().animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(newLang)
                    );
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
