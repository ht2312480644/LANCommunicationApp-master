/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/28
 * Description: PG1-Smart Team-CT PT-31 [MM] Video Recording Requirement
 ***/
package com.skysoft.smart.intranetchat.model.camera.mediamuxer;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.model.camera.entity.EventMessage;
import com.skysoft.smart.intranetchat.model.camera.util.FileUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.skysoft.smart.intranetchat.ui.activity.camera.VideoActivity.TAKE_VIDEO_URL;

public class MyMediaMuxer {
    private static final String TAG = "debug " + MyMediaMuxer.class.getSimpleName() + " ";
    private MediaMuxer mMediaMuxer;
    private boolean isStart = false;
    private boolean audioReady = false;
    private boolean videoReady = false;
    private int track;
    private int videoTrack;
    private int audioTrack;
    private long nanoTime;
    private String mVideoUrl;


    public MyMediaMuxer() {
        init();
    }

    private void init() {
        try {
            mVideoUrl = FileUtil.getVideoPath() + System.currentTimeMillis() + ".mp4";
            mMediaMuxer = new MediaMuxer(mVideoUrl, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            mMediaMuxer.setOrientationHint(90);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean papre(MediaFormat format, boolean isVideo) {
        if (isVideo && !videoReady) {
            track = mMediaMuxer.addTrack(format);
            videoTrack = track;
            videoReady = true;
            TLog.d(TAG, " video" + isVideo + " videotrack " + track);
        } else if (!isVideo && !audioReady) {
            track = mMediaMuxer.addTrack(format);
            audioTrack = track;
            audioReady = true;
            TLog.d(TAG, " video" + isVideo + " audioTrack " + track);
        }

        return audioReady && videoReady;
    }

    public void startMuxer() {
        if (audioTrack != -1 && videoTrack != -1) {
            mMediaMuxer.start();
            nanoTime = System.nanoTime();
            TLog.d(TAG, "muxer start-----");
            isStart = true;
        }
    }

    public void writeData(ByteBuffer buffer, MediaCodec.BufferInfo info, boolean isVideo) {

        if (audioTrack == -1 || videoTrack == -1) {
            TLog.e(TAG, "音频轨和视频轨没有添加");
            return;
        }
        if (isStart) {
            if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {

            } else if (info.size != 0) {
                info.presentationTimeUs = (System.nanoTime() - nanoTime) / 1000;
                mMediaMuxer.writeSampleData(isVideo ? videoTrack : audioTrack, buffer, info);
                TLog.d(TAG, "write......" + info.size + "  " + isVideo + " videoTrack " + videoTrack + " audioTrack" + audioTrack);

            }
        }
    }

    public boolean isStart() {
        return isStart;
    }

    public void stopMuxer(boolean isSend) {
        if (mMediaMuxer != null) {
            if (isSend) EventBus.getDefault().post(new EventMessage(mVideoUrl,0, TAKE_VIDEO_URL));
            isStart = false;
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mMediaMuxer = null;
            TLog.d(TAG, "Muxer release");
        }
    }
}
