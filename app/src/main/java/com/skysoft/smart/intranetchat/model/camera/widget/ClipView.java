/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/21
 * Description: PG1-Smart Team-CT PT-29 [MM] Viewing Pictures Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;


public class ClipView extends View {

    private static final String TAG = "debug " + ClipView.class.getSimpleName() + " ";

    private Paint mPaint = new Paint();
    private Paint mBorderPaint = new Paint();

    /**
     * 水平方向间距
     */
    private float mHorizontalPadding;
    /**
     * 边框宽度
     */
    private int mClipBorderWidth;
    /**
     * 矩形宽度
     */
    private int mClipWidth;
    /**
     * 圆半径
     */
    private int clipRadiusWidth;
    private boolean isCircle = false;
    private boolean isShwo = false;

    private Xfermode mXfermode;

    public ClipView(Context context) {
        this(context, null);
    }

    public ClipView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Style.FILL);
        mBorderPaint.setStyle(Style.STROKE);
        mBorderPaint.setColor(Color.WHITE);
        mBorderPaint.setStrokeWidth(mClipBorderWidth);
        mBorderPaint.setAntiAlias(true);
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_OUT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.saveLayer(0, 0, this.getWidth(), this.getHeight(), null, Canvas.ALL_SAVE_FLAG);


        if (isShwo) {
            @SuppressLint("DrawAllocation")
            Paint p = new Paint();
            p.setColor(Color.TRANSPARENT);
            canvas.drawCircle(this.getWidth() / 2, this.getHeight() / 2, clipRadiusWidth, p);
        } else {
            canvas.drawColor(Color.parseColor("#a8000000"));
            mPaint.setXfermode(mXfermode);
            if (!isCircle) {
                canvas.drawRect(mHorizontalPadding, this.getHeight() / 2 - mClipWidth / 2, this.getWidth() - mHorizontalPadding, this.getHeight() - mClipWidth / 8, mPaint);
                canvas.drawRect(mHorizontalPadding, this.getHeight() / 2 - mClipWidth / 2, this.getWidth() - mHorizontalPadding, this.getHeight() - mClipWidth / 8, mBorderPaint);
            } else {
                canvas.drawCircle(this.getWidth() / 2, this.getHeight() / 2, clipRadiusWidth, mPaint);
                canvas.drawCircle(this.getWidth() / 2, this.getHeight() / 2, clipRadiusWidth, mBorderPaint);
            }
        }
        canvas.restore();
    }

    public void setIsCircle(boolean isCircle) {
        this.isCircle = isCircle;
    }

    public void setShwo(boolean isShwo) {
        this.isShwo = isShwo;
    }

    /**
     * 获取裁剪区域的Rect
     */
    public Rect getClipRect() {
        Rect rect = new Rect();
        if (!isCircle) {
            rect.left = (this.getWidth() / 2 - mClipWidth / 2);
            rect.right = (this.getWidth() / 2 + mClipWidth / 2);
            rect.top = (this.getHeight() / 2 - mClipWidth / 2);
            rect.bottom = (this.getHeight() - mClipWidth / 8);
        } else {
            rect.left = (this.getWidth() / 2 - clipRadiusWidth);
            rect.right = (this.getWidth() / 2 + clipRadiusWidth);
            rect.top = (this.getHeight() / 2 - clipRadiusWidth);
            rect.bottom = (this.getHeight() / 2 + clipRadiusWidth);
        }
        return rect;
    }


    /**
     * 设置裁剪框边框宽度
     *
     * @param clipBorderWidth
     */
    public void setClipBorderWidth(int clipBorderWidth) {
        this.mClipBorderWidth = clipBorderWidth;
        mBorderPaint.setStrokeWidth(clipBorderWidth);
        invalidate();
    }

    /**
     * 设置裁剪框水平间距
     */
    public void setmHorizontalPadding(float mHorizontalPadding) {
        this.mHorizontalPadding = mHorizontalPadding;
        this.mClipWidth = (int) (getScreenWidth(getContext()) - 2 * mHorizontalPadding);
        this.clipRadiusWidth = (int) (getScreenWidth(getContext()) - 2 * mHorizontalPadding) / 3;
    }

    /**
     * 获得屏幕高度
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }
}
