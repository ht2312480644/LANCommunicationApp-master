/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/28
 * Description: PG1-Smart Team-CT PT-31 [MM] Video Recording Requirement
 ***/
package com.skysoft.smart.intranetchat.model.camera.mediacodec;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import androidx.annotation.NonNull;

import com.skysoft.smart.intranetchat.model.camera.audiorecord.MyAudioRecord;
import com.skysoft.smart.intranetchat.model.camera.entity.EventMessage;
import com.skysoft.smart.intranetchat.model.camera.mediamuxer.MyMediaMuxer;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

public class AudioEncoder {
    private static final String TAG = "debug " + VideoEncoder.class.getSimpleName() + " ";

    private final static ArrayBlockingQueue<byte[]> mInputDatasQueue = new ArrayBlockingQueue<byte[]>(8);
    private MediaCodec mMediaCodec;
    private MediaFormat mMediaFormat;
    private MyMediaMuxer mMediaMuxer;
    private MyAudioRecord mAudioRecord;
    private Handler mAudioEncoderHandler;
    private HandlerThread mAudioEncoderHandlerThread = new HandlerThread("AudioEncoder");
    private File mFile;
    private FileOutputStream mOutput;
    private long mStartTime;
    private long mStopTime;

    @SuppressLint("NewApi")
    public AudioEncoder(MyAudioRecord audioRecord, MyMediaMuxer mediaMuxer) {
        try {
            mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
        } catch (IOException e) {
            TLog.e(TAG, Log.getStackTraceString(e));
            mMediaCodec = null;
            return;
        }
        if (mediaMuxer != null) {
            mMediaMuxer = mediaMuxer;
        }
        mAudioRecord = audioRecord;
        mAudioEncoderHandlerThread.start();
        mAudioEncoderHandler = new Handler(mAudioEncoderHandlerThread.getLooper());
        mMediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1);
        mMediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
        mMediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mAudioRecord.getBufferSize());
        mMediaCodec.setCallback(mCallback, mAudioEncoderHandler);
        mMediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    private MediaCodec.Callback mCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int id) {
            if (mAudioRecord.isStop()) {
                return;
            }
            ByteBuffer inputBuffer = mediaCodec.getInputBuffer(id);
            inputBuffer.clear();
            byte[] dataSources = mInputDatasQueue.poll();
            int length = 0;
            if (dataSources != null) {
                inputBuffer.put(dataSources);
                length = dataSources.length;
            }
            mediaCodec.queueInputBuffer(id, 0, length, 0, 0);
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int id, @NonNull MediaCodec.BufferInfo bufferInfo) {
            if (mMediaMuxer != null) {
                AudioWithVideo(mediaCodec, id, bufferInfo);
            } else {
                audioRecordOnly(mediaCodec, id, bufferInfo);
            }

            mediaCodec.releaseOutputBuffer(id, false);
        }

        @Override
        public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {
            TLog.d(TAG, " MediaCodec.Callback onError");
            e.printStackTrace();
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {
            TLog.d(TAG, "MediaCodec.Callback onOutputFormatChanged");
        }
    };

    /**
     * 只录音
     */
    private void audioRecordOnly(MediaCodec mediaCodec, int id, MediaCodec.BufferInfo bufferInfo) {
        if (mOutput == null) return;
        ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(id);
        if (outputBuffer != null && bufferInfo.size > 0) {
            int outBitsSize = bufferInfo.size;
            int outPacketSize = outBitsSize + 7;
            outputBuffer.position(bufferInfo.offset);
            outputBuffer.limit(bufferInfo.offset + outBitsSize);
            byte[] outData = new byte[outPacketSize];
            addADTStoPacket(outData, outPacketSize);
            outputBuffer.get(outData, 7, outBitsSize);
            try {
                mOutput.write(outData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; //AAC LC
        int freqIdx = 4; //44100
        int chanCfg = 2; //CPE
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }


    /**
     * 录视频同时录音
     */
    private void AudioWithVideo(MediaCodec mediaCodec, int id, MediaCodec.BufferInfo bufferInfo) {
        ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(id);
        MediaFormat outputFormat = mediaCodec.getOutputFormat(id);
        if (!mMediaMuxer.isStart()) {
            TLog.d(TAG, "audio no start");
            if (mMediaMuxer.papre(outputFormat, false)) {
                mMediaMuxer.startMuxer();
                TLog.d(TAG, "audio  start");
            }
        } else {
            if (outputBuffer != null && bufferInfo.size > 0) {
                mMediaMuxer.writeData(outputBuffer, bufferInfo, false);
            }
        }
    }

    @SuppressLint("NewApi")
    public void startEncoder() {
        if (mMediaCodec != null) {
            mMediaCodec.start();
        } else {
            throw new IllegalArgumentException("startEncoder failed");
        }
    }

    public void inputFrameToEncoder(byte[] needEncodeData, File file) {
        mInputDatasQueue.offer(needEncodeData);
        if (mFile == null && file != null) {
            mStartTime = System.currentTimeMillis();
            mFile = file;
            try {
                mOutput = new FileOutputStream(mFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopEncoder() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.setCallback(null);
            TLog.d(TAG, "AudioEncor stop");
        }
        if (mOutput != null) {
            try {
                mOutput.flush();
                mOutput.close();
                mOutput = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void release(boolean isSend) {
        if (mMediaCodec != null) {
            mInputDatasQueue.clear();
            mMediaCodec.release();
            TLog.d(TAG, "AudioEncor release");
        }

        if (mFile != null && isSend) {
            mStopTime = System.currentTimeMillis();
            int time = (int) ((mStopTime - mStartTime) / 1000)+1;
            EventBus.getDefault().post(new EventMessage(mFile.getAbsolutePath(), time, 3));
            TLog.d(TAG, "stopRecord: time" + time);
        }
    }
}
