/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/28
 * Description: PG1-Smart Team-CT PT-31 [MM] Video Recording Requirement
 ***/
package com.skysoft.smart.intranetchat.ui.activity.camera;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.view.View;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.model.camera.entity.EventMessage;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;

import org.greenrobot.eventbus.EventBus;

public class ShowVideoActivity extends AppCompatActivity implements View.OnClickListener {

    public static void goActivity(Context context, String url) {
        Intent intent = new Intent(context, ShowVideoActivity.class);
        intent.putExtra("URL", url);
        context.startActivity(intent);
    }

    private static final String TAG = "debug " + ShowVideoActivity.class.getSimpleName() + " ";

    private VideoView mShowVideo;
    private String mVideoUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);
        CustomStatusBarBackground.customStatusBarTransparent(this);
        CustomStatusBarBackground.colorViewStatusBar(this,R.color.color_black,findViewById(R.id.custom_status_bar_background));
        init();
    }

    private void init() {
        mShowVideo = findViewById(R.id.activity_show_video_VideoView);
        findViewById(R.id.activity_show_video_cancel).setOnClickListener(this);
        findViewById(R.id.activity_show_video_confirm).setOnClickListener(this);
        mVideoUrl = getIntent().getStringExtra("URL");
        initLocalVideo();
    }

    private void initLocalVideo() {
        mShowVideo.setVideoURI(Uri.parse(mVideoUrl));
        mShowVideo.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.activity_show_video_cancel:
                finish();
                break;
            case R.id.activity_show_video_confirm:
                EventBus.getDefault().post(new EventMessage(mVideoUrl,0, 2));
                TLog.d(TAG, "url " + mVideoUrl);
                finish();
                break;
        }
    }

}
