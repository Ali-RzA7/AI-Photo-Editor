package com.example.myapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Fotoğraf üzerine parmakla maske çizimi yapan Custom View.
 *
 * İki mod:
 * - DRAW (Çizim Modu): Parmakla maske çizimi
 * - PAN_ZOOM (Pan/Zoom Modu): İki parmakla zoom, bir parmakla pan
 *
 * ScaleGestureDetector ile pinch-to-zoom, çizim ile çakışmaz.
 * Fırça boyutu ayarlanabilir, undo ve clear desteği var.
 * Çizilen maskeyi orijinal boyutta Siyah-Beyaz PNG olarak export eder.
 */
public class MaskDrawingView extends View {

    /**
     * Etkileşim modları.
     */
    public enum Mode {
        DRAW,       // Çizim modu
        PAN_ZOOM    // Pan/Zoom modu
    }

    private Mode currentMode = Mode.DRAW;

    // Bitmap'ler
    private Bitmap backgroundBitmap;
    private Bitmap drawingBitmap;
    private Canvas drawingCanvas;

    // Paint'ler
    private Paint drawPaint;
    private Paint bgPaint;

    // Çizim state
    private Path currentPath;
    private float brushSize = 40f;
    private final List<DrawAction> actions = new ArrayList<>();
    private float lastTouchX, lastTouchY;
    private boolean isDrawing = false;

    // Pan/Zoom state (view transform)
    private float viewScale = 1.0f;
    private float viewTranslateX = 0f;
    private float viewTranslateY = 0f;
    private float minScale = 1.0f;
    private float maxScale = 5.0f;

    // Pan state
    private float panStartX, panStartY;
    private float panLastTranslateX, panLastTranslateY;
    private boolean isPanning = false;

    // Fit-to-view hesaplamaları
    private float fitScale;   // Bitmap → View fit scale
    private float fitOffsetX; // Bitmap → View fit offset X
    private float fitOffsetY; // Bitmap → View fit offset Y

    // Gesture Detectors
    private ScaleGestureDetector scaleGestureDetector;
    private boolean isScaling = false;

    // Mod değişikliği listener
    private OnModeChangeListener modeChangeListener;

    public interface OnModeChangeListener {
        void onModeChanged(Mode newMode);
    }

    public MaskDrawingView(Context context) {
        super(context);
        init(context);
    }

    public MaskDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MaskDrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        // Çizim fırçası - yarı saydam kırmızı
        drawPaint = new Paint();
        drawPaint.setColor(Color.argb(150, 255, 80, 80));
        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setStrokeWidth(brushSize);

        bgPaint = new Paint();
        bgPaint.setFilterBitmap(true);
        bgPaint.setAntiAlias(true);

        currentPath = new Path();

        // Scale Gesture Detector (Pinch-to-zoom)
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                isScaling = true;
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
                float newScale = viewScale * scaleFactor;

                // Sınırla
                newScale = Math.max(minScale, Math.min(maxScale, newScale));

                // Zoom merkezi etrafında ölçekle
                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();

                viewTranslateX = focusX - (focusX - viewTranslateX) * (newScale / viewScale);
                viewTranslateY = focusY - (focusY - viewTranslateY) * (newScale / viewScale);

                viewScale = newScale;
                constrainTranslation();
                invalidate();
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                isScaling = false;
            }
        });
    }

    // ========== Public API ==========

    /**
     * Arka plan fotoğrafını ayarlar.
     */
    public void setBackgroundImage(Bitmap bitmap) {
        this.backgroundBitmap = bitmap;
        drawingBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        drawingCanvas = new Canvas(drawingBitmap);
        actions.clear();
        currentPath = new Path();
        viewScale = 1.0f;
        viewTranslateX = 0f;
        viewTranslateY = 0f;
        invalidate();
    }

    /**
     * Modu ayarlar (DRAW veya PAN_ZOOM).
     */
    public void setMode(Mode mode) {
        this.currentMode = mode;
        if (modeChangeListener != null) {
            modeChangeListener.onModeChanged(mode);
        }
    }

    public Mode getMode() {
        return currentMode;
    }

    /**
     * Mod değişikliği toggle eder.
     */
    public void toggleMode() {
        setMode(currentMode == Mode.DRAW ? Mode.PAN_ZOOM : Mode.DRAW);
    }

    public void setOnModeChangeListener(OnModeChangeListener listener) {
        this.modeChangeListener = listener;
    }

    /**
     * Fırça boyutunu ayarlar (piksel cinsinden).
     */
    public void setBrushSize(float size) {
        this.brushSize = size;
        drawPaint.setStrokeWidth(size);
    }

    public float getBrushSize() {
        return brushSize;
    }

    /**
     * Son çizimi geri alır.
     */
    public void undo() {
        if (!actions.isEmpty()) {
            actions.remove(actions.size() - 1);
            redrawAll();
            invalidate();
        }
    }

    /**
     * Tüm çizimleri temizler.
     */
    public void clearAll() {
        actions.clear();
        if (drawingBitmap != null) {
            drawingBitmap.eraseColor(Color.TRANSPARENT);
        }
        currentPath = new Path();
        invalidate();
    }

    /**
     * Zoom'u sıfırlar (fit-to-view).
     */
    public void resetZoom() {
        viewScale = 1.0f;
        viewTranslateX = 0f;
        viewTranslateY = 0f;
        invalidate();
    }

    /**
     * Maskeyi Siyah-Beyaz Bitmap olarak döndürür.
     * Çizilen alanlar beyaz (255), geri kalan siyah (0).
     */
    public Bitmap getMaskBitmap() {
        if (backgroundBitmap == null) return null;

        int w = backgroundBitmap.getWidth();
        int h = backgroundBitmap.getHeight();
        Bitmap maskBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas maskCanvas = new Canvas(maskBitmap);

        maskCanvas.drawColor(Color.BLACK);

        Paint maskPaint = new Paint();
        maskPaint.setColor(Color.WHITE);
        maskPaint.setAntiAlias(true);
        maskPaint.setStyle(Paint.Style.STROKE);
        maskPaint.setStrokeJoin(Paint.Join.ROUND);
        maskPaint.setStrokeCap(Paint.Cap.ROUND);

        for (DrawAction action : actions) {
            maskPaint.setStrokeWidth(action.brushSize);
            maskCanvas.drawPath(action.path, maskPaint);
        }

        return maskBitmap;
    }

    /**
     * Çizim var mı kontrol eder.
     */
    public boolean hasDrawing() {
        return !actions.isEmpty();
    }

    // ========== Drawing ==========

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (backgroundBitmap == null) return;

        calculateFitDimensions();

        canvas.save();

        // View transform uygula (pan/zoom)
        canvas.translate(viewTranslateX, viewTranslateY);
        canvas.scale(viewScale, viewScale);

        // Arka plan görselini fit olarak çiz
        RectF destRect = new RectF(fitOffsetX, fitOffsetY,
                fitOffsetX + backgroundBitmap.getWidth() * fitScale,
                fitOffsetY + backgroundBitmap.getHeight() * fitScale);

        canvas.drawBitmap(backgroundBitmap, null, destRect, bgPaint);

        // Çizim katmanını göster
        if (drawingBitmap != null) {
            canvas.drawBitmap(drawingBitmap, null, destRect, bgPaint);
        }

        // Mod göstergesi: Çizim modunda kenarlık
        if (currentMode == Mode.DRAW) {
            Paint borderPaint = new Paint();
            borderPaint.setColor(Color.argb(80, 255, 80, 80));
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(3f / viewScale); // Zoom'a uyumlu
            canvas.drawRect(destRect, borderPaint);
        }

        canvas.restore();
    }

    // ========== Touch Handling ==========

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (backgroundBitmap == null) return false;

        if (currentMode == Mode.PAN_ZOOM) {
            return handlePanZoomTouch(event);
        } else {
            return handleDrawTouch(event);
        }
    }

    /**
     * PAN/ZOOM modunda touch işleme.
     * Tek parmak: pan, İki parmak: pinch-to-zoom.
     */
    private boolean handlePanZoomTouch(MotionEvent event) {
        // ScaleGestureDetector her zaman event'i alsın
        scaleGestureDetector.onTouchEvent(event);

        // Scaling sırasında pan yapma
        if (isScaling) {
            isPanning = false;
            return true;
        }

        // Tek parmak pan
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                panStartX = event.getX();
                panStartY = event.getY();
                panLastTranslateX = viewTranslateX;
                panLastTranslateY = viewTranslateY;
                isPanning = true;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (isPanning && event.getPointerCount() == 1) {
                    viewTranslateX = panLastTranslateX + (event.getX() - panStartX);
                    viewTranslateY = panLastTranslateY + (event.getY() - panStartY);
                    constrainTranslation();
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isPanning = false;
                return true;
        }

        return true;
    }

    /**
     * DRAW modunda touch işleme.
     * Sadece tek parmak çizim, iki parmak algılanırsa çizim iptal edilir.
     */
    private boolean handleDrawTouch(MotionEvent event) {
        // İki parmak algılandığında çizimi iptal et
        if (event.getPointerCount() > 1) {
            if (isDrawing) {
                // Mevcut çizimi iptal et (save etme)
                isDrawing = false;
                currentPath = new Path();
                redrawAll();
                invalidate();
            }
            return true;
        }

        // Dokunma koordinatını bitmap koordinatına çevir (view transform dahil)
        float viewX = event.getX();
        float viewY = event.getY();
        float bitmapX = viewToBitmapX(viewX);
        float bitmapY = viewToBitmapY(viewY);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                currentPath = new Path();
                currentPath.moveTo(bitmapX, bitmapY);
                lastTouchX = bitmapX;
                lastTouchY = bitmapY;
                isDrawing = true;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (!isDrawing) return true;

                float dx = Math.abs(bitmapX - lastTouchX);
                float dy = Math.abs(bitmapY - lastTouchY);
                if (dx >= 2 || dy >= 2) {
                    currentPath.quadTo(lastTouchX, lastTouchY,
                            (bitmapX + lastTouchX) / 2, (bitmapY + lastTouchY) / 2);
                    lastTouchX = bitmapX;
                    lastTouchY = bitmapY;
                }

                // Geçici çizimi göster
                drawingBitmap.eraseColor(Color.TRANSPARENT);
                redrawAllToCanvas();
                drawingCanvas.drawPath(currentPath, drawPaint);
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                if (!isDrawing) return true;

                currentPath.lineTo(bitmapX, bitmapY);
                actions.add(new DrawAction(new Path(currentPath), brushSize));
                isDrawing = false;
                redrawAll();
                invalidate();
                return true;

            case MotionEvent.ACTION_CANCEL:
                isDrawing = false;
                currentPath = new Path();
                redrawAll();
                invalidate();
                return true;
        }

        return true;
    }

    // ========== Coordinate Transforms ==========

    /**
     * View koordinatını bitmap koordinatına çevirir.
     * view transform (pan/zoom) + fit transform uygulanır.
     */
    private float viewToBitmapX(float viewX) {
        // Ters transform: view → canvas → bitmap
        float canvasX = (viewX - viewTranslateX) / viewScale;
        return (canvasX - fitOffsetX) / fitScale;
    }

    private float viewToBitmapY(float viewY) {
        float canvasY = (viewY - viewTranslateY) / viewScale;
        return (canvasY - fitOffsetY) / fitScale;
    }

    // ========== Internal Helpers ==========

    private void calculateFitDimensions() {
        if (backgroundBitmap == null) return;

        float viewW = getWidth();
        float viewH = getHeight();
        float bmpW = backgroundBitmap.getWidth();
        float bmpH = backgroundBitmap.getHeight();

        fitScale = Math.min(viewW / bmpW, viewH / bmpH);
        fitOffsetX = (viewW - bmpW * fitScale) / 2f;
        fitOffsetY = (viewH - bmpH * fitScale) / 2f;
    }

    /**
     * Pan sınırlarını kontrol eder (görsel ekran dışına çıkmasın).
     */
    private void constrainTranslation() {
        if (backgroundBitmap == null) return;
        calculateFitDimensions();

        float contentWidth = getWidth() * viewScale;
        float contentHeight = getHeight() * viewScale;

        float maxTx = 0;
        float minTx = getWidth() - contentWidth;
        float maxTy = 0;
        float minTy = getHeight() - contentHeight;

        if (contentWidth <= getWidth()) {
            viewTranslateX = 0;
        } else {
            viewTranslateX = Math.max(minTx, Math.min(maxTx, viewTranslateX));
        }

        if (contentHeight <= getHeight()) {
            viewTranslateY = 0;
        } else {
            viewTranslateY = Math.max(minTy, Math.min(maxTy, viewTranslateY));
        }
    }

    private void redrawAll() {
        if (drawingBitmap == null) return;
        drawingBitmap.eraseColor(Color.TRANSPARENT);
        redrawAllToCanvas();
    }

    private void redrawAllToCanvas() {
        Paint p = new Paint(drawPaint);
        for (DrawAction action : actions) {
            p.setStrokeWidth(action.brushSize);
            drawingCanvas.drawPath(action.path, p);
        }
    }

    /**
     * Çizim aksiyonunu temsil eden iç sınıf.
     */
    private static class DrawAction {
        final Path path;
        final float brushSize;

        DrawAction(Path path, float brushSize) {
            this.path = path;
            this.brushSize = brushSize;
        }
    }
}
