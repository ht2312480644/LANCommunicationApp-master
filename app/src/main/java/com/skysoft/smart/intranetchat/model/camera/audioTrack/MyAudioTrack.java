/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/28
 * Description: PG1-Smart Team-CT PT-31 [MM] Video Recording Requirement
 ***/
package com.skysoft.smart.intranetchat.model.camera.audioTrack;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MyAudioTrack {
    private static final String TAG = "LCH_DEBUG: " + MyAudioTrack.class.getSimpleName() + " ";
    /**
     *  采样率
     */
    private int mFrequency = 44100;
    /**
     *  声道
     */
    private int mChannel = AudioFormat.CHANNEL_OUT_MONO;
    /**
     *  采样精度
     */
    private int mSampBit = AudioFormat.ENCODING_PCM_16BIT;
    private int mBufSize;
    private AudioTrack mAudioTrack;

    public MyAudioTrack() {
    }


    private void init() {
        if (mAudioTrack != null) {
            release();
        }
        mBufSize = getMinBufferSize();
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mFrequency, mChannel, mSampBit, mBufSize, AudioTrack.MODE_STREAM);
        mAudioTrack.play();
        TLog.d(TAG, "init: track play");
    }

    public void release() {
        if (mAudioTrack != null) {
            if (mAudioTrack.getState()==AudioTrack.STATE_INITIALIZED){
                mAudioTrack.flush();
                mAudioTrack.stop();
                mAudioTrack.release();
                mAudioTrack = null;
            }
            TLog.d(TAG, "release: track");
        }
    }

    public void playAudio(String url) {
        init();
        new Thread(() -> {
            File file = new File(url);
            FileInputStream fis;
            try {
                fis = new FileInputStream(file);
                byte[] buffer = new byte[mBufSize];
                while (fis.available() > 0) {
                    int readCount = fis.read(buffer);
                    if (readCount == -1) {
                        TLog.e(TAG, "没有更多数据可以读取了");
                        break;
                    }
                    mAudioTrack.write(buffer, 0, readCount);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private int getMinBufferSize() {
        return AudioTrack.getMinBufferSize(mFrequency, mChannel, mSampBit);
    }
}
