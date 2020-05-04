/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/15
 * Description: PG1-Smart Team-CTPT-21 [MM] Taking Picture Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.Size;

public class ManualFocusing {

    private Rect mPreviewRect;
    private Rect mFocusRect = new Rect();
    private float currentX;
    private float currentY;
    private CoordinateTransformer mTransformer;
    private CameraManager mCameraManager;
    private CameraCharacteristics mCameraCharacteristics;
    private MeteringRectangle focusRect;

    public ManualFocusing(Activity activity, String cameraId, Size previewSize, float x, float y) {
        mCameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            mCameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        currentX = x;
        currentY = y;
        mPreviewRect = new Rect(0, 0, previewSize.getWidth(), previewSize.getHeight());
        setMeteringRectangle();
    }


    public CaptureRequest.Builder setManualFocusing(CaptureRequest.Builder requestBuilder) {
        MeteringRectangle[] rectangle = new MeteringRectangle[]{focusRect};
        requestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_AUTO);
//        requestBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, rectangle);
        requestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, rectangle);
        requestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
//        requestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        return requestBuilder;
    }


    private void setMeteringRectangle() {

        mTransformer = new CoordinateTransformer(mCameraCharacteristics, rectToRectF(mPreviewRect));
        int areaSize = mPreviewRect.width() / 5;
        int left = clamp((int) currentX - areaSize / 2, mPreviewRect.left, mPreviewRect.right - areaSize);
        int top = clamp((int) currentY - areaSize / 2, mPreviewRect.top, mPreviewRect.bottom - areaSize);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        toFocusRect(mTransformer.toCameraSpace(rectF));
        focusRect = new MeteringRectangle(mFocusRect, 1000);
    }


    private RectF rectToRectF(Rect rect) {
        return new RectF(rect);
    }

    private void toFocusRect(RectF rectF) {
        mFocusRect.left = Math.round(rectF.left);
        mFocusRect.top = Math.round(rectF.top);
        mFocusRect.right = Math.round(rectF.right);
        mFocusRect.bottom = Math.round(rectF.bottom);
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }
}
