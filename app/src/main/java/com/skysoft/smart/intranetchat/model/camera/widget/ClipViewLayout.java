/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/21
 * Description: PG1-Smart Team-CT PT-29 [MM] Viewing Pictures Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.exifinterface.media.ExifInterface;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.model.camera.util.BitmapUtil;
import com.skysoft.smart.intranetchat.model.camera.util.FileUtil;

import java.io.IOException;


public class ClipViewLayout extends RelativeLayout {

    private static final String TAG = "debug " + ClipViewLayout.class.getSimpleName() + " ";

    /**
     * 裁剪原图
     */
    private ImageView mImageView;
    /**
     * 裁剪框
     */
    private ClipView mClipView;
    /**
     * 裁剪框水平方向间距
     */
    private float mHorizontalPadding;
    /**
     * 裁剪框垂直方向间距
     */
    private float mVerticalPadding;
    /**
     * 图片缩放、移动操作矩阵
     */
    private Matrix mMatrix = new Matrix();
    /**
     * 图片原来已经缩放、移动过的操作矩阵
     */
    private Matrix mSavedMatrix = new Matrix();
    /**
     * 动作标志：无
     */
    private static final int NONE = 0;
    /**
     * 动作标志：拖动
     */
    private static final int DRAG = 1;
    /**
     * 动作标志：缩放
     */
    private static final int ZOOM = 2;
    /**
     * 初始化动作标志
     */
    private int mode = NONE;
    /**
     * 记录起始坐标
     */
    private PointF mStart = new PointF();
    /**
     * 记录缩放时两指中间点坐标
     */
    private PointF mMid = new PointF();
    private float mOldDist = 1f;
    /**
     * 用于存放矩阵的9个值
     */
    private final float[] mMatrixValues = new float[9];
    /**
     * 最小缩放比例
     */
    private float mMinScale;
    /**
     * 最大缩放比例
     */
    private float mMaxScale = 4;

    private int mWidthPixels;
    private int mHeightPixels;
    private String mPath;
    private boolean mFlag = false;
    private long mDownTime;
    private long mUpTime;

    public ClipViewLayout(Context context) {
        this(context, null);
    }

    public ClipViewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClipViewLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ClipViewLayout);
        mHorizontalPadding = array.getDimensionPixelSize(R.styleable.ClipViewLayout_horizontalPadding, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
        int clipBorderWidth = array.getDimensionPixelSize(R.styleable.ClipViewLayout_clipBorderWidth, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        array.recycle();
        mClipView = new ClipView(context);

        mClipView.setClipBorderWidth(clipBorderWidth);
        mClipView.setmHorizontalPadding(mHorizontalPadding);
        mImageView = new ImageView(context);
        android.view.ViewGroup.LayoutParams lp = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(mImageView, lp);
        this.addView(mClipView, lp);

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mWidthPixels = displayMetrics.widthPixels;
        mHeightPixels = displayMetrics.heightPixels;
    }


    /**
     * 初始化图片
     */
    public void setImageSrc(Uri uri) {
        ViewTreeObserver observer = mImageView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                initSrcPic(uri);
                mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public void initSrcPic(Uri uri) {
        if (uri == null) {
            return;
        }
        mPath = FileUtil.getRealFilePathFromUri(uri, getContext());
        if (TextUtils.isEmpty(mPath)) {
            return;
        }
        int[] imageWidthHeight = BitmapUtil.getImageWidthHeight(mPath);
        int w = imageWidthHeight[0];
        int h = imageWidthHeight[1];
        Bitmap bitmap = BitmapUtil.decodeSampledBitmap(mPath, w > mWidthPixels ? mWidthPixels : w, h > mHeightPixels ? mHeightPixels : h);
        if (bitmap == null) {
            return;
        }

        int rotation = getExifOrientation(mPath);
        Matrix m = new Matrix();
        m.setRotate(rotation);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

        float scaleX;
        float scaleY;
        if (bitmap.getWidth() >= bitmap.getHeight()) {
            scaleX = (float) mImageView.getWidth() / bitmap.getWidth();
            Rect rect = mClipView.getClipRect();
            mMinScale = rect.height() / (float) bitmap.getHeight();
            if (scaleX < mMinScale) {
                scaleX = mMinScale;
            }
        } else {
            scaleX = (float) mImageView.getHeight() / bitmap.getHeight();
            Rect rect = mClipView.getClipRect();
            mMinScale = rect.width() / (float) bitmap.getWidth();
            if (scaleX < mMinScale) {
                scaleX = mMinScale;
            }
        }
        scaleY = scaleX;
        mMatrix.postScale(scaleX, scaleY);

        int midX = mImageView.getWidth() / 2;
        int midY = mImageView.getHeight() / 2;
        int imageMidX = (int) (bitmap.getWidth() * scaleX / 2);
        int imageMidY = (int) (bitmap.getHeight() * scaleY / 2);
        mMatrix.postTranslate(midX - imageMidX, midY - imageMidY);
        mImageView.setScaleType(ImageView.ScaleType.MATRIX);
        mImageView.setImageMatrix(mMatrix);
        mImageView.setImageBitmap(bitmap);
    }

    /**
     * 查询图片旋转角度
     */
    public static int getExifOrientation(String filepath) {
        int degree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        degree = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        degree = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        degree = 270;
                        break;
                }

            }
        }
        return degree;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mSavedMatrix.set(mMatrix);
                mStart.set(event.getX(), event.getY());
                mode = DRAG;
                if (mFlag) {
                    mDownTime = System.currentTimeMillis();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mOldDist = spacing(event);
                if (mOldDist > 10f) {
                    mSavedMatrix.set(mMatrix);
                    midPoint(mMid, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
                mode = NONE;
                if (mFlag) {
                    if (System.currentTimeMillis() - mDownTime < 100) {
                        activity.finish();
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    mMatrix.set(mSavedMatrix);
                    float dx = event.getX() - mStart.x;
                    float dy = event.getY() - mStart.y;
                    mVerticalPadding = mClipView.getClipRect().top;
                    mMatrix.postTranslate(dx, dy);
                    checkBorder();
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        float scale = newDist / mOldDist;
                        if (scale < 1) {
                            if (getScale() > mMinScale) {
                                mMatrix.set(mSavedMatrix);
                                mVerticalPadding = mClipView.getClipRect().top;
                                mMatrix.postScale(scale, scale, mMid.x, mMid.y);
                                while (getScale() < mMinScale) {
                                    scale = 1 + 0.01F;
                                    mMatrix.postScale(scale, scale, mMid.x, mMid.y);
                                }
                            }
                            checkBorder();
                        } else {
                            if (getScale() <= mMaxScale) {
                                mMatrix.set(mSavedMatrix);
                                mVerticalPadding = mClipView.getClipRect().top;
                                mMatrix.postScale(scale, scale, mMid.x, mMid.y);
                            }
                        }
                    }
                }
                mImageView.setImageMatrix(mMatrix);
                break;
        }
        return true;
    }

    /**
     * 根据当前图片的Matrix获得图片的范围
     */
    private RectF getMatrixRectF(Matrix matrix) {
        RectF rect = new RectF();
        Drawable d = mImageView.getDrawable();
        if (null != d) {
            rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }

    /**
     * 边界检测
     */
    private void checkBorder() {
        RectF rect = getMatrixRectF(mMatrix);
        float deltaX = 0;
        float deltaY = 0;
        int width = mImageView.getWidth();
        int height = mImageView.getHeight();
        if (rect.width() + 0.01 >= width - 2 * mHorizontalPadding) {
            if (rect.left > mHorizontalPadding) {
                deltaX = -rect.left + mHorizontalPadding;
            }
            if (rect.right < width - mHorizontalPadding) {
                deltaX = width - mHorizontalPadding - rect.right;
            }
        }
        if (rect.height() + 0.01 >= height - 2 * mVerticalPadding) {
            if (rect.top > mVerticalPadding) {
                deltaY = -rect.top + mVerticalPadding;
            }
            if (rect.bottom < height - mVerticalPadding) {
                deltaY = height - mVerticalPadding - rect.bottom;
            }
        }

        mMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 获得当前的缩放比例
     */
    private float getScale() {
        mMatrix.getValues(mMatrixValues);
        return mMatrixValues[Matrix.MSCALE_X];
    }

    /**
     * 多点触控时，计算最先放下的两指距离
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 多点触控时，计算最先放下的两指中心坐标
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * 获取修改后的图
     */
    public Bitmap clip() {
        mImageView.setDrawingCacheEnabled(true);
        mImageView.buildDrawingCache();
        Rect rect = mClipView.getClipRect();
        Bitmap cropBitmap = null;
        Bitmap zoomedCropBitmap = null;
        try {
            cropBitmap = Bitmap.createBitmap(mImageView.getDrawingCache(), rect.left, rect.top, rect.width(), rect.height());
            zoomedCropBitmap = BitmapUtil.zoomBitmap(cropBitmap, rect.width(), rect.height());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cropBitmap != null) {
            cropBitmap.recycle();
        }
        mImageView.destroyDrawingCache();
        return zoomedCropBitmap;
    }

    public String getPath() {
        return mPath;
    }

    public void setIsCircle(boolean isCircle) {
        if (mClipView != null) {
            mClipView.setIsCircle(isCircle);
        }
    }

    public void setShow(boolean isShow) {
        if (mClipView != null) {
            mClipView.setShwo(isShow);
            mFlag = isShow;
        }
    }

    private Activity activity;

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
}
