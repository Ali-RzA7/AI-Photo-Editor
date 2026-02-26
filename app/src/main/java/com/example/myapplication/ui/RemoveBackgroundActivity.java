package com.example.myapplication.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityRemoveBgBinding;
import com.example.myapplication.util.BitmapUtils;
import com.example.myapplication.util.PermissionHelper;
import com.example.myapplication.viewmodel.HistoryViewModel;
import com.example.myapplication.viewmodel.RemoveBgViewModel;

/**
 * Arka Plan Silme Activity.
 * Akış: Görsel Seç → API ile Arka Plan Kaldır → Şeffaf PNG Göster → Kaydet / Arka Plan Değiştir
 */
public class RemoveBackgroundActivity extends AppCompatActivity {

    private ActivityRemoveBgBinding binding;
    private RemoveBgViewModel viewModel;
    private Bitmap originalBitmap;
    private Bitmap resultBitmap;
    private Bitmap compositeBitmap;

    // Görsel seçici
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    viewModel.loadImage(uri);
                }
            });

    // Arka plan seçici
    private final ActivityResultLauncher<String> bgPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    applyNewBackground(uri);
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

    // Arka plan seçme izni sonucu
    private final ActivityResultLauncher<String> bgPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    bgPickerLauncher.launch("image/*");
                } else {
                    Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRemoveBgBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RemoveBgViewModel.class);

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
                viewModel.processRemoveBackground(originalBitmap);
            }
        });

        binding.btnChangeBg.setOnClickListener(v -> {
            if (PermissionHelper.hasGalleryReadPermission(this)) {
                bgPickerLauncher.launch("image/*");
            } else {
                PermissionHelper.requestGalleryReadPermission(bgPermissionLauncher);
            }
        });

        binding.btnSave.setOnClickListener(v -> saveResult());
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
                    compositeBitmap = null;
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
        binding.btnChangeBg.setVisibility(View.GONE);
        binding.btnSave.setVisibility(View.GONE);
    }

    private void showResultState() {
        binding.layoutSelectImage.setVisibility(View.GONE);
        binding.layoutPreview.setVisibility(View.GONE);
        binding.layoutResult.setVisibility(View.VISIBLE);
        binding.btnProcess.setVisibility(View.GONE);
        binding.btnChangeBg.setVisibility(View.VISIBLE);
        binding.btnSave.setVisibility(View.VISIBLE);

        drawCheckerboardBackground();

        Bitmap displayBitmap = compositeBitmap != null ? compositeBitmap : resultBitmap;
        binding.imgResult.setImageBitmap(displayBitmap);
    }

    private void drawCheckerboardBackground() {
        binding.checkerboardBg.post(() -> {
            int w = binding.checkerboardBg.getWidth();
            int h = binding.checkerboardBg.getHeight();
            if (w <= 0 || h <= 0) return;

            Bitmap checkerboard = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(checkerboard);
            Paint lightPaint = new Paint();
            lightPaint.setColor(Color.rgb(240, 240, 240));
            Paint darkPaint = new Paint();
            darkPaint.setColor(Color.rgb(200, 200, 200));

            int squareSize = 20;
            for (int x = 0; x < w; x += squareSize) {
                for (int y = 0; y < h; y += squareSize) {
                    Paint paint = ((x / squareSize + y / squareSize) % 2 == 0) ? lightPaint : darkPaint;
                    canvas.drawRect(x, y, x + squareSize, y + squareSize, paint);
                }
            }

            binding.checkerboardBg.setBackground(
                    new android.graphics.drawable.BitmapDrawable(getResources(), checkerboard));
        });
    }

    private void applyNewBackground(Uri bgUri) {
        try {
            Bitmap bgBitmap = BitmapUtils.loadBitmapForApi(this, bgUri);
            if (resultBitmap != null && bgBitmap != null) {
                Bitmap scaledBg = Bitmap.createScaledBitmap(
                        bgBitmap, resultBitmap.getWidth(), resultBitmap.getHeight(), true);

                compositeBitmap = Bitmap.createBitmap(
                        resultBitmap.getWidth(), resultBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(compositeBitmap);
                canvas.drawBitmap(scaledBg, 0, 0, null);
                canvas.drawBitmap(resultBitmap, 0, 0, null);

                binding.imgResult.setImageBitmap(compositeBitmap);
                binding.checkerboardBg.setBackground(null);
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_occurred), Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        binding.layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void saveResult() {
        Bitmap bitmapToSave = compositeBitmap != null ? compositeBitmap : resultBitmap;
        if (bitmapToSave == null) return;

        try {
            String fileName = "removebg_" + System.currentTimeMillis() + ".png";
            Uri savedUri = BitmapUtils.saveBitmapToGallery(this, bitmapToSave, fileName, true);
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
            String origPath = BitmapUtils.saveToInternalStorage(this, originalBitmap, "removebg_orig_" + ts + ".jpg");
            String resultPath = BitmapUtils.saveToInternalStorage(this, resultBitmap, "removebg_result_" + ts + ".png");
            HistoryViewModel.saveToHistory(getApplication(), getString(R.string.module_removebg), origPath, resultPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
