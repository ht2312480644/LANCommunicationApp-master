/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/11/4
 * Description: PG1-Smart Team-CT PT-68 [MM] Voice Communication Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.audioTrack;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.app.IntranetChatApplication;

public class VioceCallPlay extends Thread {
    private static final String TAG = "LCH_DEBUG: " + VioceCallPlay.class.getSimpleName() + " ";

    private int mFrequency = 44100;
    private int mChannel = AudioFormat.CHANNEL_OUT_MONO;
    private int mSampBit = AudioFormat.ENCODING_PCM_16BIT;
    private int mBufSize;
    private int mCurrentSize;
    private AudioTrack mAudioTrack;
    private boolean isStop = false;

    public VioceCallPlay() {
        init();
    }

    private void init() {
        mBufSize = AudioTrack.getMinBufferSize(mFrequency, mChannel, mSampBit) * 5;
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mFrequency, mChannel, mSampBit, mBufSize, AudioTrack.MODE_STREAM);
        mAudioTrack.play();
        TLog.d(TAG, "init:  play");
    }

    @Override
    public void run() {
        super.run();
        while (!isStop) {
            byte[] bytes = IntranetChatApplication.getmDatasQueue().poll();
            if (bytes == null) continue;
            play(bytes);
        }
        release();
    }

    public void stopPlay() {
        isStop = true;
    }

    private void play(byte[] data) {
        if (data == null || mAudioTrack == null) {
            return;
        }
        mCurrentSize += data.length;
        if (mCurrentSize >= mBufSize) {
            mAudioTrack.flush();
            TLog.d(TAG, "play: flush");
        }
        mAudioTrack.write(data, 0, data.length);
        TLog.d(TAG, "play: write");
    }

    private void release() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
            mAudioTrack = null;
            TLog.d(TAG, "release: track");
        }
    }
}
