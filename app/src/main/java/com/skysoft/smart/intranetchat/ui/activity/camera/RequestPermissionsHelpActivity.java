/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/15
 * Description: PG1-Smart Team-CT PT-21 [MM] Taking Picture Coding
 ***/
package com.skysoft.smart.intranetchat.ui.activity.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.skysoft.smart.intranetchat.model.camera.util.AppPermissionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 动态请求权限帮助activity，与AppPermissionUtil联用。
 * 注：这个不是我们app的页面，所有不要轻易改动。
 */
public class RequestPermissionsHelpActivity extends AppCompatActivity {

    private int requestCode;

    private static AppPermissionUtil.OnPermissionListener transferOnPermissionListener;

    private AppPermissionUtil.OnPermissionListener mOnPermissionListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);

        getIntentData();
    }

    private void getIntentData(){
        mOnPermissionListener=transferOnPermissionListener;
        transferOnPermissionListener=null;
        String[] permissions=getIntent().getStringArrayExtra("permissions");
        if(permissions!=null&&permissions.length>0){
            requestPermissions(permissions);
        }else {
            throw new NullPointerException("申请的权限列表不能为空！");
        }
    }

    /**
     * 去申请所有权限
     * @param permissions
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissions(String[] permissions){
        Random random=new Random();
        requestCode=random.nextInt(1000);
        List<String> deniedPermissions = getDeniedPermissions(permissions);
        if (deniedPermissions.size() > 0) {
            requestPermissions(deniedPermissions.toArray(new String[deniedPermissions.size()]), requestCode);
        } else {
            if(mOnPermissionListener!=null)
                mOnPermissionListener.onPermissionGranted();
            if(!isFinishing()) {
                finish();
            }
        }
    }

    /**
     * 请求权限结果
     */
    public void requestPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode != -1) {
            if (verifyPermissions(grantResults)) {
                if(mOnPermissionListener!=null)
                    mOnPermissionListener.onPermissionGranted();
                finish();
            } else {
                if(mOnPermissionListener!=null)
                    mOnPermissionListener.onPermissionDenied();
                finish();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        requestPermissionsResult(requestCode,grantResults);
    }

    /**
     * 获取请求权限中需要授权的权限,有的可能已经授权过了
     */
    private List<String> getDeniedPermissions(String[] permissions) {
        List<String> deniedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permission);
            }
        }
        return deniedPermissions;
    }

    /**
     * 验证所有权限是否都已经授权
     */
    private static boolean verifyPermissions(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOnPermissionListener=null;
    }

    /**
     * 启动activity，并带些必要参数过来
     * @param context
     * @param permissions 申请权限列表
     * @param listener 结果回调
     */
    public static void start(Context context, String[] permissions, AppPermissionUtil.OnPermissionListener listener){
        Intent intent = new Intent(context, RequestPermissionsHelpActivity.class);
        intent.putExtra("permissions",permissions);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        transferOnPermissionListener=listener;
    }
}