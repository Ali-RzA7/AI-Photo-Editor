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
 * Nesne/Kişi Silme (Cleanup) ViewModel.
 */
public class CleanupViewModel extends AndroidViewModel {

    private final MutableLiveData<ApiResult<Bitmap>> resultLiveData = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> originalBitmapLiveData = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ClipDropRepository repository;

    public CleanupViewModel(@NonNull Application application) {
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
     * Cleanup API çağrısı yapar.
     */
    public void processCleanup(Bitmap originalBitmap, Bitmap maskBitmap) {
        resultLiveData.postValue(ApiResult.loading());

        executor.execute(() -> {
            try {
                Bitmap result = repository.cleanup(originalBitmap, maskBitmap);
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
