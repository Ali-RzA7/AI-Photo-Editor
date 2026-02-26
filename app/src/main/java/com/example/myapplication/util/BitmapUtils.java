package com.example.myapplication.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Bitmap işlemleri için yardımcı sınıf.
 * OOM hatalarını önlemek için ölçeklendirme ve optimize edilmiş yükleme sağlar.
 */
public class BitmapUtils {

    private static final int MAX_DIMENSION = 2048;

    /**
     * URI'den optimize edilmiş Bitmap yükler (OOM koruması).
     * Önce boyutları okur, sonra uygun inSampleSize ile ölçeklendirir.
     */
    public static Bitmap decodeSampledBitmapFromUri(Context context, Uri uri, int reqWidth, int reqHeight) throws IOException {
        ContentResolver resolver = context.getContentResolver();

        // Önce sadece boyutları oku
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream is = resolver.openInputStream(uri)) {
            BitmapFactory.decodeStream(is, null, options);
        }

        // inSampleSize hesapla
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        try (InputStream is = resolver.openInputStream(uri)) {
            return BitmapFactory.decodeStream(is, null, options);
        }
    }

    /**
     * URI'den orijinal boyutta Bitmap yükler (API'ye göndermek için).
     * Yine de çok büyük görselleri MAX_DIMENSION'a ölçeklendirir.
     */
    public static Bitmap loadBitmapForApi(Context context, Uri uri) throws IOException {
        ContentResolver resolver = context.getContentResolver();

        // Boyutları öğren
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try (InputStream is = resolver.openInputStream(uri)) {
            BitmapFactory.decodeStream(is, null, options);
        }

        int width = options.outWidth;
        int height = options.outHeight;

        // Çok büyükse ölçekle
        if (width > MAX_DIMENSION || height > MAX_DIMENSION) {
            float scale = Math.min((float) MAX_DIMENSION / width, (float) MAX_DIMENSION / height);
            int targetW = Math.round(width * scale);
            int targetH = Math.round(height * scale);
            options.inSampleSize = calculateInSampleSize(options, targetW, targetH);
        } else {
            options.inSampleSize = 1;
        }

        options.inJustDecodeBounds = false;
        try (InputStream is = resolver.openInputStream(uri)) {
            return BitmapFactory.decodeStream(is, null, options);
        }
    }

    /**
     * Bitmap'i PNG formatında byte dizisine çevirir.
     */
    public static byte[] bitmapToPngBytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    /**
     * Bitmap'i JPEG formatında byte dizisine çevirir.
     */
    public static byte[] bitmapToJpegBytes(Bitmap bitmap, int quality) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }

    /**
     * Byte dizisinden Bitmap oluşturur.
     */
    public static Bitmap bytesToBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Bitmap'i galeriye kaydeder (MediaStore API kullanarak).
     */
    public static Uri saveBitmapToGallery(Context context, Bitmap bitmap, String fileName, boolean isPng) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, isPng ? "image/png" : "image/jpeg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AIPhotoEditor");

        ContentResolver resolver = context.getContentResolver();
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (imageUri != null) {
            try (OutputStream os = resolver.openOutputStream(imageUri)) {
                if (os != null) {
                    if (isPng) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                    } else {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, os);
                    }
                }
            }
        }

        return imageUri;
    }

    /**
     * Bitmap'i geçici dosyaya yazar (API'ye göndermek için).
     */
    public static File bitmapToTempFile(Context context, Bitmap bitmap, String prefix, boolean isPng) throws IOException {
        File tempFile = File.createTempFile(prefix, isPng ? ".png" : ".jpg", context.getCacheDir());
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            if (isPng) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            }
        }
        return tempFile;
    }

    /**
     * Bitmap'i uygulamanın dahili deposuna kaydeder ve dosya yolunu döndürür.
     * Room Database geçmişi için kullanılır — BLOB yerine dosya yolu saklanır.
     */
    public static String saveToInternalStorage(Context context, Bitmap bitmap, String fileName) throws IOException {
        File dir = new File(context.getFilesDir(), "history");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            boolean isPng = fileName.endsWith(".png");
            if (isPng) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            }
        }
        return file.getAbsolutePath();
    }

    /**
     * Uygun inSampleSize hesaplar (2'nin katları).
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
