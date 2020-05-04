/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/15
 * Description: PG1-Smart Team-CTPT-21 [MM] Taking Picture Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.manager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.model.camera.util.AppPermissionUtil;
import com.skysoft.smart.intranetchat.model.camera.util.CameraUtil;
import com.skysoft.smart.intranetchat.model.camera.util.ManualFocusing;
import com.skysoft.smart.intranetchat.model.camera.widget.AutoFitTextureView;
import com.skysoft.smart.intranetchat.model.camera.widget.FocusView;
import com.skysoft.smart.intranetchat.tools.toastutil.ToastUtil;
import com.skysoft.smart.intranetchat.ui.fragment.camera.CameraFragment;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MyCameraManager {
    private static final String TAG = "debug " + CameraFragment.class.getSimpleName() + " ";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private final String[] PERMISSIONS = {android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
    /**
     * 相机状态:
     * 0: 预览
     * 1: 等待上锁(拍照片前将预览锁上保证图像不在变化)
     * 2: 等待预拍照(对焦, 曝光等操作)
     * 3: 等待非预拍照(闪光灯等操作)
     * 4: 已经获取照片
     */
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_LOCK = 1;
    private static final int STATE_WAITING_PRECAPTURE = 2;
    private static final int STATE_WAITING_NON_PRECAPTURE = 3;
    private static final int STATE_PICTURE_TAKEN = 4;

    /**
     * 闪光灯
     */
    public static final int AUTO_FLASH = 0;
    public static final int OPEN_FLASH = 1;
    public static final int CLOSE_FLASH = 2;

    /**
     * 正在使用的相机id
     */
    private String mCameraId;

    /**
     * 默认后置摄像头
     */
    private static boolean sBackId = false;

    /**
     * 摄像头方向
     */
    private int mSensorOrientation;

    /**
     * 预览用的获取会话
     */
    private CameraCaptureSession mCaptureSession;

    /**
     * 正在使用的相机
     */
    private CameraDevice mCameraDevice;

    /**
     * 预览数据的尺寸
     */
    private Size mPreviewSize;

    /**
     * 处理拍照等工作的子线程
     */
    private HandlerThread mBackgroundThread;

    /**
     * 子线程的处理器
     */
    private Handler mBackgroundHandler;

    /**
     * 静止页面拍照处理器
     */
    private ImageReader mImageReader;

    /**
     * 输出照片的文件
     */
    private File mFile;

    /**
     * 预览请求构建器
     */
    private CaptureRequest.Builder mPreviewRequestBuilder;

    /**
     * 预览请求
     */
    private CaptureRequest mPreviewRequest;

    /**
     * 当前的相机状态, 初始化为预览
     */
    private int mState = STATE_PREVIEW;

    /**
     * 信号量控制器
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * 当前相机设备是否支持Flash
     */
    private static boolean mFlashSupported;
    private int mFlashTemp = CLOSE_FLASH;
    private int mFlash = mFlashTemp;

    private AutoFitTextureView mTextureView;
    private FocusView mForcusView;
    private Activity mActivity;
    private float mFingerSpacing = 0;
    private int mZoomLevel = 1;
    private Rect mZoom;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);

    }


    public MyCameraManager(Activity activity, AutoFitTextureView textureView, FocusView focusView) {
        mActivity = activity;
        mTextureView = textureView;
        mForcusView = focusView;
        mFile = new File(activity.getExternalFilesDir("camera"), System.currentTimeMillis() + ".jpg");
    }

    /**
     * 相机状态改变的回调函数
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            if (null != mActivity) {
                TLog.d(TAG, "CameraDevice.StateCallback onError finish");
                mActivity.finish();
            }
        }

    };

    /**
     * 通过cameraId打开特定的相机
     */
    @SuppressLint("MissingPermission")
    public void openCamera(int width, int height) {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            TLog.d(TAG, "permission fail");
            checkPermissions();
            return;
        }
        setCameraId();
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * 创建预览对话
     */
    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface surface = new Surface(texture);
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            mCameraDevice.createCaptureSession(Arrays.asList(surface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            if (null == mCameraDevice) {
                                TLog.d(TAG, " mCameraDevice null");
                                return;
                            }

                            mCaptureSession = cameraCaptureSession;
                            try {
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
                                mForcusView.initFocusArea(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                                setFlash(mPreviewRequestBuilder);
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(
                                @NonNull CameraCaptureSession cameraCaptureSession) {
                            TLog.d(TAG, "onConfigureFailed fail");
                        }
                    }, null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 捕获会话回调函数
     */
    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW: {
                    break;
                }
                case STATE_WAITING_LOCK: {
                    waitLock(result);
                    break;
                }
                case STATE_WAITING_PRECAPTURE: {
                    waitPrePicture(result);
                    break;
                }
                case STATE_WAITING_NON_PRECAPTURE: {
                    waitNoPrePicture(result);
                    break;
                }
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            process(result);
        }

    };

    /**
     * ImageReader的回调函数, 其中的onImageAvailable会在照片准备好可以被保存时调用
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new CameraUtil.ImageSaver(reader.acquireNextImage(), mFile));
        }

    };

    /**
     * 实现拍照的方法
     */
    public void takePicture() {
        lockFocus();
    }

    /**
     * 等待预拍照
     */
    private void waitPrePicture(CaptureResult result) {
        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE || aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
            mState = STATE_WAITING_NON_PRECAPTURE;
        }
    }

    /**
     * 等待非预拍照
     */
    private void waitNoPrePicture(CaptureResult result) {
        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
        if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
            mState = STATE_PICTURE_TAKEN;
            captureStillPicture();
        }
    }

    /**
     * 执行预拍照操作
     */
    private void runPrecaptureSequence() {
        try {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            mState = STATE_WAITING_PRECAPTURE;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拍照操作
     */
    private void captureStillPicture() {
        try {
            if (null == mActivity || null == mCameraDevice) {
                return;
            }
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            if (mZoom != null) captureBuilder.set(CaptureRequest.SCALER_CROP_REGION, mZoom);
            mFlash = mFlashTemp;
            setFlash(captureBuilder);
            captureBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
            int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getOrientation(rotation));
            CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    TLog.d(TAG, mFile.toString());
                    unlockFocus();
                }
            };

            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();
            mCaptureSession.capture(captureBuilder.build(), captureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 等待锁定的状态
     */
    private void waitLock(CaptureResult result) {
        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
        if (afState == null) {
            captureStillPicture();
        } else if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_INACTIVE == afState) {
            Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
            if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                mState = STATE_PICTURE_TAKEN;
                captureStillPicture();
            } else {
                runPrecaptureSequence();
            }
        }

    }

    /**
     * 锁定焦点
     */
    private void lockFocus() {
        try {
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
            mState = STATE_WAITING_LOCK;
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解开锁定的焦点
     */
    private void unlockFocus() {
        mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
        mFlash = CLOSE_FLASH;
        setFlash(mPreviewRequestBuilder);
        try {
            mCaptureSession.capture(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
            mState = STATE_PREVIEW;
            mCaptureSession.setRepeatingRequest(mPreviewRequest, mCaptureCallback,
                    mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 屏幕方向发生改变时调用转换数据方法
     *
     * @param viewWidth  mTextureView 的宽度
     * @param viewHeight mTextureView 的高度
     */
    public void configureTransform(int viewWidth, int viewHeight) {

        if (null == mTextureView || null == mPreviewSize || null == mActivity) {
            return;
        }
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        mTextureView.setTransform(CameraUtil.configureTransform(viewWidth, viewHeight, mPreviewSize, rotation));
    }

    /**
     * 设置相机的输出, 包括预览和拍照
     * <p>
     * 1. 获取当前的摄像头, 并将拍照输出设置为最高画质
     * 2. 判断显示方向和摄像头传感器方向是否一致, 是否需要旋转画面
     * 3. 获取当前显示尺寸和相机的输出尺寸, 选择最合适的预览尺寸
     *
     * @param width  预览宽度
     * @param height 预览高度
     */
    private void setUpCameraOutputs(int width, int height) {

        CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = null;
        try {
            characteristics = manager.getCameraCharacteristics(mCameraId);
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CameraUtil.CompareSizesByArea());
            mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(), ImageFormat.JPEG, /*maxImages*/2);
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
            boolean swappedDimensions = CameraUtil.needRotation(mActivity, manager, mCameraId);
            mPreviewSize = CameraUtil.setPreViewSize(mActivity, width, height, swappedDimensions, map);
            getOrientation();
            mFlashSupported = CameraUtil.supportFlash(characteristics);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取当前的屏幕方向
     */
    private void getOrientation() {
        int orientation = mActivity.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        } else {
            mTextureView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
        }
    }

    private int getOrientation(int rotation) {
        return (ORIENTATIONS.get(rotation) + mSensorOrientation + 270) % 360;
    }

    private void setFlash(CaptureRequest.Builder requestBuilder) {
        if (mFlashSupported) {
            switch (mFlash) {
                case OPEN_FLASH:
                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                    break;
                case AUTO_FLASH:
                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                    break;
                case CLOSE_FLASH:
                    requestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
                    requestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                    break;
                default:
            }
        }
    }

    private void setCameraId() {
        CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        if (sBackId) {
            mCameraId = CameraUtil.getCameraIdList(manager).getBackCameraId();
        } else {
            mCameraId = CameraUtil.getCameraIdList(manager).getFrontCameraId();
        }
    }

    public void changeId() {
        sBackId = !sBackId;
        setCameraId();
    }

    public void closeFlash() {
        mFlashTemp = CLOSE_FLASH;
    }

    public void openFlash() {
        mFlashTemp = OPEN_FLASH;
    }

    public void autoFlash() {
        mFlashTemp = AUTO_FLASH;
    }

    public int getFlash() {
        return mFlashTemp;
    }

    public void setManualFocusing(float x, float y) {
        mPreviewRequestBuilder = new ManualFocusing(mActivity, mCameraId, mPreviewSize, x, y).setManualFocusing(mPreviewRequestBuilder);
        try {
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void setZoom(MotionEvent event) {
        CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = null;
        try {
            characteristics = manager.getCameraCharacteristics(mCameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        float maxZoom = characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) * 5;
        Rect rect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        float currentFingerSpacing = getFingerSpacing(event);
        if (mFingerSpacing != 0) {
            if (currentFingerSpacing > mFingerSpacing && maxZoom > mZoomLevel) {
                mZoomLevel++;
            } else if (currentFingerSpacing < mFingerSpacing && mZoomLevel > 1) {
                mZoomLevel--;
            }
            int minW = (int) (rect.width() / maxZoom);
            int minH = (int) (rect.height() / maxZoom);
            int difW = rect.width() - minW;
            int difH = rect.height() - minH;
            int cropW = difW / 100 * mZoomLevel;
            int cropH = difH / 100 * mZoomLevel;
            cropW -= cropW & 3;
            cropH -= cropH & 3;
            mZoom = new Rect(cropW, cropH, rect.width() - cropW, rect.height() - cropH);
            mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, mZoom);
        }
        mFingerSpacing = currentFingerSpacing;
        try {
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), mCaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 关闭正在使用的相机
     */
    public void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (null != mImageReader) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    /**
     * 开启子线程
     */
    public void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

    }

    /**
     * 停止子线程
     */
    public void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkPermissions() {
        AppPermissionUtil.requestPermissions(mActivity, PERMISSIONS, new AppPermissionUtil.OnPermissionListener() {
            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied() {
                mActivity.finish();
                ToastUtil.toast(mActivity, String.valueOf(R.string.MyCameraManager_onPermissionDenied_toast_text));
            }
        });
    }
}
