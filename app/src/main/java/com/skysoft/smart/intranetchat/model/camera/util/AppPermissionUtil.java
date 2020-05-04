/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/15
 * Description: PG1-Smart Team-CT PT-21 [MM] Taking Picture Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.util;

import android.content.Context;
import android.os.Build;

import com.skysoft.smart.intranetchat.ui.activity.camera.RequestPermissionsHelpActivity;


public class AppPermissionUtil {

    /**
     * 去请求所有权限
     * @param context
     * @param permissions 需要请求的权限列表
     * @param listener 请求权限回调
     */
    public static void requestPermissions(Context context, String[] permissions, OnPermissionListener listener) {
        if(context==null||listener==null){
            throw new NullPointerException("context参数为空，或者listener参数为空");
        }

        if (Build.VERSION.SDK_INT <= 22) {
            listener.onPermissionGranted();
        } else {
            RequestPermissionsHelpActivity.start(context,permissions,listener);
        }
    }

    public interface OnPermissionListener {

        void onPermissionGranted();//授权

        void onPermissionDenied();//拒绝
    }
}

