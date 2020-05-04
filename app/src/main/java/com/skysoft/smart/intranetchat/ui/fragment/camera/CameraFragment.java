/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/15
 * Description: PG1-Smart Team-CT PT-21 [MM] Taking Picture Coding
 ***/
package com.skysoft.smart.intranetchat.ui.fragment.camera;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.tools.QuickClickListener;
import com.skysoft.smart.intranetchat.ui.activity.camera.ClipImageActivity;
import com.skysoft.smart.intranetchat.ui.activity.camera.ShowPictureActivity;
import com.skysoft.smart.intranetchat.model.camera.entity.EventMessage;
import com.skysoft.smart.intranetchat.model.camera.manager.MyCameraManager;
import com.skysoft.smart.intranetchat.model.camera.widget.AutoFitTextureView;
import com.skysoft.smart.intranetchat.model.camera.widget.FocusView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.skysoft.smart.intranetchat.model.camera.manager.MyCameraManager.CLOSE_FLASH;
import static com.skysoft.smart.intranetchat.model.camera.manager.MyCameraManager.OPEN_FLASH;


public class CameraFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "debug " + CameraFragment.class.getSimpleName() + " ";

    public static final int SAVE_PITCURE_OK = 10010;
    private AutoFitTextureView mTextureView;
    private LinearLayout mShowLightningOptions;
    private ImageView mLightningImg;
    private float mDownX;
    private float mDownY;
    private FocusView mForcusView;
    private RelativeLayout mRootView;
    private MyCameraManager mCameraManager;
    private int mFinger;
    private boolean isCircle;
    private boolean isCapture = false;


    public static CameraFragment newInstance(boolean isCircle) {
        CameraFragment cameraFragment = new CameraFragment();
        Bundle args = new Bundle();
        args.putBoolean("type", isCircle);
        cameraFragment.setArguments(args);
        return cameraFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isCircle = getArguments().getBoolean("type", false);
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        view.findViewById(R.id.fragment_camera2_take_picture).setOnClickListener(this);
        view.findViewById(R.id.fragment_camera2_change_camera_id).setOnClickListener(this);
        view.findViewById(R.id.fragment_camera2_open_lighting).setOnClickListener(this);
        view.findViewById(R.id.fragment_camera2_auto_lighting).setOnClickListener(this);
        view.findViewById(R.id.fragment_camera2_close_lighting).setOnClickListener(this);
        view.findViewById(R.id.fragment_camera2_cancel).setOnClickListener(this);
        mShowLightningOptions = view.findViewById(R.id.fragment_camera2_show_hide_lightning);
        mLightningImg = view.findViewById(R.id.fragment_camera2_lighting);
        mLightningImg.setOnClickListener(this);
        mTextureView = view.findViewById(R.id.texture);
        mTextureView.setOnTouchListener(mSurfaceTextureOnTouchListener);
        mRootView = view.findViewById(R.id.root_view);
        mForcusView = new FocusView(getContext());
        mForcusView.setVisibility(View.GONE);
        mRootView.addView(mForcusView);
        mCameraManager = new MyCameraManager(getActivity(), mTextureView, mForcusView);
        setLightningImg();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraManager.startBackgroundThread();
        textureViewIsAvailable();
    }

    @Override
    public void onPause() {
        mCameraManager.closeCamera();
        mCameraManager.stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMsg(EventMessage message) {
        if (message.getType() == SAVE_PITCURE_OK) {
            if (!isCircle) {
                ShowPictureActivity.goActivity(getActivity(), Uri.parse(message.getMessage()), isCircle);
            } else {
                ClipImageActivity.goActivity(getActivity(), Uri.parse(message.getMessage()), isCircle);
            }
            getActivity().finish();
        }
    }

    /**
     * 当屏幕关闭后重新打开, 若SurfaceTexture已经就绪, 此时onSurfaceTextureAvailable不会被回调, 这种情况下
     * 如果SurfaceTexture已经就绪, 则直接打开相机, 否则等待SurfaceTexture已经就绪的回调
     */
    private void textureViewIsAvailable() {
        if (mTextureView.isAvailable()) {
            mCameraManager.openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    /**
     * SurfaceTexture监听器
     */
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            mCameraManager.openCamera(width, height);

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            mCameraManager.configureTransform(width, height);

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    private TextureView.OnTouchListener mSurfaceTextureOnTouchListener = new TextureView.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mFinger = 1;
                    mDownX = event.getX();
                    mDownY = event.getY();
                    mCameraManager.setManualFocusing(mDownX, mDownY);
                    mForcusView.moveToPosition(mDownX, mDownY);
                    break;
                case MotionEvent.ACTION_UP:
                    mFinger = 0;
                    mForcusView.hideFocusView();
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    mFinger += 1;
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mFinger -= 1;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mFinger >= 2) {
                        mCameraManager.setZoom(event);
                    }
                    break;
            }
            return true;
        }
    };

    private void setLightningImg() {
        int mFlash = mCameraManager.getFlash();
        if (mFlash == CLOSE_FLASH) {
            Glide.with(getActivity()).load(R.drawable.ic_lightning_close).into(mLightningImg);
        } else if (mFlash == OPEN_FLASH) {
            Glide.with(getActivity()).load(R.drawable.ic_lightning).into(mLightningImg);
        } else {
            Glide.with(getActivity()).load(R.drawable.ic_lightning_auto).into(mLightningImg);
        }
    }

    private void showLightningOptions() {
        if (mShowLightningOptions.getVisibility() == View.GONE) {
            mShowLightningOptions.setVisibility(View.VISIBLE);
        } else {
            mShowLightningOptions.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fragment_camera2_take_picture:
                isCapture = true;
                if (QuickClickListener.isFastClick()) mCameraManager.takePicture();
                break;
            case R.id.fragment_camera2_change_camera_id:
                if (isCapture) return;
                mCameraManager.closeCamera();
                mCameraManager.changeId();
                textureViewIsAvailable();
                isCapture = false;
                break;
            case R.id.fragment_camera2_lighting:
                showLightningOptions();
                break;
            case R.id.fragment_camera2_close_lighting:
                mCameraManager.closeFlash();
                mShowLightningOptions.setVisibility(View.GONE);
                break;
            case R.id.fragment_camera2_open_lighting:
                mCameraManager.openFlash();
                mShowLightningOptions.setVisibility(View.GONE);
                break;
            case R.id.fragment_camera2_auto_lighting:
                mCameraManager.autoFlash();
                mShowLightningOptions.setVisibility(View.GONE);
                break;
            case R.id.fragment_camera2_cancel:
                getActivity().finish();
                break;
            default:
        }
        setLightningImg();
    }

}