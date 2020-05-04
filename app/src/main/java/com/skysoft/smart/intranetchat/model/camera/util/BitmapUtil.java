/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/21
 * Description: PG1-Smart Team-CT PT-29 [MM] Viewing Pictures Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.model.camera.entity.EventMessage;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

import static com.skysoft.smart.intranetchat.ui.activity.camera.ClipImageActivity.TAKE_PICTURE_URL;

public class BitmapUtil {

    private static final String TAG = "debug " + BitmapUtil.class.getSimpleName() + " ";

    public static int[] getImageWidthHeight(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return new int[]{options.outWidth, options.outHeight};
    }


    /**
     * 图片等比例压缩
     */
    public static Bitmap decodeSampledBitmap(String filePath, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        int inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 计算InSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            int ratio = heightRatio < widthRatio ? heightRatio : widthRatio;
            if (ratio < 3)
                inSampleSize = ratio;
            else if (ratio < 6.5)
                inSampleSize = 4;
            else if (ratio < 8)
                inSampleSize = 8;
            else
                inSampleSize = ratio;
        }

        return inSampleSize;
    }

    public static Bitmap zoomBitmap(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) w / width);
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
    }

    public static void pictureCompression(Activity activity, String path) {
        String newPath = activity.getExternalFilesDir("compression").getAbsolutePath();
        Luban.with(activity)
                .load(path)
                .ignoreBy(100)
                .setTargetDir(newPath)
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        TLog.d(TAG, "start luban");
                    }

                    @Override
                    public void onSuccess(File file) {
                        EventBus.getDefault().post(new EventMessage(file.getAbsolutePath(), 0, TAKE_PICTURE_URL));
                        TLog.d(TAG, "luban success");
                    }

                    @Override
                    public void onError(Throwable e) {
                        TLog.e(TAG, "luban error" + e.getMessage());
                    }
                }).launch();

    }
}
