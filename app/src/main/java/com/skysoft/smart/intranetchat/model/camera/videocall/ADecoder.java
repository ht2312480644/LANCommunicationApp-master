/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/11/12
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

public class ADecoder {
    private static final String TAG = "jesse: " + ADecoder.class.getSimpleName() + " ";
    private   ArrayBlockingQueue<byte[]> mInputDatasQueue = new ArrayBlockingQueue<byte[]>(16);
    private   ArrayBlockingQueue<byte[]> mOutputDatasQueue = new ArrayBlockingQueue<byte[]>(16);
    private static final int SAMPLE_RATE_INHZ = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private MediaCodec mMediaCodec;
    private MediaFormat mMediaFormat;
    private Handler mAudioDecoderHandler;
    private HandlerThread mAudioDecoderHandlerThread = new HandlerThread("ADecoder");

    @SuppressLint("NewApi")
    public ADecoder() {
        try {
            mMediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
        } catch (IOException e) {
            TLog.e(TAG, Log.getStackTraceString(e));
            mMediaCodec = null;
            return;
        }

        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_INHZ, CHANNEL_CONFIG, AUDIO_FORMAT);
        mAudioDecoderHandlerThread.start();
        mAudioDecoderHandler = new Handler(mAudioDecoderHandlerThread.getLooper());
        mMediaFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1);
        mMediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 1);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 96000);
        mMediaFormat.setInteger(MediaFormat.KEY_IS_ADTS, 1);
        mMediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, minBufferSize);
        mMediaFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
        mMediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

        byte[] data = new byte[]{(byte) 0x12, (byte) 0x08};
        ByteBuffer csd_0 = ByteBuffer.wrap(data);
        mMediaFormat.setByteBuffer("csd-0", csd_0);
        mMediaCodec.setCallback(mCallback, mAudioDecoderHandler);
        mMediaCodec.configure(mMediaFormat, null, null, 0);
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
                    TLog.d(TAG, "onInputBufferAvailable: decoder--------------- " + length);
                }
                mediaCodec.queueInputBuffer(id, 0, length, 0, 0);
            }catch (IllegalStateException e){
                e.printStackTrace();
            }
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int id, @NonNull MediaCodec.BufferInfo bufferInfo) {
            try {

                ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(id);
                if (outputBuffer != null && bufferInfo.size > 0) {
                    byte[] buffer = new byte[outputBuffer.remaining()];
                    outputBuffer.get(buffer);
                    mOutputDatasQueue.offer(buffer);
                    TLog.d(TAG, "ADcoder: out" + buffer.length);
                }
                mediaCodec.releaseOutputBuffer(id, false);
            }catch (IllegalStateException e){
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


    @SuppressLint("NewApi")
    public void startDecoder() {
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

    public void stopDecoder() {
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
