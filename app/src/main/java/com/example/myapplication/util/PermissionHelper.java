package com.example.myapplication.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Modern depolama izinleri yönetimi.
 * - API 33+ (Android 13+): READ_MEDIA_IMAGES
 * - API 30-32 (Android 11-12): READ_EXTERNAL_STORAGE
 * - Kaydetme: MediaStore API kullanılır, izin gerektirmez.
 * - Kamera: CAMERA izni
 */
public class PermissionHelper {

    /**
     * Galeri okuma izni var mı kontrol eder.
     * Android 13+ için READ_MEDIA_IMAGES, altı için READ_EXTERNAL_STORAGE.
     */
    public static boolean hasGalleryReadPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33+)
            return ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED;
        } else {
            // Android 11-12 (API 30-32)
            return ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    /**
     * Galeri okuma izni ister.
     * ActivityResultLauncher<String> ile birlikte kullanılmalıdır.
     */
    public static void requestGalleryReadPermission(ActivityResultLauncher<String> launcher) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            launcher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    /**
     * Kamera izni var mı kontrol eder.
     */
    public static boolean hasCameraPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Kamera izni ister.
     */
    public static void requestCameraPermission(ActivityResultLauncher<String> launcher) {
        launcher.launch(Manifest.permission.CAMERA);
    }

    /**
     * İzin reddedildi ve "bir daha sorma" seçildi mi kontrol eder.
     */
    public static boolean shouldShowRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    /**
     * Galeri okuma için gerekli izin string'ini döndürür.
     */
    public static String getGalleryReadPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            return Manifest.permission.READ_EXTERNAL_STORAGE;
        }
    }
}
