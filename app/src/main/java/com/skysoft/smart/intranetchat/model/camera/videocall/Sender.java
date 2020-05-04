/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/11/12
 * Description: PG1-Smart Team-CT PT-72 [MM] Video Communication Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.videocall;

import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.model.net_model.VoiceCall;

import java.util.concurrent.ArrayBlockingQueue;

public class Sender {

    private static final String TAG = "jesse: " + Sender.class.getSimpleName() + " ";
    public final static ArrayBlockingQueue<byte[]> mInputDatasQueue = new ArrayBlockingQueue<byte[]>(1024);
    private Send mSend;
    private boolean isStop = false;

    public Sender() {
        mSend = new Send();
        mSend.start();
    }

    public void stop() {
        isStop = true;
    }

    private class Send extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isStop) {
                byte[] bytes = mInputDatasQueue.poll();
                if (bytes == null) continue;
                VoiceCall.sendVoiceCallData(bytes);
            }
            mInputDatasQueue.clear();
            TLog.d(TAG, "run: stop sender w");
        }
    }
}