/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/15
 * Description: PG1-Smart Team-CTPT-21 [MM] Taking Picture Coding
 ***/
package com.skysoft.smart.intranetchat.ui.activity.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;
import com.skysoft.smart.intranetchat.ui.fragment.camera.CameraFragment;

public class CameraActivity extends AppCompatActivity {

    public static void goActivity(Context context) {
        Intent intent = new Intent(context, CameraActivity.class);
        context.startActivity(intent);
    }

    public static void goActivity(Context context,boolean isCircle) {
        Intent intent = new Intent(context, CameraActivity.class);
        intent.putExtra("type",isCircle);
        context.startActivity(intent);
    }

    private boolean isCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        CustomStatusBarBackground.customStatusBarTransparent(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, CameraFragment.newInstance(getIntent().getBooleanExtra("type",isCircle)))
                .commit();
    }
}