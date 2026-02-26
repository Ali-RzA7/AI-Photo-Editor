package com.example.myapplication.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.myapplication.model.ApiResult;
import com.example.myapplication.repository.ClipDropRepository;
import com.example.myapplication.util.BitmapUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Görüntü Kalite Artırma ViewModel.
 */
public class UpscaleViewModel extends AndroidViewModel {

    private final MutableLiveData<ApiResult<Bitmap>> resultLiveData = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> originalBitmapLiveData = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ClipDropRepository repository;

    public UpscaleViewModel(@NonNull Application application) {
        super(application);
        repository = ClipDropRepository.getInstance();
    }

    public LiveData<ApiResult<Bitmap>> getResultLiveData() {
        return resultLiveData;
    }

    public LiveData<Bitmap> getOriginalBitmapLiveData() {
        return originalBitmapLiveData;
    }

    /**
     * Seçilen görseli yükler.
     */
    public void loadImage(Uri imageUri) {
        executor.execute(() -> {
            try {
                Bitmap bitmap = BitmapUtils.loadBitmapForApi(getApplication(), imageUri);
                originalBitmapLiveData.postValue(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Upscale API çağrısı yapar.
     * Orijinal boyutu 2 katına çıkarır (max 4096).
     */
    public void processUpscale(Bitmap originalBitmap) {
        resultLiveData.postValue(ApiResult.loading());

        executor.execute(() -> {
            try {
                int targetWidth = Math.min(originalBitmap.getWidth() * 2, 4096);
                int targetHeight = Math.min(originalBitmap.getHeight() * 2, 4096);

                // Oran koruma: eğer biri 4096'yı aşıyorsa, oranı koru
                float scaleW = (float) targetWidth / originalBitmap.getWidth();
                float scaleH = (float) targetHeight / originalBitmap.getHeight();
                float scale = Math.min(scaleW, scaleH);

                targetWidth = Math.round(originalBitmap.getWidth() * scale);
                targetHeight = Math.round(originalBitmap.getHeight() * scale);

                Bitmap result = repository.upscale(originalBitmap, targetWidth, targetHeight);
                resultLiveData.postValue(ApiResult.success(result));
            } catch (Exception e) {
                resultLiveData.postValue(ApiResult.error(e.getMessage()));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
