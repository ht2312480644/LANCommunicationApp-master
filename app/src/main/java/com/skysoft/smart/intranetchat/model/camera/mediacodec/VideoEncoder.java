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

import com.skysoft.smart.intranetchat.model.camera.mediamuxer.MyMediaMuxer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoEncoder {
    private static final String TAG = "debug " + VideoEncoder.class.getSimpleName() + " ";

    private MediaCodec mMediaCodec;
    private MediaFormat mMediaFormat;
    private MyMediaMuxer mMediaMuxer;
    private Handler mVideoEncoderHandler;
    private HandlerThread mVideoEncoderHandlerThread = new HandlerThread("VideoEncoder");

    @SuppressLint("NewApi")
    public VideoEncoder(String mimeType, MyMediaMuxer mediaMuxer) {
        try {
            mMediaCodec = MediaCodec.createEncoderByType(mimeType);
        } catch (IOException e) {
            TLog.e(TAG, Log.getStackTraceString(e));
            mMediaCodec = null;
            return;
        }
        mVideoEncoderHandlerThread.start();
        mVideoEncoderHandler = new Handler(mVideoEncoderHandlerThread.getLooper());
        mMediaFormat = MediaFormat.createVideoFormat(mimeType, 1280, 720);
        mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, 1920 * 1080);
        mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 25);
        mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5);
        mMediaCodec.setCallback(mCallback, mVideoEncoderHandler);
        mMediaCodec.configure(mMediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mMediaMuxer = mediaMuxer;
    }

    private MediaCodec.Callback mCallback = new MediaCodec.Callback() {
        @Override
        public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int id) {
        }

        @Override
        public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int id, @NonNull MediaCodec.BufferInfo bufferInfo) {
            try {

                ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(id);
                MediaFormat outputFormat = mediaCodec.getOutputFormat();
                if (!mMediaMuxer.isStart()) {
                    if (mMediaMuxer.papre(outputFormat, true)) {
                        mMediaMuxer.startMuxer();
                    }
                } else {
                    if (outputBuffer != null && bufferInfo.size > 0) {
                        mMediaMuxer.writeData(outputBuffer, bufferInfo, true);
                    }
                }
                mediaCodec.releaseOutputBuffer(id, false);
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {
            TLog.d(TAG, " MediaCodec.Callback onError");
        }

        @Override
        public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {
            TLog.d(TAG, "MediaCodec.Callback onOutputFormatChanged");
        }
    };


    public MediaCodec getMediaCodec() {
        return mMediaCodec;
    }

    @SuppressLint("NewApi")
    public void startEncoder() {
        if (mMediaCodec != null) {
            mMediaCodec.start();
        } else {
            throw new IllegalArgumentException("startEncoder failed,is the MediaCodec has been init correct?");
        }
    }

    public void stopEncoder() {
        if (mMediaCodec != null) {
            mMediaCodec.stop();
            mMediaCodec.setCallback(null);
            TLog.d(TAG, "videoEncoder stop");
        }
    }

    public void release() {
        if (mMediaCodec != null) {
            mMediaCodec.release();
            TLog.d(TAG, "videoEncoder release");
        }
    }
}
