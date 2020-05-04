/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/15
 * Description: PG1-Smart Team-CTPT-21 [MM] Taking Picture Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.entity;

public class CameraId {
    private String frontCameraId;
    private String backCameraId;


    public String getFrontCameraId() {
        return frontCameraId;
    }

    public void setFrontCameraId(String frontCameraId) {
        this.frontCameraId = frontCameraId;
    }

    public String getBackCameraId() {
        return backCameraId;
    }

    public void setBackCameraId(String backCameraId) {
        this.backCameraId = backCameraId;
    }
}
