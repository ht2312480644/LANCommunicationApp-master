/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/11/12
 * Description: PG1-Smart Team-CT PT-72 [MM] Video Communication Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.videocall;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import static android.media.AudioRecord.RECORDSTATE_RECORDING;
import static android.media.AudioRecord.RECORDSTATE_STOPPED;

public class RecordThread extends Thread {

    private static final String TAG = "jesse: " + RecordThread.class.getSimpleName() + " ";
    private static final int SAMPLE_RATE_INHZ = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord mAudioRecord;
    private AEncoder mAEncoder;
    private int mBufferSize;
    private int state;

    public RecordThread(AEncoder mAEncoder) {
        this.mAEncoder = mAEncoder;
        mBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT, mBufferSize);
        mAudioRecord.startRecording();
        TLog.d(TAG, "RecordThread: start");
        state = mAudioRecord.getRecordingState();
    }

    @Override
    public void run() {
        super.run();
        byte[] data = new byte[mBufferSize];
        while (state == RECORDSTATE_RECORDING) {
            int read = mAudioRecord.read(data, 0, mBufferSize);
            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                mAEncoder.inputFrameToEncoder(data);
                TLog.d(TAG, "run: record " + data.length);
            }
        }
        mAudioRecord.stop();
        mAudioRecord.release();
    }

    public void stopRecord() {
        state = RECORDSTATE_STOPPED;
    }
}
