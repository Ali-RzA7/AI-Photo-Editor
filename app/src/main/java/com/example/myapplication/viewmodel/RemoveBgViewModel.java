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
 * Arka Plan Silme ViewModel.
 */
public class RemoveBgViewModel extends AndroidViewModel {

    private final MutableLiveData<ApiResult<Bitmap>> resultLiveData = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> originalBitmapLiveData = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ClipDropRepository repository;

    public RemoveBgViewModel(@NonNull Application application) {
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
     * Remove Background API çağrısı yapar.
     */
    public void processRemoveBackground(Bitmap originalBitmap) {
        resultLiveData.postValue(ApiResult.loading());

        executor.execute(() -> {
            try {
                Bitmap result = repository.removeBackground(originalBitmap);
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
