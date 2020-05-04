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
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

public class VCDecoder {

    private static final String TAG = "LCH_debug: " + VCDecoder.class.getSimpleName() + " ";
    private final static int CONFIGURE_FLAG_DECODE = 0;

    private MediaCodec mMediaCodec;
    private MediaFormat mMediaFormat;
    private Surface mSurface;
    private boolean isStop = false;

    private Handler mVCDecoderHandler;
    private HandlerThread mVCDecoderHandlerThread = new HandlerThread("VCDecoder");
    private ArrayBlockingQueue<byte[]> mInputDatasQueue = new ArrayBlockingQueue<byte[]>(16);

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
                    TLog.d(TAG, "video call data VCDecoder length " + dataSources.length);
                }
                mediaCodec.queueInputBuffer(id, 0, length, 0, 0);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int id, @NonNull MediaCodec.BufferInfo bufferInfo) {
           try{

               ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(id);
               MediaFormat outputFormat = mMediaCodec.getOutputFormat(id);
               if (mMediaFormat == outputFormat && outputBuffer != null && bufferInfo.size > 0) {
                   byte[] buffer = new byte[outputBuffer.remaining()];
                   outputBuffer.get(buffer);
               }
               mMediaCodec.releaseOutputBuffer(id, true);
           }catch (IllegalStateException e){
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

    @SuppressLint("InlinedApi")
    public VCDecoder(Surface surface, int width, int height) {
        try {
            mMediaCodec = MediaCodec.createDecoderByType("video/avc");
        } catch (IOException e) {
            TLog.e(TAG, Log.getStackTraceString(e));
            mMediaCodec = null;
            return;
        }

        if (surface == null) {
            return;
        }
        this.mSurface = surface;

        mVCDecoderHandlerThread.start();
        mVCDecoderHandler = new Handler(mVCDecoderHandlerThread.getLooper());

        mMediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
        mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1920 * 1280);
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
        mMediaFormat.setInteger(MediaFormat.KEY_ROTATION, 270);
//        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
    }

    @SuppressLint("NewApi")
    public void startDecoder() {
        if (mMediaCodec != null && mSurface != null) {
            mMediaCodec.setCallback(mCallback, mVCDecoderHandler);
            mMediaCodec.configure(mMediaFormat, mSurface, null, CONFIGURE_FLAG_DECODE);
            mMediaCodec.start();
            TLog.d(TAG, "startDecoder: ");
        } else {
            throw new IllegalArgumentException("startDecoder failed");
        }
    }

    public void inputFrameToEncoder(byte[] needEncodeData) {
        boolean inputResult = mInputDatasQueue.offer(needEncodeData);
    }


    public void stopVCDecoder() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.setCallback(null);
        }
        releaseVCDecoder();
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean isStop) {
        this.isStop = isStop;
    }

    public void releaseVCDecoder() {
        if (mMediaCodec != null) {
            mInputDatasQueue.clear();
            mMediaCodec.release();
            mMediaCodec = null;
        }
    }
}
