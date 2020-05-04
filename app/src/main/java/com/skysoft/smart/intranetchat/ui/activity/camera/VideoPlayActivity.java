package com.skysoft.smart.intranetchat.ui.activity.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;

/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/11/8
 * Description: PG1-Smart Team-CT PT-72 [MM] Video Communication Coding
 ***/
public class VideoPlayActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "jesse: " + VideoPlayActivity.class.getSimpleName() + " ";
    private ImageView mImgPlay;
    private VideoView mVideoView;
    private String mPath;
    private RelativeLayout mRelative;
    private float mDownX;
    private float mUpX;

    public static void goActivity(Context context, String url) {
        Intent intent = new Intent(context, VideoPlayActivity.class);
        intent.putExtra("URL", url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        CustomStatusBarBackground.customStatusBarTransparent(this);
        CustomStatusBarBackground.colorViewStatusBar(this,R.color.color_black,findViewById(R.id.custom_status_bar_background));
        init();
        initVideo();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init() {
        mImgPlay = findViewById(R.id.activity_video_play_start);
        mVideoView = findViewById(R.id.activity_video_play);
        mRelative = findViewById(R.id.activity_video_play_relative_layout);
        mPath = getIntent().getStringExtra("URL");
        mRelative.setOnTouchListener(mRelativeOnTouchListener);
        mImgPlay.setOnClickListener(this);
    }

    private RelativeLayout.OnTouchListener mRelativeOnTouchListener = new RelativeLayout.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = event.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    mUpX = event.getX();
                    if (Math.sqrt(Math.abs(mDownX * mDownX - mUpX * mUpX)) > 150) {
                        finish();
                    }
                    break;
                default:
            }
            return true;
        }
    };


    private void initVideo() {
        MediaController mediaController = new MediaController(this);
        mVideoView.setMediaController(mediaController);
        mVideoView.setVideoURI(Uri.parse(mPath));
        mVideoView.setOnPreparedListener(mp -> mVideoView.start());
        mVideoView.setOnCompletionListener(mp -> mImgPlay.setVisibility(View.VISIBLE));
    }

    @Override
    public void onClick(View v) {
        mVideoView.start();
        mImgPlay.setVisibility(View.GONE);
    }
}
