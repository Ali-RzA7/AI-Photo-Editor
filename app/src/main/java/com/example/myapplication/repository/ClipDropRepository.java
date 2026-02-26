package com.example.myapplication.repository;

import android.graphics.Bitmap;

import com.example.myapplication.api.ClipDropApiService;
import com.example.myapplication.api.RetrofitClient;
import com.example.myapplication.util.BitmapUtils;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Repository sınıfı - Clipdrop API çağrılarını saran katman.
 * Tüm API çağrıları senkron olarak yapılır, ViewModel'den ExecutorService ile çağrılmalıdır.
 */
public class ClipDropRepository {

    private static ClipDropRepository instance;
    private final ClipDropApiService apiService;

    private ClipDropRepository() {
        apiService = RetrofitClient.getInstance().getApiService();
    }

    public static synchronized ClipDropRepository getInstance() {
        if (instance == null) {
            instance = new ClipDropRepository();
        }
        return instance;
    }

    /**
     * Arka Plan Değiştirme API çağrısı.
     * Görseli göndermeden önce max 2048x2048'e küçültür.
     *
     * @param imageBitmap Orijinal görsel
     * @param prompt      Yeni arka plan açıklaması
     * @return Yeni arka planlı Bitmap
     */
    public Bitmap replaceBackground(Bitmap imageBitmap, String prompt) throws IOException {
        // API limiti: max 2048x2048
        Bitmap scaledBitmap = scaleDownToMax(imageBitmap, 2048);

        byte[] imageBytes = BitmapUtils.bitmapToJpegBytes(scaledBitmap, 90);

        RequestBody imageBody = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image_file", "image.jpg", imageBody);

        RequestBody promptBody = RequestBody.create(MediaType.parse("text/plain"), prompt != null ? prompt : "");

        Response<ResponseBody> response = apiService.replaceBackground(imagePart, promptBody).execute();

        if (response.isSuccessful() && response.body() != null) {
            byte[] responseBytes = response.body().bytes();
            return BitmapUtils.bytesToBitmap(responseBytes);
        } else {
            String errorMsg = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            throw new IOException("API Error (" + response.code() + "): " + errorMsg);
        }
    }

    /**
     * Nesne/Kişi Silme API çağrısı.
     *
     * @param imageBitmap Orijinal görsel
     * @param maskBitmap  Maske görseli (siyah-beyaz PNG)
     * @return İşlenmiş görsel Bitmap veya null
     */
    public Bitmap cleanup(Bitmap imageBitmap, Bitmap maskBitmap) throws IOException {
        byte[] imageBytes = BitmapUtils.bitmapToPngBytes(imageBitmap);
        byte[] maskBytes = BitmapUtils.bitmapToPngBytes(maskBitmap);

        RequestBody imageBody = RequestBody.create(MediaType.parse("image/png"), imageBytes);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image_file", "image.png", imageBody);

        RequestBody maskBody = RequestBody.create(MediaType.parse("image/png"), maskBytes);
        MultipartBody.Part maskPart = MultipartBody.Part.createFormData("mask_file", "mask.png", maskBody);

        Response<ResponseBody> response = apiService.cleanup(imagePart, maskPart).execute();

        if (response.isSuccessful() && response.body() != null) {
            byte[] responseBytes = response.body().bytes();
            return BitmapUtils.bytesToBitmap(responseBytes);
        } else {
            String errorMsg = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            throw new IOException("API Error (" + response.code() + "): " + errorMsg);
        }
    }

    /**
     * Arka Plan Silme API çağrısı.
     *
     * @param imageBitmap Orijinal görsel
     * @return Şeffaf PNG olarak Bitmap
     */
    public Bitmap removeBackground(Bitmap imageBitmap) throws IOException {
        byte[] imageBytes = BitmapUtils.bitmapToPngBytes(imageBitmap);

        RequestBody imageBody = RequestBody.create(MediaType.parse("image/png"), imageBytes);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image_file", "image.png", imageBody);

        Response<ResponseBody> response = apiService.removeBackground(imagePart).execute();

        if (response.isSuccessful() && response.body() != null) {
            byte[] responseBytes = response.body().bytes();
            return BitmapUtils.bytesToBitmap(responseBytes);
        } else {
            String errorMsg = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            throw new IOException("API Error (" + response.code() + "): " + errorMsg);
        }
    }

    /**
     * Görüntü Kalite Artırma API çağrısı.
     *
     * @param imageBitmap    Orijinal görsel
     * @param targetWidth    Hedef genişlik (max 4096)
     * @param targetHeight   Hedef yükseklik (max 4096)
     * @return Upscale edilmiş Bitmap
     */
    public Bitmap upscale(Bitmap imageBitmap, int targetWidth, int targetHeight) throws IOException {
        byte[] imageBytes = BitmapUtils.bitmapToPngBytes(imageBitmap);

        RequestBody imageBody = RequestBody.create(MediaType.parse("image/png"), imageBytes);
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image_file", "image.png", imageBody);

        RequestBody widthBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(targetWidth));
        RequestBody heightBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(targetHeight));

        Response<ResponseBody> response = apiService.upscale(imagePart, widthBody, heightBody).execute();

        if (response.isSuccessful() && response.body() != null) {
            byte[] responseBytes = response.body().bytes();
            return BitmapUtils.bytesToBitmap(responseBytes);
        } else {
            String errorMsg = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
            throw new IOException("API Error (" + response.code() + "): " + errorMsg);
        }
    }

    /**
     * Bitmap'i belirtilen maksimum boyuta oranını koruyarak küçültür.
     */
    private Bitmap scaleDownToMax(Bitmap bitmap, int maxDimension) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w <= maxDimension && h <= maxDimension) {
            return bitmap;
        }
        float scale = Math.min((float) maxDimension / w, (float) maxDimension / h);
        int newW = Math.round(w * scale);
        int newH = Math.round(h * scale);
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true);
    }
}
