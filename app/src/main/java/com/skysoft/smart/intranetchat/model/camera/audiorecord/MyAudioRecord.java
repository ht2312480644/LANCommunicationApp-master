/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/28
 * Description: PG1-Smart Team-CT PT-31 [MM] Video Recording Requirement
 ***/
package com.skysoft.smart.intranetchat.model.camera.audiorecord;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.model.net_model.VoiceCall;
import com.skysoft.smart.intranetchat.model.camera.mediacodec.AudioEncoder;

import java.io.File;

public class MyAudioRecord {
    private static final String TAG = "debug " + MyAudioRecord.class.getSimpleName() + " ";

    /**
     * 采样率44100Hz,。
     */
    private static final int SAMPLE_RATE_INHZ = 44100;

    /**
     * 声道数
     */
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 返回的音频数据的格式
     */
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord mAudioRecord;
    private int mBufferSize;
    private boolean isRecording = false;
    private boolean isStop = false;
    private File mFile = null;

    public MyAudioRecord() {
        mBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT, mBufferSize);
        TLog.d(TAG, "MyAudioRecord: start");
    }

    public void startRecord(AudioEncoder audioEncoder) {
        byte data[] = new byte[mBufferSize];
        mAudioRecord.startRecording();
        isRecording = true;
        TLog.d(TAG, "MyAudioRocord start");
        new Thread(() -> {
            while (isRecording) {
                int read = mAudioRecord.read(data, 0, mBufferSize);
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    TLog.d(TAG, "MyAudioRocord red data");
                    audioEncoder.inputFrameToEncoder(data,null);
                }
            }
        }).start();
    }

    public void startRecordOnly(Activity activity, AudioEncoder audioEncoder) {
        mFile = new File(activity.getExternalFilesDir("audio"), System.currentTimeMillis() + ".aac");
        byte[] data = new byte[mBufferSize];
        mAudioRecord.startRecording();
        isRecording = true;
        new Thread(() -> {
            while (isRecording) {
                int read = mAudioRecord.read(data, 0, mBufferSize);
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    audioEncoder.inputFrameToEncoder(data,mFile);
                }
            }
        }).start();
    }

    public void vioceCall(){
        byte[] data = new byte[mBufferSize];
        mAudioRecord.startRecording();
        TLog.d(TAG, "MyAudioRocord start");
        isRecording = true;
        new Thread(() -> {
            while (isRecording) {
                int read = mAudioRecord.read(data, 0, mBufferSize);
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    VoiceCall.sendVoiceCallData(data);
                    TLog.d(TAG, "vioceCall send data");
                }
            }
        }).start();
    }

    public boolean isStop() {
        return isStop;
    }

    public int getBufferSize() {
        return mBufferSize;
    }

    public void stopRecord() {
        if (null != mAudioRecord) {
            isRecording = false;
            isStop = true;
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
        TLog.d(TAG, "MyAudioRocord stop");
    }

}
