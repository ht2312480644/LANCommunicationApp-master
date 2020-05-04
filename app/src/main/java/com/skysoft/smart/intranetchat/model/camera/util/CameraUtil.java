/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/15
 * Description: PG1-Smart Team-CTPT-21 [MM] Taking Picture Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.util;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.net.Uri;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.util.Size;
import android.view.Surface;

import com.skysoft.smart.intranetchat.model.camera.entity.CameraId;
import com.skysoft.smart.intranetchat.model.camera.entity.EventMessage;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.skysoft.smart.intranetchat.ui.fragment.camera.CameraFragment.SAVE_PITCURE_OK;

public class CameraUtil {
    private CameraUtil() {
    }

    private static final String TAG = "debug " + CameraUtil.class.getSimpleName() + " ";
    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;


    /**
     * 返回最合适的预览尺寸
     *
     * @param choices           相机希望输出类支持的尺寸list
     * @param textureViewWidth  texture view 宽度
     * @param textureViewHeight texture view 高度
     * @param maxWidth          能够选择的最大宽度
     * @param maxHeight         能够选择的最大高度
     * @param aspectRatio       图像的比例(pictureSize, 只有当pictureSize和textureSize保持一致, 才不会失真)
     * @return 最合适的预览尺寸
     */
    public static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        /* 存放小于等于限定尺寸, 大于等于texture控件尺寸的Size*/
        List<Size> bigEnough = new ArrayList<>();
        /*存放小于限定尺寸, 小于texture控件尺寸的Size*/
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            TLog.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    /**
     * 比较两个Size的大小基于它们的area
     */
    public static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * 保存jpeg到指定的文件夹下, 开启子线程执行保存操作
     */
    public static class ImageSaver implements Runnable {

        private final Image mImage;
        private final File mFile;

        public ImageSaver(Image image, File file) {
            mImage = image;
            mFile = file;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(mFile);
                output.write(bytes);
                EventBus.getDefault().post(new EventMessage(Uri.fromFile(mFile).toString(),0,SAVE_PITCURE_OK));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 屏幕方向发生改变时调用转换数据方法
     */
    public static Matrix configureTransform(int viewWidth, int viewHeight, Size mPreviewSize, int rotation) {
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        return matrix;
    }

    /**
     * 获取前后置摄像头Id
     */
    public static CameraId getCameraIdList(CameraManager manager) {
        CameraId c = new CameraId();
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    c.setFrontCameraId(cameraId);
                }
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    c.setBackCameraId(cameraId);
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return c;
    }

    /**
     * 是否需要旋转
     */
    public static boolean needRotation(Activity activity, CameraManager manager, String cameraId) {
        boolean swappedDimensions = false;
        int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        CameraCharacteristics characteristics = null;
        try {
            characteristics = manager.getCameraCharacteristics(cameraId);
            int mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            switch (displayRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return swappedDimensions;
    }

    /**
     * 设置合适的尺寸
     */
    public static Size setPreViewSize(Activity activity, int width, int height, boolean swappedDimensions, StreamConfigurationMap map) {

        Point displaySize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
        int rotatedPreviewWidth = width;
        int rotatedPreviewHeight = height;
        int maxPreviewWidth = displaySize.x;
        int maxPreviewHeight = displaySize.y;
        if (swappedDimensions) {
            rotatedPreviewWidth = height;
            rotatedPreviewHeight = width;
            maxPreviewWidth = displaySize.y;
            maxPreviewHeight = displaySize.x;
        }

        if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
            maxPreviewWidth = MAX_PREVIEW_WIDTH;
        }

        if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
            maxPreviewHeight = MAX_PREVIEW_HEIGHT;
        }
        Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CameraUtil.CompareSizesByArea());

        return CameraUtil.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                maxPreviewHeight, largest);
    }

    /**
     * 是否支持FLASH
     */
    public static boolean supportFlash(CameraCharacteristics characteristics) {
        Boolean available = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        return available == null ? false : available;
    }

    /**
     * 从sizeMap中获取和Textureview宽高比例相同的map，然后在获取接近自己想获取到的尺寸
     *
     */
    public static Size getMinPreSize(Size[] sizeMap, int surfaceWidth, int surfaceHeight, int maxHeight) {
        float reqRatio = ((float) surfaceWidth) / surfaceHeight;
        float curRatio;
        List<Size> sizeList = new ArrayList<>();
        Size retSize = null;
        for (Size size : sizeMap) {
            curRatio = ((float) size.getHeight()) / size.getWidth();
            if (reqRatio == curRatio) {
                sizeList.add(size);
            }
        }
        if (sizeList.size() != 0) {
            for (int i = sizeList.size() - 1; i >= 0; i--) {
                if (sizeList.get(i).getWidth() >= maxHeight) {
                    retSize = sizeList.get(i);
                    break;
                }
            }
            if (retSize == null) {
                retSize = sizeList.get(sizeList.size() - 1);
            }

        } else {
            retSize = getCloselyPreSize(sizeMap, surfaceWidth, surfaceHeight);
        }
        return retSize;
    }

    private static Size getCloselyPreSize(Size[] sizeMap, int surfaceWidth, int surfaceHeight) {
        int reqTmpWidth = surfaceHeight;
        int reqTmpHeight = surfaceWidth;
        for (Size size : sizeMap) {
            if ((size.getWidth() == reqTmpWidth) && (size.getHeight() == reqTmpHeight)) {
                return size;
            }
        }
        float reqRatio = ((float) reqTmpWidth) / reqTmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Size retSize = null;
        for (Size size : sizeMap) {
            curRatio = ((float) size.getWidth()) / size.getHeight();
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }
        return retSize;
    }
}