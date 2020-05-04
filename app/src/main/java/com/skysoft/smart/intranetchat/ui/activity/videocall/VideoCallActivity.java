/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/6
 * Description: [Intranet Chat] [APP] [Communication]Voice or video communication function
 */
package com.skysoft.smart.intranetchat.ui.activity.videocall;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.model.net_model.VoiceCall;
import com.skysoft.smart.intranetchat.app.impl.OnEstablishCallConnect;
import com.skysoft.smart.intranetchat.app.impl.OnReceiveCallHungUp;
import com.skysoft.smart.intranetchat.bean.RecordCallBean;
import com.skysoft.smart.intranetchat.model.camera.util.AppPermissionUtil;
import com.skysoft.smart.intranetchat.model.camera.util.CameraUtil;
import com.skysoft.smart.intranetchat.model.camera.videocall.Manager;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;
import com.skysoft.smart.intranetchat.tools.toastutil.ToastUtil;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomConfig;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

public class VideoCallActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LCH_debug: " + VideoCallActivity.class.getSimpleName() + " ";

    private TextureView mOtherTexture;
    private TextureView mTexture;
    private final String[] PERMISSIONS = {android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};
    private String mCameraId;
    private boolean backId = true;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;
    private CameraCaptureSession mCaptureSession;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private int mState = STATE_UNREADY;
    private static final int STATE_UNREADY = 0;
    private static final int STATE_READY = 1;
    private static final int STATE_USING = 2;

    private ImageView mHungUpCall;
    private ImageView mMute;
    private String host;
    private Manager mManager;
    private boolean isMute = false;
    private Surface mInputSurface;
    private String mIdentifier;
    private boolean isAnswer;
    private boolean isHaungUp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        CustomStatusBarBackground.customStatusBarTransparent(this);
        IntranetChatApplication.setStartCallTime(System.currentTimeMillis());
        mHungUpCall = findViewById(R.id.on_video_call_hung_up);
        IntranetChatApplication.getsCallback().setHungUpInAnswer(hungUp);
        IntranetChatApplication.getsCallback().setOnEstablishCallConnect(onEstablishCallConnect);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        host = bundle.getString("host");
        mIdentifier = bundle.getString("identifier");
        isAnswer = bundle.getBoolean("answer");
        startBackgroundThread();
        init();
        setMuteImg();
        mManager = new Manager();
        mHungUpCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VoiceCall.hungUpVoiceCall(host);
                IntranetChatApplication.setInCall(false);
                IntranetChatApplication.setEndCallTime(System.currentTimeMillis());
                endCall(mIdentifier, host, isAnswer, false);
                isHaungUp = true;
                finish();
            }
        });
    }


    public static void endCall(String identifier, String host, boolean isAnswer, boolean isVoice) {
        if (isAnswer) {
            EventBus.getDefault().post(new RecordCallBean(identifier, ChatRoomConfig.CALL_END_ANSWER, host, isVoice));
        } else {
            EventBus.getDefault().post(new RecordCallBean(identifier, ChatRoomConfig.CALL_END_LAUNCH, host, isVoice));
        }
    }

    private void init() {
        mOtherTexture = findViewById(R.id.activity_on_video_call_other);
        mTexture = findViewById(R.id.activity_on_video_call_mine);
        mMute = findViewById(R.id.activity_video_mute);
        findViewById(R.id.activity_video_camera_change).setOnClickListener(this);
        mMute.setOnClickListener(this);
        mOtherTexture.setSurfaceTextureListener(mSurfaceTextureListener);
        mTexture.setSurfaceTextureListener(mDecodeTextureListener);
    }

    private OnReceiveCallHungUp hungUp = new OnReceiveCallHungUp() {
        @Override
        public void onReceiveHungUpVoiceCall(String host) {
            if (!VideoCallActivity.this.host.equals(host)) {
                return;
            }
            IntranetChatApplication.setInCall(false);
            IntranetChatApplication.setEndCallTime(System.currentTimeMillis());
            endCall(mIdentifier, host, isAnswer, false);
            isHaungUp = true;
            finish();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isHaungUp){
            VoiceCall.hungUpVoiceCall(host);
            IntranetChatApplication.setInCall(false);
            IntranetChatApplication.setEndCallTime(System.currentTimeMillis());
            endCall(mIdentifier, host, isAnswer, false);
        }
        try {
            mManager.stop();
            closeCamera();
            stopBackgroundThread();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        VoiceCall.hungUpVoiceCall(host);
        IntranetChatApplication.setInCall(false);
        IntranetChatApplication.setEndCallTime(System.currentTimeMillis());
        endCall(mIdentifier, host, isAnswer, false);
        isHaungUp = true;
        finish();
    }

    public OnEstablishCallConnect onEstablishCallConnect = new OnEstablishCallConnect() {
        @Override
        public void onEstablishCallConnect() {
            TLog.d(TAG, "onEstablishCallConnect: ");
        }
    };

    public static void go(Activity activity, String host, String identifier, boolean answer) {
        Intent intent = new Intent(activity, VideoCallActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("host", host);
        bundle.putString("identifier", identifier);
        bundle.putBoolean("answer", answer);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }


    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            mInputSurface = mManager.initVCEncoder(width, height).getMediaCodec().createInputSurface();
            openCamera();
            TLog.d(TAG, "video call mSurfaceTextureListener onSurfaceTextureAvailable: ");
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            TLog.d(TAG, "onSurfaceTextureSizeChanged: ");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    private final TextureView.SurfaceTextureListener mDecodeTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            mManager.startDecoder(new Surface(surfaceTexture), i, i1,VideoCallActivity.this);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    };

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            initVideo();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;

        }
    };

    @SuppressLint("MissingPermission")
    public void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
            return;
        }
        setCameraId();
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void initVideo() {
        try {
            SurfaceTexture texture = mOtherTexture.getSurfaceTexture();
            texture.setDefaultBufferSize(4032, 1872);
            Surface previewSurface = new Surface(texture);
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mPreviewRequestBuilder.addTarget(previewSurface);
            mPreviewRequestBuilder.addTarget(mInputSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mInputSurface), new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            if (null == mCameraDevice) {
                                TLog.d(TAG, " mCameraDevice null");
                                return;
                            }
                            try {
                                mCaptureSession = cameraCaptureSession;
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO);
                                mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
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

    private CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_UNREADY:
                    mManager.startEncoder();
                    mState = STATE_READY;
                    break;
                case STATE_READY:
                    mState = STATE_USING;
                    break;
                case STATE_USING:
                    break;
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

    private void setCameraId() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (!backId) {
            mCameraId = CameraUtil.getCameraIdList(manager).getBackCameraId();
        } else {
            mCameraId = CameraUtil.getCameraIdList(manager).getFrontCameraId();
        }
    }

    private void closeCaptureSession() {
        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
    }

    public void closeCamera() {
        if (null != mCaptureSession) {
            mCaptureSession.close();
            mCaptureSession = null;
        }
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }


    public void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("VideoCallBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

    }

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
        AppPermissionUtil.requestPermissions(this, PERMISSIONS, new AppPermissionUtil.OnPermissionListener() {
            @Override
            public void onPermissionGranted() {
                finish();
                ToastUtil.toast(VideoCallActivity.this, getString(R.string.VideoCallActivity_checkPermissions_onPermissionGranted_toast_text));
            }

            @Override
            public void onPermissionDenied() {
                finish();
                ToastUtil.toast(VideoCallActivity.this, getString(R.string.VideoCallActivity_checkPermissions_onPermissionDenied_toast_text));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_video_mute:
                isMute = !isMute;
                mManager.setMute(isMute);
                setMuteImg();
                break;
            case R.id.activity_video_camera_change:
                closeCamera();
                backId = !backId;
                openCamera();
                break;
            default:
        }
    }

    private void setMuteImg() {
        if (isMute) mMute.setImageDrawable(getDrawable(R.drawable.ic_silance));
        else mMute.setImageDrawable(getDrawable(R.drawable.ic_unsilance));
    }

}
