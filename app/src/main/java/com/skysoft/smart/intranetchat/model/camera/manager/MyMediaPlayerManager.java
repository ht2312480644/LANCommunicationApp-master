/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/11/4
 * Description: PG1-Smart Team-CT PT-68 [MM] Voice Communication Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.manager;

import android.media.MediaPlayer;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import java.io.IOException;

public class MyMediaPlayerManager {
    private static MediaPlayer mMediaPlayer;

    private volatile static MyMediaPlayerManager sInstance;

    private MyMediaPlayerManager() {

    }

    public static MyMediaPlayerManager getsInstance() {
        if (sInstance == null) {
            synchronized (MyMediaPlayerManager.class) {
                if (sInstance == null) {
                    sInstance = new MyMediaPlayerManager();
                    sInstance.mMediaPlayer = new MediaPlayer();
                }
            }
        }
        return sInstance;
    }

    public void play(String url) {
        if (sInstance.mMediaPlayer == null){
            sInstance.mMediaPlayer = new MediaPlayer();
        }
        init(url);
    }

    private void init(String url) {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(mp -> mMediaPlayer.start());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            TLog.d("MyMediaPlayerManager", "stop: mMediaPlayer");
            mMediaPlayer = null;
        }
    }

    public boolean isPlaying(){
        if (sInstance.mMediaPlayer == null){
            sInstance.mMediaPlayer = new MediaPlayer();
        }
        return sInstance.mMediaPlayer.isPlaying();
    }
}
