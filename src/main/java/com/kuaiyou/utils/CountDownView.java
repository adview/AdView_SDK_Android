package com.kuaiyou.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import com.kuaiyou.utils.ConstantValues;

public class CountDownView extends View {

    private String texColor = "";
    private String content = "";
    private Paint paint;
    private Paint backgroundPaint;
    private int progress=0;
    private int strokeWidth=5;
    private Paint circlePaint=new Paint();

    public CountDownView(Context context) {
        super(context);
        paint = new Paint();
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.parseColor(ConstantValues.VIDEO_ICON_BG_COLOR));
    }

    public void setTextSize(int size) {
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(size);
    }

    public void updateProgress(int progress){
        this.progress=progress;
        invalidate();
    }

    public void updateContent(String content) {
        this.content = content;
        invalidate();
    }

    public void setPaintColor(int color) {
        if (null != paint)
            paint.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect drawRect = new Rect();
        getDrawingRect(drawRect);
        RectF drawRectF=new RectF(drawRect.left+strokeWidth,drawRect.top+strokeWidth,drawRect.right-strokeWidth,drawRect.bottom-strokeWidth);
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        int baseline = (drawRect.bottom + drawRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
        // 下面这行是实现水平居中，drawText对应改为传入targetRect.centerX()
        paint.setTextAlign(Paint.Align.CENTER);
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.WHITE);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(strokeWidth);
        canvas.drawCircle(drawRect.centerX(), drawRect.centerY(), drawRect.right / 2, backgroundPaint);
        canvas.drawText(content, drawRect.centerX(), baseline, paint);
        canvas.drawArc(drawRectF,-90,progress,false,circlePaint);
    }
}
