/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/11/12
 * Description: PG1-Smart Team-CT PT-72 [MM] Video Communication Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.videocall;

import android.app.Activity;
import android.view.Surface;

public class Manager {
    private Sender mSender;
    private Receiver mReceiver;
    private AudioTrackThread mTrack;
    private VCEncoder mVCEncoder;
    private VCDecoder mVCDecoder;
    private RecordThread mRecord;
    private AEncoder mAEncoder;
    private ADecoder mADecoder;

    public void startEncoder() {
        mSender = new Sender();
        mAEncoder = new AEncoder();
        mVCEncoder.startVCEncoder();
        mAEncoder.startEncoder();
        mRecord = new RecordThread(mAEncoder);
        mRecord.start();
    }

    public void startDecoder(Surface surface, int width, int height, Activity activity) {
        mVCDecoder = new VCDecoder(surface, width, height);
        mADecoder = new ADecoder();
        mVCDecoder.startDecoder();
        mADecoder.startDecoder();
        mTrack = new AudioTrackThread(mADecoder);
        mTrack.start();
        mReceiver = new Receiver(mADecoder, mVCDecoder, activity);
    }

    public void stop() {
        if (mReceiver != null) {
            mReceiver.stop();
            mReceiver = null;
        }
        if (mSender != null) {
            mSender.stop();
            mSender = null;
        }
        if (mVCDecoder != null) mVCDecoder.stopVCDecoder();
        if (mVCEncoder != null) mVCEncoder.stopVCEncoder();
        if (mADecoder != null) mADecoder.stopDecoder();
        if (mAEncoder != null) mAEncoder.stopEncoder();
        if (mRecord != null) mRecord.stopRecord();
        if (mTrack != null) mTrack.stopTrck();
    }

    public VCEncoder initVCEncoder(int width, int height) {
        mVCEncoder = new VCEncoder(width, height);
        return mVCEncoder;
    }

    public void setMute(boolean isMute) {
        mTrack.setMute(isMute);
    }

}
