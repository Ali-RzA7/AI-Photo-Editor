package com.example.myapplication.ui;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityCleanupBinding;
import com.example.myapplication.model.ApiResult;
import com.example.myapplication.util.BitmapUtils;
import com.example.myapplication.util.PermissionHelper;
import com.example.myapplication.view.MaskDrawingView;
import com.example.myapplication.viewmodel.CleanupViewModel;
import com.example.myapplication.viewmodel.HistoryViewModel;

/**
 * Nesne/Kişi Silme Activity.
 * Akış: Görsel Seç → Maske Çiz (Draw/Pan-Zoom toggle) → API'ye Gönder → Önce/Sonra Slider ile Sonuç
 */
public class CleanupActivity extends AppCompatActivity {

    private ActivityCleanupBinding binding;
    private CleanupViewModel viewModel;
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
        binding = ActivityCleanupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CleanupViewModel.class);

        setupToolbar();
        setupModeToggle();
        setupBrushControls();
        setupClickListeners();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.btnBack.setOnClickListener(v -> onBackPressed());
    }

    /**
     * Çizim / Pan-Zoom mod toggle ayarları.
     */
    private void setupModeToggle() {
        // Başlangıç: Çizim modu
        updateModeUI(MaskDrawingView.Mode.DRAW);

        binding.btnModeToggle.setOnClickListener(v -> {
            binding.maskDrawingView.toggleMode();
        });

        binding.maskDrawingView.setOnModeChangeListener(newMode -> {
            updateModeUI(newMode);
        });
    }

    /**
     * Mod değişikliğinde UI'ı günceller.
     */
    private void updateModeUI(MaskDrawingView.Mode mode) {
        if (mode == MaskDrawingView.Mode.DRAW) {
            binding.btnModeToggle.setText(R.string.cleanup_mode_draw);
            binding.layoutBrushControls.setVisibility(View.VISIBLE);
        } else {
            binding.btnModeToggle.setText(R.string.cleanup_mode_pan_zoom);
            binding.layoutBrushControls.setVisibility(View.GONE);
        }
    }

    private void setupBrushControls() {
        binding.seekbarBrushSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float brushSize = 10 + progress; // 10-100 arası
                binding.maskDrawingView.setBrushSize(brushSize);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        binding.btnUndo.setOnClickListener(v -> binding.maskDrawingView.undo());
        binding.btnClear.setOnClickListener(v -> binding.maskDrawingView.clearAll());
    }

    private void setupClickListeners() {
        // Dropzone'a tıklama
        binding.btnSelectImage.setOnClickListener(v -> {
            openGalleryWithPermission();
        });

        // Alt buton ile de galeri aç
        binding.btnSelectImageAlt.setOnClickListener(v -> {
            openGalleryWithPermission();
        });

        binding.btnProcess.setOnClickListener(v -> {
            if (originalBitmap != null && binding.maskDrawingView.hasDrawing()) {
                Bitmap maskBitmap = binding.maskDrawingView.getMaskBitmap();
                viewModel.processCleanup(originalBitmap, maskBitmap);
            } else if (!binding.maskDrawingView.hasDrawing()) {
                Toast.makeText(this, getString(R.string.cleanup_draw_mask), Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSave.setOnClickListener(v -> {
            if (resultBitmap != null) {
                saveResult();
            }
        });
    }

    /**
     * Galeri açmadan önce izin kontrolü yapar.
     */
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
                showDrawMaskState();
                binding.maskDrawingView.setBackgroundImage(bitmap);
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
                    break;
                case ERROR:
                    showLoading(false);
                    Toast.makeText(this, result.getError(), Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void showDrawMaskState() {
        binding.layoutSelectImage.setVisibility(View.GONE);
        binding.layoutDrawMask.setVisibility(View.VISIBLE);
        binding.layoutResult.setVisibility(View.GONE);
        binding.layoutBottomButtons.setVisibility(View.VISIBLE);
        binding.btnProcess.setVisibility(View.VISIBLE);
        binding.btnSave.setVisibility(View.GONE);
    }

    private void showResultState() {
        binding.layoutSelectImage.setVisibility(View.GONE);
        binding.layoutDrawMask.setVisibility(View.GONE);
        binding.layoutResult.setVisibility(View.VISIBLE);
        binding.layoutBottomButtons.setVisibility(View.VISIBLE);
        binding.btnProcess.setVisibility(View.GONE);
        binding.btnSave.setVisibility(View.VISIBLE);

        binding.beforeAfterSlider.setImages(originalBitmap, resultBitmap);
    }

    private void showLoading(boolean show) {
        binding.layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void saveResult() {
        try {
            String fileName = "cleanup_" + System.currentTimeMillis() + ".png";
            Uri savedUri = BitmapUtils.saveBitmapToGallery(this, resultBitmap, fileName, true);
            if (savedUri != null) {
                Toast.makeText(this, getString(R.string.success_saved), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_occurred) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void saveHistory() {
        try {
            long ts = System.currentTimeMillis();
            String origPath = BitmapUtils.saveToInternalStorage(this, originalBitmap, "cleanup_orig_" + ts + ".jpg");
            String resultPath = BitmapUtils.saveToInternalStorage(this, resultBitmap, "cleanup_result_" + ts + ".jpg");
            HistoryViewModel.saveToHistory(getApplication(), getString(R.string.module_cleanup), origPath, resultPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
