/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/11/12
 * Description: PG1-Smart Team-CT PT-72 [MM] Video Communication Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.videocall;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

public class AudioTrackThread extends Thread {

    private static final String TAG = "jesse: " + AudioTrackThread.class.getSimpleName() + " ";
    private int mFrequency = 44100;
    private int mChannel = AudioFormat.CHANNEL_OUT_MONO;
    private int mSampBit = AudioFormat.ENCODING_PCM_16BIT;
    private int mBufSize;
    private int mCurrentSize;
    private AudioTrack mAudioTrack;
    private ADecoder mADecoder;
    private boolean mState = false;
    private byte[] bytes;

    public AudioTrackThread(ADecoder mADecoder ) {
        this.mADecoder = mADecoder;
        mBufSize = AudioTrack.getMinBufferSize(mFrequency, mChannel, mSampBit) * 5;
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mFrequency, mChannel, mSampBit, mBufSize, AudioTrack.MODE_STREAM);
        mAudioTrack.play();
    }

    @Override
    public void run() {
        super.run();
        while (!mState) {
            bytes = mADecoder.pollFrameFromEncoder();
            byte[] temp = bytes;
            bytes = null;
            if (temp == null) continue;
            mCurrentSize += temp.length;
            if (mCurrentSize >= mBufSize) {
                mAudioTrack.flush();
            }
            mAudioTrack.write(temp, 0, temp.length);
            TLog.d(TAG, "run: AudioTrack write " + temp.length);
        }
        mAudioTrack.stop();
        mAudioTrack.release();
    }

    public void stopTrck() {
        mState = true;
    }

    public void setMute(boolean isMute) {
        if (isMute) mAudioTrack.setVolume(0);
        else mAudioTrack.setVolume(1);
    }
}
