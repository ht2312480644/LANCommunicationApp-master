/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/11/11
 * Description: PG1-Smart Team-CT PT-72 [MM] Video Communication Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.videocall;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

public class AEncoder {

    private static final String TAG = "jesse: " + AEncoder.class.getSimpleName() + " ";
    private   ArrayBlockingQueue<byte[]> mInputDatasQueue = new ArrayBlockingQueue<byte[]>(8);
    private   ArrayBlockingQueue<byte[]> mOutputDatasQueue = new ArrayBlockingQueue<byte[]>(8);
    private static final int SAMPLE_RATE_INHZ = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private MediaCodec mMediaCodec;
    private MediaFormat mMediaFormat;
    private Handler mAudioEncoderHandler;
    private HandlerThread mAudioEncoderHandlerThread = new HandlerThread("AEncoder");

    @SuppressLint("NewApi")
    public AEncoder() {
        try {
            mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
        } catch (IOException e) {
            TLog.e(TAG, Log.getStackTraceString(e));
            mMediaCodec = null;
            return;
        }
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        mAudioEncoderHandlerThread.start();
        mAudioEncoderHandler = new Handler(mAudioEncoderHandlerThread.getLooper());
        mMediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1);
        mMediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
        mMediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, minBufferSize);
        mMediaCodec.setCallback(mCallback, mAudioEncoderHandler);
        mMediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    private MediaCodec.Callback mCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int id) {
            try {

                ByteBuffer inputBuffer = mediaCodec.getInputBuffer(id);
                inputBuffer.clear();
                byte[] dataSources = mInputDatasQueue.poll();
                int length = 0;
                if (dataSources != null) {
                    inputBuffer.put(dataSources);
                    length = dataSources.length;
                }
                mediaCodec.queueInputBuffer(id, 0, length, 0, 0);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int id, @NonNull MediaCodec.BufferInfo bufferInfo) {
            try {

                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(id);
                if (outputBuffer != null && bufferInfo.size > 0) {
                    int outBitsSize = bufferInfo.size;
                    int outPacketSize = outBitsSize + 7;
                    outputBuffer.position(bufferInfo.offset);
                    outputBuffer.limit(bufferInfo.offset + outBitsSize);
                    byte[] outData = new byte[outPacketSize];
                    addADTStoPacket(outData, outPacketSize);
                    outputBuffer.get(outData, 7, outBitsSize);

                    byte[] temp = outData;
                    outData = new byte[temp.length + 7];
                    for (int i = 0; i < temp.length; i++) {
                        outData[i + 7] = temp[i];
                    }
                    addTag(outData, temp.length);
                    Sender.mInputDatasQueue.offer(outData);
                    TLog.d(TAG, "AEncoder: send " + outData.length);
                }
                mediaCodec.releaseOutputBuffer(id, false);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
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

    private void addTag(byte[] bytes, int length) {

        String aS = String.valueOf(length);
        char[] asC = aS.toCharArray();
        int count = 6;
        for (int i = asC.length - 1; i >= 0; i--) {
            bytes[count] = (byte) asC[i];
            count--;

        }
        for (int i = 1; i < 7 - asC.length; i++) {
            bytes[i] = 48;
        }
        bytes[0] = 49;
    }


    @SuppressLint("NewApi")
    public void startEncoder() {
        if (mMediaCodec != null) {
            mMediaCodec.start();
        } else {
            throw new IllegalArgumentException("startEncoder failed");
        }
    }

    public void inputFrameToEncoder(byte[] needEncodeData) {
        mInputDatasQueue.offer(needEncodeData);
    }

    public byte[] pollFrameFromEncoder() {
        return mOutputDatasQueue.poll();
    }

    public void stopEncoder() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.setCallback(null);
            TLog.d(TAG, "AEncor stop");
        }
        release();
    }

    public void release() {
        if (mMediaCodec != null) {
            mInputDatasQueue.clear();
            mOutputDatasQueue.clear();
            mMediaCodec.release();
            mMediaCodec = null;
            TLog.d(TAG, "AEncor release");
        }
    }
}
