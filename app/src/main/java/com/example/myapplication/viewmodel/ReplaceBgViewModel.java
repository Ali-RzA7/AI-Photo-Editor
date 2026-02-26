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
 * Arka Plan Değiştirme ViewModel.
 * Görsel yükleme ve Replace Background API çağrısını yönetir.
 */
public class ReplaceBgViewModel extends AndroidViewModel {

    private final MutableLiveData<Bitmap> originalBitmapLiveData = new MutableLiveData<>();
    private final MutableLiveData<ApiResult<Bitmap>> resultLiveData = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ClipDropRepository repository = ClipDropRepository.getInstance();

    public ReplaceBgViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Bitmap> getOriginalBitmapLiveData() {
        return originalBitmapLiveData;
    }

    public LiveData<ApiResult<Bitmap>> getResultLiveData() {
        return resultLiveData;
    }

    /**
     * URI'den görsel yükler.
     */
    public void loadImage(Uri uri) {
        executor.execute(() -> {
            try {
                Bitmap bitmap = BitmapUtils.loadBitmapForApi(getApplication(), uri);
                originalBitmapLiveData.postValue(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Replace Background API çağrısı yapar.
     * Bitmap otomatik olarak 2048x2048'e küçültülür (repository katmanında).
     */
    public void processReplaceBackground(Bitmap originalBitmap, String prompt) {
        resultLiveData.postValue(ApiResult.loading());

        executor.execute(() -> {
            try {
                Bitmap result = repository.replaceBackground(originalBitmap, prompt);
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
