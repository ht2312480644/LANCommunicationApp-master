/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/28
 * Description: PG1-Smart Team-CT PT-31 [MM] Video Recording Requirement
 ***/
package com.skysoft.smart.intranetchat.model.camera.manager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.model.camera.mediacodec.VideoEncoder;
import com.skysoft.smart.intranetchat.model.camera.mediamuxer.MyMediaMuxer;
import com.skysoft.smart.intranetchat.model.camera.util.AppPermissionUtil;
import com.skysoft.smart.intranetchat.model.camera.util.CameraUtil;
import com.skysoft.smart.intranetchat.tools.toastutil.ToastUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MyVideoManager {
    private static final String TAG = "debug " + MyVideoManager.class.getSimpleName() + " ";

    private final static String MIME_FORMAT = "video/avc";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private final String[] PERMISSIONS = {android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};

    private String mCameraId;
    private boolean backId = true;

    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private CameraCaptureSession mCaptureSession;
    private Size mPreviewSize;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    /**
     * 信号量控制器
     */
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private TextureView mTextureView;
    private Activity mActivity;
    private VideoEncoder mVideoEncoder;
    private MyAudioManager mAudioManager;
    private MyMediaMuxer mMediaMuxer;
    private Surface mEncoderSurface;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);

    }


    public MyVideoManager(Activity activity, TextureView textureView) {
        mActivity = activity;
        mTextureView = textureView;
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
        mMediaMuxer = new MyMediaMuxer();
        mVideoEncoder = new VideoEncoder(MIME_FORMAT, mMediaMuxer);
        mAudioManager = new MyAudioManager(mMediaMuxer);
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
     * 创建预览
     */
    private void createCameraPreviewSession() {
        try {

            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface surface = new Surface(texture);
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            TLog.d(TAG, "mPreviewSize.getWidth() " + mPreviewSize.getWidth() + " mPreviewSize.getHeight() " + mPreviewSize.getHeight());
            mCameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            if (null == mCameraDevice) {
                                TLog.d(TAG, " mCameraDevice null");
                                return;
                            }
                            mCaptureSession = cameraCaptureSession;
                            try {
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
                                mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
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

    public void configureTransform(int viewWidth, int viewHeight) {

        if (null == mTextureView || null == mPreviewSize || null == mActivity) {
            return;
        }
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        mTextureView.setTransform(CameraUtil.configureTransform(viewWidth, viewHeight, mPreviewSize, rotation));
    }

    private void setUpCameraOutputs(int width, int height) {
        CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        CameraCharacteristics characteristics = null;
        try {
            characteristics = manager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = CameraUtil.getMinPreSize(map.getOutputSizes(SurfaceTexture.class), width, height, 1000);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化录像预览
     */
    private void init() {
        try {
            closeCaptureSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            Surface previewSurface = new Surface(texture);
            mPreviewRequestBuilder.addTarget(previewSurface);
            mEncoderSurface = mVideoEncoder.getMediaCodec().createInputSurface();
            mPreviewRequestBuilder.addTarget(mEncoderSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mEncoderSurface), new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            if (null == mCameraDevice) {
                                TLog.d(TAG, " mCameraDevice null");
                                return;
                            }
                            try {
                                mCaptureSession = cameraCaptureSession;
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
                                mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
                                mPreviewRequest = mPreviewRequestBuilder.build();
                                mCaptureSession.setRepeatingRequest(mPreviewRequest, null, mBackgroundHandler);
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

    public void takeVideo() {
        init();
        mVideoEncoder.startEncoder();
        mAudioManager.startAudioRecord();
    }

    public void closeVideo(boolean isSend) {
        mAudioManager.stop();
        mVideoEncoder.stopEncoder();
        mVideoEncoder.release();
        mMediaMuxer.stopMuxer(isSend);
        closeCaptureSession();
    }

    private void closeCaptureSession() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
    }

    private void setCameraId() {
        CameraManager manager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);
        if (backId) {
            mCameraId = CameraUtil.getCameraIdList(manager).getBackCameraId();
        } else {
            mCameraId = CameraUtil.getCameraIdList(manager).getFrontCameraId();
        }
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
        mBackgroundThread = new HandlerThread("VideoBackground");
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
                mActivity.finish();
                ToastUtil.toast(mActivity, String.valueOf(R.string.MyVideoManager_checkPermissions_onPermissionGranted_toast_text));
            }

            @Override
            public void onPermissionDenied() {
                mActivity.finish();
                ToastUtil.toast(mActivity, String.valueOf(R.string.MyVideoManager_checkPermissions_onPermissionDenied_toast_text));
            }
        });
    }

}
