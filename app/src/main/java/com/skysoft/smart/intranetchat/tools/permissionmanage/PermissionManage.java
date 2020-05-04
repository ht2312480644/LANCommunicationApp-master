/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/30
 * Description: [PT-60][Intranet Chat] [APP][UI] contact list page
 */
package com.skysoft.smart.intranetchat.tools.permissionmanage;

import android.Manifest;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.tbruyelle.rxpermissions2.RxPermissions;

import androidx.fragment.app.FragmentActivity;

public class PermissionManage{

    public static void allPermissionManage(FragmentActivity activity){
        RxPermissions rxPermissions = new RxPermissions(activity);
        rxPermissions.requestEach(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR)
                .subscribe(permission -> {
                    TLog.d("权限判断！", "allPermissionManage: 判断权限允许、拒绝、不再询问。");
                    if (permission.granted) {
                        TLog.i("未拒绝！","权限名称:"+permission.name+",申请结果:"+permission.granted);
                    } else if (permission.shouldShowRequestPermissionRationale) {
                        TLog.i("仅拒绝！","权限名称:"+permission.name+",申请结果:"+permission.granted);
                    } else {
                        TLog.i("拒绝后不再询问！","权限名称:"+permission.name+",申请结果:"+permission.granted);
                    }
                });
    }
}
