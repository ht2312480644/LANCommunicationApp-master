/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/15
 * Description: PG1-Smart Team-CTPT-21 [MM] Taking Picture Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.util;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.camera2.CameraCharacteristics;

public class CoordinateTransformer {

        private final Matrix mPreviewToCameraTransform;
        private RectF mDriverRectF;

        public CoordinateTransformer(CameraCharacteristics chr, RectF previewRect) {
            if (!hasNonZeroArea(previewRect)) {
                throw new IllegalArgumentException("previewRect");
            }
            Rect rect = chr.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            Integer sensorOrientation = chr.get(CameraCharacteristics.SENSOR_ORIENTATION);
            int rotation = sensorOrientation == null ? 90 : sensorOrientation;
            mDriverRectF = new RectF(rect);
            Integer face = chr.get(CameraCharacteristics.LENS_FACING);
            boolean mirrorX = face != null && face == CameraCharacteristics.LENS_FACING_FRONT;
            mPreviewToCameraTransform = previewToCameraTransform(mirrorX, rotation, previewRect);
        }

        public RectF toCameraSpace(RectF source) {
            RectF result = new RectF();
            mPreviewToCameraTransform.mapRect(result, source);
            return result;
        }

        private Matrix previewToCameraTransform(boolean mirrorX, int sensorOrientation,
                                                RectF previewRect) {
            Matrix transform = new Matrix();
            transform.setScale(mirrorX ? -1 : 1, 1);
            transform.postRotate(-sensorOrientation);
            transform.mapRect(previewRect);
            Matrix fill = new Matrix();
            fill.setRectToRect(previewRect, mDriverRectF, Matrix.ScaleToFit.FILL);
            transform.setConcat(fill, transform);
            return transform;
        }

        private boolean hasNonZeroArea(RectF rect) {
            return rect.width() != 0 && rect.height() != 0;
        }
    }

