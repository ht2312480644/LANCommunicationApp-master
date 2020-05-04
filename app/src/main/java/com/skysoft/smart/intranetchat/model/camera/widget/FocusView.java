/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/15
 * Description: PG1-Smart Team-CTPT-21 [MM] Taking Picture Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import com.skysoft.smart.intranetchat.R;

public class FocusView extends View {

    private final String TAG = this.getClass().getSimpleName();
    private int radiusOuter, radiusInner, strokeWidth;
    private int colorNormal, colorCurrent;
    private int previewWidth;
    private int previewHeight;
    private RectF innerRectF;
    private Paint paint;

    public FocusView(Context context) {
        super(context);
        Resources resources = context.getResources();
        radiusOuter = resources.getDimensionPixelSize(R.dimen.dp_50);
        radiusInner = resources.getDimensionPixelSize(R.dimen.dp_50);
        strokeWidth = resources.getDimensionPixelSize(R.dimen.dp_2);

        colorNormal = resources.getColor(R.color.color_focus);
        colorCurrent = colorNormal;

        innerRectF = new RectF(radiusOuter - radiusInner, radiusOuter - radiusInner,
                radiusOuter + radiusInner, radiusOuter + radiusInner);

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(strokeWidth);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(radiusOuter * 2, radiusOuter * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircle(canvas, colorCurrent);
    }

    private void drawCircle(Canvas canvas, int color) {
        paint.setColor(color);
        for (int i = 0; i < 4; i++) {
            canvas.drawArc(innerRectF, 90 * i + 50, 80, false, paint);
        }

    }

    public void startFocus() {
        this.setVisibility(VISIBLE);
        colorCurrent = colorNormal;
        invalidate();

    }

    public void hideFocusView() {
            this.setVisibility(GONE);
    }


    public void moveToPosition(float x, float y) {
        x -= radiusOuter;
        y -= radiusOuter;
        this.setTranslationX(x);
        this.setTranslationY(y);
        this.setVisibility(VISIBLE);
        colorCurrent = colorNormal;
        invalidate();
    }

    public void resetToDefaultPosition() {
        int x = previewWidth / 2 - radiusOuter;
        int y = previewHeight / 2 - radiusOuter;
        this.setTranslationX(x);
        this.setTranslationY(y);
    }

    public void initFocusArea(int width, int height) {
        previewWidth = width;
        previewHeight = height;
        resetToDefaultPosition();
    }

}
