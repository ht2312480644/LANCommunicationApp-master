/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/28
 * Description: PG1-Smart Team-CT PT-31 [MM] Video Recording Requirement
 ***/
package com.skysoft.smart.intranetchat.ui.activity.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.model.camera.entity.EventMessage;
import com.skysoft.smart.intranetchat.model.camera.manager.MyVideoManager;
import com.skysoft.smart.intranetchat.tools.QuickClickListener;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

import static com.skysoft.smart.intranetchat.MainActivity.CALL_FROM_OTHER;

public class VideoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "debug " + VideoActivity.class.getSimpleName() + " ";
    public static final int TAKE_VIDEO_URL = 10011;
    private ImageView mTakeVideo;
    private TextureView mTextureView;
    private TextView mTime;
    private MyVideoManager mVideoManager;
    private boolean isTakeVideo = false;
    private int mCountTime = 0;
    private Timer mTimer;
    private String mVideoUrl;


    public static void goActivity(Context context) {
        Intent intent = new Intent(context, VideoActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        CustomStatusBarBackground.customStatusBarTransparent(this);
        initView();
        mVideoManager.startBackgroundThread();
        textureViewIsAvailable();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        mTime = findViewById(R.id.activity_video_time);
        mTakeVideo = findViewById(R.id.activity_video_take);
        mTakeVideo.setOnClickListener(this);
        findViewById(R.id.activity_video_cancel).setOnClickListener(this);
        mTextureView = findViewById(R.id.activity_video_textureView);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        mVideoManager = new MyVideoManager(this, mTextureView);
        EventBus.getDefault().register(this);
    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            mVideoManager.openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            mVideoManager.configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_video_cancel:
                finish();
                break;
            case R.id.activity_video_take:
                if (QuickClickListener.isFastClick(1000)) takeOrCloseVideo();
                break;
        }
    }

    private void takeOrCloseVideo() {
        if (!isTakeVideo) {
            isTakeVideo = true;
            mVideoManager.takeVideo();
            startTimer();
        } else {
            stopTimer();
            mTime.setText("");
            mCountTime = 0;
            mVideoManager.closeVideo(true);
            isTakeVideo = false;
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mTime.setText(String.valueOf(msg.what));
            if (msg.what > 15) {
                takeOrCloseVideo();
            }
        }
    };

    private TimerTask mTimerTask = new TimerTask() {
        @Override
        public void run() {
            mCountTime++;
            Message message = new Message();
            message.what = mCountTime;
            mHandler.sendMessage(message);
        }
    };

    private void startTimer() {
        mTimer = new Timer();
        mTimer.schedule(mTimerTask, 1000, 1000);
    }

    private void stopTimer() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMsg(EventMessage message) {
        if (message.getType() == TAKE_VIDEO_URL) {
            mVideoUrl = message.getMessage();
            ShowVideoActivity.goActivity(this, mVideoUrl);
            finish();
        }
        if (message.getType() == CALL_FROM_OTHER) {
            finish();
        }
    }

    private void textureViewIsAvailable() {
        if (mTextureView.isAvailable()) {
            mVideoManager.openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        EventBus.getDefault().unregister(this);
        if (isTakeVideo) {
            mVideoManager.closeVideo(false);
            isTakeVideo = false;
        }
        mVideoManager.closeCamera();
        mVideoManager.stopBackgroundThread();
    }
}
