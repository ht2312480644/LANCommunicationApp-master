package com.skysoft.smart.intranetchat.ui.activity.camera;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.model.camera.widget.ClipViewLayout;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;

/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/11/8
 * Description: PG1-Smart Team-CT PT-72 [MM] Video Communication Coding
 ***/
public class PictureShowActivity extends AppCompatActivity {

    public static void goActivity(Context context, String url) {
        Intent intent = new Intent(context, PictureShowActivity.class);
        intent.putExtra("url", url);
        context.startActivity(intent);
    }

    private static final String TAG = "jesse: " + PictureShowActivity.class.getSimpleName() + " ";
    private ClipViewLayout mClipViewLayout;
    private String mUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_show);
        CustomStatusBarBackground.customStatusBarTransparent(this);
        CustomStatusBarBackground.colorViewStatusBar(this,R.color.color_black,findViewById(R.id.custom_status_bar_background));
        init();
    }

    private void init() {
        mClipViewLayout = findViewById(R.id.clipViewLayout_picture);
        mClipViewLayout.setVisibility(View.VISIBLE);
        mClipViewLayout.setShow(true);
        mClipViewLayout.setActivity(this);
        mUrl = getIntent().getStringExtra("url");
        mClipViewLayout.setImageSrc(Uri.parse(mUrl));
    }

}
