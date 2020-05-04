/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/11/8
 * Description: PG1-Smart Team-CT PT-72 [MM] Video Communication Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.videocall;

import android.annotation.SuppressLint;
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

public class VCEncoder {
    private static final String TAG = "jesse_debug: " + VCEncoder.class.getSimpleName() + " ";
    private final static int CONFIGURE_FLAG_ENCODE = MediaCodec.CONFIGURE_FLAG_ENCODE;

    private MediaCodec mMediaCodec;
    private MediaFormat mMediaFormat;
    private Handler mVCEncoderHandler;
    private HandlerThread mVCEncoderHandlerThread = new HandlerThread("VCEncoder");

    @SuppressLint("NewApi")
    public VCEncoder(int width, int height) {
        try {
            mMediaCodec = MediaCodec.createEncoderByType("video/avc");
        } catch (IOException e) {
            TLog.e(TAG, Log.getStackTraceString(e));
            mMediaCodec = null;
            return;
        }

        mVCEncoderHandlerThread.start();
        mVCEncoderHandler = new Handler(mVCEncoderHandlerThread.getLooper());
        mMediaFormat = MediaFormat.createVideoFormat("video/avc", 1280, 720);
        mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1280 * 720);
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        mMediaCodec.setCallback(mCallback, mVCEncoderHandler);
        mMediaCodec.configure(mMediaFormat, null, null, CONFIGURE_FLAG_ENCODE);
    }

    private MediaCodec.Callback mCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int id) {

        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int id, @NonNull MediaCodec.BufferInfo bufferInfo) {
            try {

                ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(id);
                if (outputBuffer != null && bufferInfo.size > 0) {

                    int outBitsSize = bufferInfo.size;
                    int outPacketSize = outBitsSize + 7;
                    outputBuffer.position(bufferInfo.offset);
                    outputBuffer.limit(bufferInfo.offset + outBitsSize);
                    byte[] outData = new byte[outPacketSize];
                    addTag(outData, outBitsSize);
                    outputBuffer.get(outData, 7, outBitsSize);
                    Sender.mInputDatasQueue.offer(outData);
                    TLog.d(TAG, "VCEncoder: send data" + outData.length);
                }
                mMediaCodec.releaseOutputBuffer(id, true);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {
        }
    };

    private void addTag(byte[] bytes, int length) {
        String aS = String.valueOf(length);
        char[] asC = aS.toCharArray();
        int count = 6;
        for (int i = asC.length - 1; i >= 0; i--) {
            bytes[count] = (byte) asC[i];
            count--;

        }
        for (int i = 0; i < 7 - asC.length; i++) {
            bytes[i] = 48;
        }
    }

    public MediaCodec getMediaCodec() {
        return mMediaCodec;
    }

    @SuppressLint("NewApi")
    public void startVCEncoder() {
        if (mMediaCodec != null) {
            mMediaCodec.start();
            TLog.d(TAG, "startVCEncoder: ");
        }
    }

    public void stopVCEncoder() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.setCallback(null);
        }
        releaseVCEncoder();
    }

    public void releaseVCEncoder() {
        if (mMediaCodec != null) {
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }
}
