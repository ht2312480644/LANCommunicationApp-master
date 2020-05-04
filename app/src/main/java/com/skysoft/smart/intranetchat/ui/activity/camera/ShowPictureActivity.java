/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/15
 * Description: PG1-Smart Team-CT PT-21 [MM] Taking Picture Coding
 ***/
package com.skysoft.smart.intranetchat.ui.activity.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.model.camera.util.BitmapUtil;
import com.skysoft.smart.intranetchat.model.camera.util.FileUtil;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;


public class ShowPictureActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "debug " + ShowPictureActivity.class.getSimpleName() + " ";
    private Uri mUri;
    private ImageView mShowPicture;

    public static void goActivity(Context context, Uri uri, boolean isCircle) {
        Intent intent = new Intent(context, ShowPictureActivity.class);
        intent.setData(uri);
        intent.putExtra("type", isCircle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_picture);
        CustomStatusBarBackground.customStatusBarTransparent(this);
        CustomStatusBarBackground.colorViewStatusBar(this,R.color.color_black,findViewById(R.id.custom_status_bar_background));
        initView();
    }

    @SuppressLint("CheckResult")
    private void initView() {
        mShowPicture = findViewById(R.id.show_picture);
        findViewById(R.id.show_picture_cancel).setOnClickListener(this);
        findViewById(R.id.show_picture_use_it).setOnClickListener(this);
        findViewById(R.id.show_picture_cut).setOnClickListener(this);
        mUri = getIntent().getData();
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE);
        Glide.with(this).load(mUri).apply(requestOptions).into(mShowPicture);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.show_picture_cancel:
                finish();
                break;
            case R.id.show_picture_use_it:
                BitmapUtil.pictureCompression(this,FileUtil.getRealFilePathFromUri(mUri, this));
                finish();
                break;
            case R.id.show_picture_cut:
                ClipImageActivity.goActivity(this, mUri, getIntent().getBooleanExtra("type", false));
                finish();
                break;
            default:
        }

    }
}
