/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/21
 * Description: PG1-Smart Team-CT PT-29 [MM] Viewing Pictures Coding
 ***/
package com.skysoft.smart.intranetchat.ui.activity.camera;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.model.camera.entity.EventMessage;
import com.skysoft.smart.intranetchat.model.camera.util.FileUtil;
import com.skysoft.smart.intranetchat.model.camera.widget.ClipViewLayout;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;

import org.greenrobot.eventbus.EventBus;


public class ClipImageActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "debug " + ClipImageActivity.class.getSimpleName() + " ";
    public static final int TAKE_PICTURE_URL = 1;
    public static final int HEAD_PICTURE_URL = 4;
    public static final String TYPE = "type_clip";
    private ClipViewLayout mClipViewLayout;
    private TextView mCancel;
    private TextView mDetermine;
    private Uri mOldUri;
    private String mNewUri;

    public static void goActivity(Context context, Uri uri) {
        Intent intent = new Intent(context, ClipImageActivity.class);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static void goActivity(Context context, Uri uri, boolean isCircle) {
        Intent intent = new Intent(context, ClipImageActivity.class);
        intent.setData(uri);
        intent.putExtra(TYPE, isCircle);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clip_image);
        CustomStatusBarBackground.customStatusBarTransparent(this);
        initView();
    }

    public void initView() {
        mClipViewLayout = findViewById(R.id.clipViewLayout);
        mCancel = findViewById(R.id.clip_image_cancel);
        mDetermine = findViewById(R.id.clip_image_determine);
        CustomStatusBarBackground.colorViewStatusBar(this,R.color.color_black,findViewById(R.id.custom_status_bar_background));
        mCancel.setOnClickListener(this);
        mDetermine.setOnClickListener(this);
        mOldUri = getIntent().getData();
        mClipViewLayout.setVisibility(View.VISIBLE);
        mClipViewLayout.setIsCircle(getIntent().getBooleanExtra(TYPE, false));
        mClipViewLayout.setImageSrc(mOldUri);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clip_image_cancel:
                finish();
                break;
            case R.id.clip_image_determine:
                mNewUri = FileUtil.saveClipImg(this, mClipViewLayout.getPath(), mClipViewLayout.clip());
                if (!getIntent().getBooleanExtra(TYPE, false)) {
                    EventBus.getDefault().post(new EventMessage(mNewUri, 0, TAKE_PICTURE_URL));
                } else {
                    EventBus.getDefault().post(new EventMessage(mNewUri, 0, HEAD_PICTURE_URL));
                }
                finish();
                break;
            default:
        }
    }

}
