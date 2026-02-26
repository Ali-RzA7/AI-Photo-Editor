package com.example.myapplication.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

/**
 * Splash Activity — Android 12+ SplashScreen API kullanır.
 * İlk girişte OnboardingActivity'ye, sonrakilerde MainActivity'ye yönlendirir.
 */
public class SplashActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "ai_photo_editor_prefs";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // SplashScreen API — tema geçişi
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // SharedPreferences ile ilk giriş kontrolü
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean onboardingDone = prefs.getBoolean(KEY_ONBOARDING_DONE, false);

        if (onboardingDone) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            startActivity(new Intent(this, OnboardingActivity.class));
        }

        finish();
    }
}
