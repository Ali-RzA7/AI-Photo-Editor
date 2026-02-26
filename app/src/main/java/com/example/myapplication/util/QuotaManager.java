package com.example.myapplication.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * Günlük kullanım kotası yöneticisi.
 * SharedPreferences ile kredi sayısını ve son sıfırlama zamanını tutar.
 * Her gece yarısı (00:00) otomatik olarak 8 krediye sıfırlanır.
 */
public class QuotaManager {

    private static final String PREFS_NAME = "quota_prefs";
    private static final String KEY_REMAINING = "remaining_credits";
    private static final String KEY_LAST_RESET = "last_reset_time";
    private static final int MAX_CREDITS = 8;

    private static QuotaManager instance;
    private final SharedPreferences prefs;

    private QuotaManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        checkAndResetDailyQuota();
    }

    public static synchronized QuotaManager getInstance(Context context) {
        if (instance == null) {
            instance = new QuotaManager(context);
        }
        return instance;
    }

    /**
     * Yeni bir güne girilip girilmediğini kontrol eder.
     * Eğer yeni gün başlamışsa krediyi MAX_CREDITS'e sıfırlar.
     */
    public void checkAndResetDailyQuota() {
        long lastReset = prefs.getLong(KEY_LAST_RESET, 0);
        long todayStart = getTodayMidnight();

        if (lastReset < todayStart) {
            // Yeni gün — kredileri sıfırla
            prefs.edit()
                    .putInt(KEY_REMAINING, MAX_CREDITS)
                    .putLong(KEY_LAST_RESET, todayStart)
                    .apply();
        }
    }

    /**
     * Kalan kredi sayısını döndürür.
     */
    public int getRemainingCredits() {
        checkAndResetDailyQuota();
        return prefs.getInt(KEY_REMAINING, MAX_CREDITS);
    }

    /**
     * Maksimum kredi sayısını döndürür.
     */
    public int getMaxCredits() {
        return MAX_CREDITS;
    }

    /**
     * Kredi olup olmadığını kontrol eder.
     */
    public boolean hasCredits() {
        return getRemainingCredits() > 0;
    }

    /**
     * Bir kredi düşürür.
     * @return true → kredi başarıyla düşürüldü, false → kredi kalmamış.
     */
    public boolean useCredit() {
        checkAndResetDailyQuota();
        int remaining = prefs.getInt(KEY_REMAINING, MAX_CREDITS);
        if (remaining > 0) {
            prefs.edit().putInt(KEY_REMAINING, remaining - 1).apply();
            return true;
        }
        return false;
    }

    /**
     * Gece yarısına kalan süreyi "X Saat Y Dk" formatında döndürür.
     */
    public String getTimeUntilMidnight() {
        Calendar now = Calendar.getInstance();
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        midnight.add(Calendar.DAY_OF_MONTH, 1);

        long diffMs = midnight.getTimeInMillis() - now.getTimeInMillis();
        long hours = diffMs / (1000 * 60 * 60);
        long minutes = (diffMs / (1000 * 60)) % 60;

        return String.format("%02d Saat %02d Dk", hours, minutes);
    }

    /**
     * Gece yarısına kalan süreyi İngilizce formatında döndürür.
     */
    public String getTimeUntilMidnightEn() {
        Calendar now = Calendar.getInstance();
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        midnight.add(Calendar.DAY_OF_MONTH, 1);

        long diffMs = midnight.getTimeInMillis() - now.getTimeInMillis();
        long hours = diffMs / (1000 * 60 * 60);
        long minutes = (diffMs / (1000 * 60)) % 60;

        return String.format("%02d Hours %02d Min", hours, minutes);
    }

    /**
     * Bugünün gece yarısı zamanını milisaniye olarak döndürür.
     */
    private long getTodayMidnight() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
