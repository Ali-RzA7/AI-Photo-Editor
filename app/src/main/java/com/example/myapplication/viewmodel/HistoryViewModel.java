package com.example.myapplication.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.HistoryDao;
import com.example.myapplication.database.HistoryEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Geçmiş ekranı ViewModel.
 * Room DAO ile geçmiş kayıtlarını yönetir.
 */
public class HistoryViewModel extends AndroidViewModel {

    private final HistoryDao historyDao;
    private final LiveData<List<HistoryEntity>> allHistory;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public HistoryViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getInstance(application);
        historyDao = db.historyDao();
        allHistory = historyDao.getAllHistory();
    }

    public LiveData<List<HistoryEntity>> getAllHistory() {
        return allHistory;
    }

    public void deleteHistory(HistoryEntity history) {
        executor.execute(() -> historyDao.deleteHistory(history));
    }

    /**
     * Statik yardımcı: herhangi bir Activity'den geçmiş kaydı eklemek için.
     */
    public static void saveToHistory(Application app, String moduleType,
                                      String originalPath, String resultPath) {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        exec.execute(() -> {
            HistoryEntity entity = new HistoryEntity(
                    moduleType, originalPath, resultPath, System.currentTimeMillis());
            AppDatabase.getInstance(app).historyDao().insertHistory(entity);
            exec.shutdown();
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
