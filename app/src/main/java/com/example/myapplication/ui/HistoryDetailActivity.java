package com.example.myapplication.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.HistoryEntity;
import com.example.myapplication.databinding.ActivityHistoryDetailBinding;
import com.example.myapplication.util.BitmapUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Geçmiş Detay Activity.
 * Before/After slider, orijinal ve sonuç görselleri, modül bilgisi,
 * galeriye kaydetme ve silme işlemleri.
 */
public class HistoryDetailActivity extends AppCompatActivity {

    public static final String EXTRA_HISTORY_ID = "extra_history_id";
    public static final String EXTRA_MODULE_TYPE = "extra_module_type";
    public static final String EXTRA_ORIGINAL_PATH = "extra_original_path";
    public static final String EXTRA_RESULT_PATH = "extra_result_path";
    public static final String EXTRA_TIMESTAMP = "extra_timestamp";

    private ActivityHistoryDetailBinding binding;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private int historyId;
    private String moduleType;
    private String originalPath;
    private String resultPath;
    private long timestamp;

    private Bitmap originalBitmap;
    private Bitmap resultBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Intent'ten verileri al
        historyId = getIntent().getIntExtra(EXTRA_HISTORY_ID, -1);
        moduleType = getIntent().getStringExtra(EXTRA_MODULE_TYPE);
        originalPath = getIntent().getStringExtra(EXTRA_ORIGINAL_PATH);
        resultPath = getIntent().getStringExtra(EXTRA_RESULT_PATH);
        timestamp = getIntent().getLongExtra(EXTRA_TIMESTAMP, 0);

        setupToolbar();
        loadImages();
        displayInfo();
        setupButtons();
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
        binding.txtToolbarTitle.setText(moduleType != null ? moduleType : "Detay");
    }

    private void loadImages() {
        // Orijinal görsel
        if (originalPath != null) {
            File origFile = new File(originalPath);
            if (origFile.exists()) {
                originalBitmap = BitmapFactory.decodeFile(originalPath);
                binding.imgOriginal.setImageBitmap(originalBitmap);
            }
        }

        // Sonuç görseli
        if (resultPath != null) {
            File resultFile = new File(resultPath);
            if (resultFile.exists()) {
                resultBitmap = BitmapFactory.decodeFile(resultPath);
                binding.imgResult.setImageBitmap(resultBitmap);
            }
        }

        // Before/After Slider
        if (originalBitmap != null && resultBitmap != null) {
            binding.beforeAfterSlider.setImages(originalBitmap, resultBitmap);
        }
    }

    private void displayInfo() {
        // Modül tipi
        binding.txtModuleType.setText(moduleType != null ? moduleType : "Bilinmiyor");

        // Tarih
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("tr"));
        binding.txtDate.setText(sdf.format(new Date(timestamp)));
    }

    private void setupButtons() {
        // Galeriye kaydet
        binding.btnSaveGallery.setOnClickListener(v -> {
            if (resultBitmap != null) {
                try {
                    String fileName = "history_" + System.currentTimeMillis() + ".jpg";
                    Uri savedUri = BitmapUtils.saveBitmapToGallery(this, resultBitmap, fileName, false);
                    if (savedUri != null) {
                        Toast.makeText(this, getString(R.string.success_saved), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Sil
        binding.btnDelete.setOnClickListener(v -> {
            if (historyId != -1) {
                executor.execute(() -> {
                    HistoryEntity entity = new HistoryEntity(moduleType, originalPath, resultPath, timestamp);
                    entity.setId(historyId);
                    AppDatabase.getInstance(getApplication()).historyDao().deleteHistory(entity);

                    // Dosyaları da sil
                    deleteFileIfExists(originalPath);
                    deleteFileIfExists(resultPath);

                    runOnUiThread(() -> {
                        Toast.makeText(this, getString(R.string.history_deleted), Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
            }
        });
    }

    private void deleteFileIfExists(String path) {
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
