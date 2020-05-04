/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/11/8
 * Description: PG1-Smart Team-CT PT-72 [MM] Video Communication Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.videocall;

import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.app.impl.OnReceiveCallBean;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

public class VideoDateThread extends Thread {

    private static final String TAG = "debug: " + VideoDateThread.class.getSimpleName() + " ";
    private boolean isStop = false;
    private int length = 0;
    private int redLength = 0;
    private int leftLength = 0;
    private int rightLength = 0;

    private byte[] temp;
    private byte[] data;
    private byte[] rightData;
    private ByteBuffer buffer;
    private VCDecoder mDecoder;
    private static final ArrayBlockingQueue<byte[]> mInputDatasQueue = new ArrayBlockingQueue<byte[]>(1);

    public VideoDateThread(VCDecoder mDecoder) {
        this.mDecoder = mDecoder;
        IntranetChatApplication.getsCallback().setOnReceiveCallBean(onReceiveCallBean);
    }

    private OnReceiveCallBean onReceiveCallBean = new OnReceiveCallBean() {
        @Override
        public void onReceiveVoiceCallData(byte[] data) {
            try {
                mInputDatasQueue.put(data);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void run() {
        super.run();
        while (!isStop) {
            try {
                if (mDecoder.isStop()) {
                    isStop = true;
                    mInputDatasQueue.clear();
                }
                try {
                    temp = mInputDatasQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (temp == null) continue;
                leftLength = temp.length;
                while (leftLength > 0) {
                    if (redLength == length) {
                        if (rightLength != 0) {
                            byte[] temp2 = temp;
                            temp = new byte[temp2.length + rightLength];
                            for (int i = 0; i < rightLength; i++) {
                                temp[i] = rightData[i];
                            }
                            for (int i = rightLength; i < temp.length; i++) {
                                temp[i] = temp2[i - rightLength];
                            }
                            rightLength = 0;
                        }

                        length = getLength(temp);
                        if (length == -1) {
                            rightData = temp;
                            length = redLength;
                            break;
                        }
                        buffer = ByteBuffer.allocate(length);
                        redLength = 0;
                        leftLength -= 6;
                        byte[] temp2 = temp;
                        temp = new byte[temp2.length - 6];
                        for (int i = 0; i < temp.length; i++) {
                            temp[i] = temp2[i + 6];
                        }
                    }
                    if (length - redLength <= leftLength) {
                        buffer.put(temp, 0, length - redLength);
                        byte[] temp2 = temp;
                        temp = new byte[temp2.length - (length - redLength)];
                        for (int i = 0; i < temp.length; i++) {
                            temp[i] = temp2[i + (length - redLength)];
                        }
                        leftLength -= (length - redLength);
                        redLength = length;
                        data = new byte[length];
                        buffer.flip();
                        buffer.get(data);
                        mDecoder.inputFrameToEncoder(data);
                    } else {
                        if (leftLength < 0) {
                            break;
                        }
                        buffer.put(temp, 0, temp.length);
                        redLength += temp.length;
                        leftLength -= temp.length;
                    }
                }
            } catch (IllegalArgumentException e) {
                TLog.d(TAG, "ByteBuffer.allocate: IllegalArgumentException");
                buffer.clear();
            }
        }
    }

    private int getLength(byte[] data) {

        if (data.length < 6) {
            rightLength = data.length;
            return -1;
        }
        int sum = 0;
        for (int i = 0; i < 6; i++) {
            sum += (data[i] - 48) * (100000 / Math.pow(10, i));
        }
        return sum;
    }

}
