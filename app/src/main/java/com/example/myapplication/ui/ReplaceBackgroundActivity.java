package com.example.myapplication.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityReplaceBgBinding;
import com.example.myapplication.util.BitmapUtils;
import com.example.myapplication.util.PermissionHelper;
import com.example.myapplication.viewmodel.HistoryViewModel;
import com.example.myapplication.viewmodel.ReplaceBgViewModel;

/**
 * AI Arka Plan Değiştirme Activity.
 * Akış: Görsel Seç → Prompt Yaz → API ile Arka Plan Üret → Before/After Slider → Kaydet
 */
public class ReplaceBackgroundActivity extends AppCompatActivity {

    private ActivityReplaceBgBinding binding;
    private ReplaceBgViewModel viewModel;
    private Bitmap originalBitmap;
    private Bitmap resultBitmap;

    // Görsel seçici (önce tanımlanmalı — forward reference)
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    viewModel.loadImage(uri);
                }
            });

    // Galeri izni sonucu
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    imagePickerLauncher.launch("image/*");
                } else {
                    Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReplaceBgBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ReplaceBgViewModel.class);

        setupToolbar();
        setupClickListeners();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        // Dropzone tıklama
        binding.btnSelectImage.setOnClickListener(v -> openGalleryWithPermission());

        // Alt buton ile galeri aç
        binding.btnSelectImageAlt.setOnClickListener(v -> openGalleryWithPermission());

        // Prompt ile işle
        binding.btnProcess.setOnClickListener(v -> {
            if (originalBitmap != null) {
                String prompt = "";
                if (binding.etPrompt.getText() != null) {
                    prompt = binding.etPrompt.getText().toString().trim();
                }
                viewModel.processReplaceBackground(originalBitmap, prompt);
            }
        });

        // Kaydet
        binding.btnSave.setOnClickListener(v -> {
            if (resultBitmap != null) {
                saveResult();
            }
        });
    }

    private void openGalleryWithPermission() {
        if (PermissionHelper.hasGalleryReadPermission(this)) {
            imagePickerLauncher.launch("image/*");
        } else {
            PermissionHelper.requestGalleryReadPermission(permissionLauncher);
        }
    }

    private void observeViewModel() {
        viewModel.getOriginalBitmapLiveData().observe(this, bitmap -> {
            if (bitmap != null) {
                originalBitmap = bitmap;
                showPreviewState();
                binding.imgOriginal.setImageBitmap(bitmap);
            }
        });

        viewModel.getResultLiveData().observe(this, result -> {
            if (result == null) return;

            switch (result.getStatus()) {
                case LOADING:
                    showLoading(true);
                    break;
                case SUCCESS:
                    showLoading(false);
                    resultBitmap = result.getData();
                    showResultState();
                    saveHistory();
                    com.example.myapplication.util.QuotaManager.getInstance(this).useCredit();
                    break;
                case ERROR:
                    showLoading(false);
                    Toast.makeText(this, result.getError(), Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void showPreviewState() {
        binding.layoutSelectImage.setVisibility(View.GONE);
        binding.layoutPreview.setVisibility(View.VISIBLE);
        binding.layoutResult.setVisibility(View.GONE);
        binding.btnSave.setVisibility(View.GONE);
    }

    private void showResultState() {
        binding.layoutSelectImage.setVisibility(View.GONE);
        binding.layoutPreview.setVisibility(View.GONE);
        binding.layoutResult.setVisibility(View.VISIBLE);
        binding.btnSave.setVisibility(View.VISIBLE);

        // Şeffaf PNG'lerde "Önce" ve "Sonra" aynı görünmesin diye
        // orijinali beyaz arka plan üzerine composite et
        Bitmap beforeBitmap = flattenTransparency(originalBitmap);
        binding.beforeAfterSlider.setImages(beforeBitmap, resultBitmap);
    }

    /**
     * Şeffaf bitmap'i beyaz arka plan üzerine çizerek opak hale getirir.
     * Bu sayede BeforeAfterSliderView'da "Önce" tarafında
     * şeffaf pikseller altındaki "Sonra" görseli görünmez.
     */
    private Bitmap flattenTransparency(Bitmap source) {
        Bitmap flattened = Bitmap.createBitmap(
                source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(flattened);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(source, 0, 0, null);
        return flattened;
    }

    private void showLoading(boolean show) {
        binding.layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void saveResult() {
        try {
            String fileName = "replacebg_" + System.currentTimeMillis() + ".jpg";
            Uri savedUri = BitmapUtils.saveBitmapToGallery(this, resultBitmap, fileName, false);
            if (savedUri != null) {
                Toast.makeText(this, getString(R.string.success_saved), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_occurred) + ": " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void saveHistory() {
        try {
            long ts = System.currentTimeMillis();
            String origPath = BitmapUtils.saveToInternalStorage(this, originalBitmap, "replacebg_orig_" + ts + ".jpg");
            String resultPath = BitmapUtils.saveToInternalStorage(this, resultBitmap, "replacebg_result_" + ts + ".jpg");
            HistoryViewModel.saveToHistory(getApplication(), getString(R.string.module_replacebg), origPath, resultPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
