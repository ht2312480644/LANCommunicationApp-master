/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/23
 * Description: PG1-Smart Team-CT PT-29 [MM] Viewing Pictures Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {
    private static final String TAG = "debug " + FileUtil.class.getSimpleName() + " ";

    /**
     * 根据Uri返回文件绝对路径
     * file:/// 和 content://
     */
    public static String getRealFilePathFromUri(Uri uri, Context context) {
        if (null == uri) return null;
        String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equalsIgnoreCase(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equalsIgnoreCase(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    /**
     * 保存修改后的图片
     */
    public static String saveClipImg(Context context, String oldPath, Bitmap bitmap) {
        File file1 = new File(oldPath);
        File file = new File(context.getExternalFilesDir("modifiedPicture"), file1.getName());
        if (bitmap == null) {
            return null;
        }
        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != output) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return FileUtil.getRealFilePathFromUri(Uri.fromFile(file), context);
    }

    public static String getVideoPath() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.skysoft.smart.intranetchat/files/ICVideo/";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public static void checkFile(String path){
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
}
