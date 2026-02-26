package com.example.myapplication.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityUpscaleBinding;
import com.example.myapplication.util.BitmapUtils;
import com.example.myapplication.util.PermissionHelper;
import com.example.myapplication.viewmodel.HistoryViewModel;
import com.example.myapplication.viewmodel.UpscaleViewModel;

/**
 * Görüntü Kalite Artırma Activity.
 * Akış: Görsel Seç → Boyut Bilgisi → API ile Upscale → Önce/Sonra Slider → Kaydet
 */
public class UpscaleActivity extends AppCompatActivity {

    private ActivityUpscaleBinding binding;
    private UpscaleViewModel viewModel;
    private Bitmap originalBitmap;
    private Bitmap resultBitmap;

    // Görsel seçici
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
        binding = ActivityUpscaleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(UpscaleViewModel.class);

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

        binding.btnProcess.setOnClickListener(v -> {
            if (originalBitmap != null) {
                viewModel.processUpscale(originalBitmap);
            }
        });

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

                // Boyut bilgisi
                int w = bitmap.getWidth();
                int h = bitmap.getHeight();
                int targetW = Math.min(w * 2, 4096);
                int targetH = Math.min(h * 2, 4096);
                String dimText = String.format("%dx%d → %dx%d (2× büyütme)", w, h, targetW, targetH);
                binding.txtDimensions.setText(dimText);
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
        binding.btnProcess.setVisibility(View.VISIBLE);
        binding.btnSave.setVisibility(View.GONE);
    }

    private void showResultState() {
        binding.layoutSelectImage.setVisibility(View.GONE);
        binding.layoutPreview.setVisibility(View.GONE);
        binding.layoutResult.setVisibility(View.VISIBLE);
        binding.btnProcess.setVisibility(View.GONE);
        binding.btnSave.setVisibility(View.VISIBLE);

        // Before/After slider
        binding.beforeAfterSlider.setImages(originalBitmap, resultBitmap);

        // Sonuç boyut bilgisi
        if (resultBitmap != null) {
            String dimText = String.format("Sonuç: %dx%d piksel", resultBitmap.getWidth(), resultBitmap.getHeight());
            binding.txtResultDimensions.setText(dimText);
        }
    }

    private void showLoading(boolean show) {
        binding.layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void saveResult() {
        try {
            String fileName = "upscale_" + System.currentTimeMillis() + ".png";
            Uri savedUri = BitmapUtils.saveBitmapToGallery(this, resultBitmap, fileName, true);
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
            String origPath = BitmapUtils.saveToInternalStorage(this, originalBitmap, "upscale_orig_" + ts + ".jpg");
            String resultPath = BitmapUtils.saveToInternalStorage(this, resultBitmap, "upscale_result_" + ts + ".jpg");
            HistoryViewModel.saveToHistory(getApplication(), getString(R.string.module_upscale), origPath, resultPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
