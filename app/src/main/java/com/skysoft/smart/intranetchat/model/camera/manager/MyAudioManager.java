/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/28
 * Description: PG1-Smart Team-CT PT-31 [MM] Video Recording Requirement
 ***/
package com.skysoft.smart.intranetchat.model.camera.manager;

import android.app.Activity;

import com.skysoft.smart.intranetchat.model.camera.audiorecord.MyAudioRecord;
import com.skysoft.smart.intranetchat.model.camera.mediacodec.AudioEncoder;
import com.skysoft.smart.intranetchat.model.camera.mediamuxer.MyMediaMuxer;

public class MyAudioManager {
    private AudioEncoder mAudioEncoder;
    private MyAudioRecord mAudioRecord;

    public MyAudioManager(MyMediaMuxer mediaMuxer) {
        this();
        mAudioEncoder = new AudioEncoder(mAudioRecord, mediaMuxer);
    }

    public MyAudioManager() {
        mAudioRecord = new MyAudioRecord();
        mAudioEncoder = new AudioEncoder(mAudioRecord, null);
    }


    public void startAudioRecord() {
        mAudioEncoder.startEncoder();
        mAudioRecord.startRecord(mAudioEncoder);
    }

    public void startAudioRecordOnly(Activity activity) {
        mAudioEncoder.startEncoder();
        mAudioRecord.startRecordOnly(activity,mAudioEncoder);
    }

    public void vioceCall(){
        mAudioRecord.vioceCall();
    }

    public void stop() {
        mAudioRecord.stopRecord();
        if (mAudioEncoder != null) {
            mAudioEncoder.stopEncoder();
            mAudioEncoder.release(true);
        }
    }
    public void stopAndNotSend(){
        mAudioRecord.stopRecord();
        if (mAudioEncoder != null) {
            mAudioEncoder.stopEncoder();
            mAudioEncoder.release(false);
        }
    }
}
