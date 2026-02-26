package com.example.myapplication.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Önce / Sonra (Before / After) Slider Custom View.
 * Parmakla sürüklenebilen bir dikey çizgi ile iki görseli karşılaştırır.
 */
public class BeforeAfterSliderView extends View {

    private Bitmap beforeBitmap;
    private Bitmap afterBitmap;
    private float sliderPosition = 0.5f; // 0.0 - 1.0

    private Paint imagePaint;
    private Paint sliderPaint;
    private Paint handlePaint;
    private Paint labelPaint;
    private Paint labelBgPaint;

    // Bitmap görselin View üzerindeki pozisyonu
    private float offsetX, offsetY, scaleFactor;
    private float fitWidth, fitHeight;

    public BeforeAfterSliderView(Context context) {
        super(context);
        init();
    }

    public BeforeAfterSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BeforeAfterSliderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        imagePaint = new Paint();
        imagePaint.setFilterBitmap(true);
        imagePaint.setAntiAlias(true);

        sliderPaint = new Paint();
        sliderPaint.setColor(Color.WHITE);
        sliderPaint.setStrokeWidth(4f);
        sliderPaint.setAntiAlias(true);
        sliderPaint.setShadowLayer(6f, 0, 0, Color.argb(100, 0, 0, 0));
        setLayerType(LAYER_TYPE_SOFTWARE, sliderPaint);

        handlePaint = new Paint();
        handlePaint.setColor(Color.WHITE);
        handlePaint.setAntiAlias(true);
        handlePaint.setShadowLayer(8f, 0, 2, Color.argb(80, 0, 0, 0));

        labelPaint = new Paint();
        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextSize(28f);
        labelPaint.setAntiAlias(true);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setFakeBoldText(true);

        labelBgPaint = new Paint();
        labelBgPaint.setColor(Color.argb(150, 0, 0, 0));
        labelBgPaint.setAntiAlias(true);
    }

    /**
     * Önceki ve sonraki görselleri ayarlar.
     */
    public void setImages(Bitmap before, Bitmap after) {
        this.beforeBitmap = before;
        this.afterBitmap = after;
        sliderPosition = 0.5f;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (beforeBitmap == null || afterBitmap == null) return;

        calculateFitDimensions();

        float drawLeft = offsetX;
        float drawTop = offsetY;
        float drawRight = offsetX + fitWidth;
        float drawBottom = offsetY + fitHeight;

        float sliderX = drawLeft + fitWidth * sliderPosition;

        // "Sonra" (After) görselini sağ tarafa tam çiz
        canvas.save();
        canvas.clipRect(drawLeft, drawTop, drawRight, drawBottom);
        canvas.drawBitmap(afterBitmap, null, new RectF(drawLeft, drawTop, drawRight, drawBottom), imagePaint);
        canvas.restore();

        // "Önce" (Before) görselini slider'ın soluna kırp
        canvas.save();
        canvas.clipRect(drawLeft, drawTop, sliderX, drawBottom);
        canvas.drawBitmap(beforeBitmap, null, new RectF(drawLeft, drawTop, drawRight, drawBottom), imagePaint);
        canvas.restore();

        // Slider çizgisi
        canvas.drawLine(sliderX, drawTop, sliderX, drawBottom, sliderPaint);

        // Slider handle (daire)
        float handleRadius = 20f;
        float handleY = drawTop + fitHeight / 2f;
        canvas.drawCircle(sliderX, handleY, handleRadius, handlePaint);

        // Ok işaretleri
        Paint arrowPaint = new Paint();
        arrowPaint.setColor(Color.DKGRAY);
        arrowPaint.setStrokeWidth(3f);
        arrowPaint.setAntiAlias(true);
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeCap(Paint.Cap.ROUND);

        // Sol ok
        Path leftArrow = new Path();
        leftArrow.moveTo(sliderX - 5, handleY);
        leftArrow.lineTo(sliderX - 10, handleY - 6);
        leftArrow.moveTo(sliderX - 5, handleY);
        leftArrow.lineTo(sliderX - 10, handleY + 6);
        canvas.drawPath(leftArrow, arrowPaint);

        // Sağ ok
        Path rightArrow = new Path();
        rightArrow.moveTo(sliderX + 5, handleY);
        rightArrow.lineTo(sliderX + 10, handleY - 6);
        rightArrow.moveTo(sliderX + 5, handleY);
        rightArrow.lineTo(sliderX + 10, handleY + 6);
        canvas.drawPath(rightArrow, arrowPaint);

        // Etiketler: Önce / Sonra
        float labelY = drawTop + 40;
        float labelPadding = 16f;

        // "Önce" etiketi (sol)
        if (sliderX - drawLeft > 80) {
            float beforeLabelX = drawLeft + (sliderX - drawLeft) / 2;
            drawLabel(canvas, "ÖNCE", beforeLabelX, labelY, labelPadding);
        }

        // "Sonra" etiketi (sağ)
        if (drawRight - sliderX > 80) {
            float afterLabelX = sliderX + (drawRight - sliderX) / 2;
            drawLabel(canvas, "SONRA", afterLabelX, labelY, labelPadding);
        }
    }

    private void drawLabel(Canvas canvas, String text, float x, float y, float padding) {
        float textWidth = labelPaint.measureText(text);
        RectF bg = new RectF(x - textWidth / 2 - padding, y - 22, x + textWidth / 2 + padding, y + 10);
        canvas.drawRoundRect(bg, 12, 12, labelBgPaint);
        canvas.drawText(text, x, y, labelPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (beforeBitmap == null || afterBitmap == null) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                sliderPosition = Math.max(0f, Math.min(1f, (x - offsetX) / fitWidth));
                invalidate();
                return true;
        }

        return super.onTouchEvent(event);
    }

    private void calculateFitDimensions() {
        if (beforeBitmap == null) return;

        float viewW = getWidth();
        float viewH = getHeight();
        float bmpW = beforeBitmap.getWidth();
        float bmpH = beforeBitmap.getHeight();

        scaleFactor = Math.min(viewW / bmpW, viewH / bmpH);
        fitWidth = bmpW * scaleFactor;
        fitHeight = bmpH * scaleFactor;
        offsetX = (viewW - fitWidth) / 2f;
        offsetY = (viewH - fitHeight) / 2f;
    }
}
