/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/11/12
 * Description: PG1-Smart Team-CT PT-72 [MM] Video Communication Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.videocall;

import android.app.Activity;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.tools.toastutil.ToastUtil;

import java.nio.ByteBuffer;

public class Receiver {

    private static final String TAG = "jesse: " + Receiver.class.getSimpleName() + " ";

    private boolean isStop = false;
    private int length = 0;
    private int redLength = 0;
    private int leftLength = 0;
    private int rightLength = 0;
    private int tag = -2;
    private byte[] temp;
    private byte[] data;
    private byte[] rightData;
    private ByteBuffer buffer;
    private Operation mOperation;
    private ADecoder mADecoder;
    private VCDecoder mVCDecoder;
    private Activity mActivity;
    private boolean isError = false;

    public Receiver(ADecoder mADecoder, VCDecoder mVCDecoder, Activity activity) {
        this.mADecoder = mADecoder;
        this.mVCDecoder = mVCDecoder;
        mOperation = new Operation();
        mOperation.start();
        mActivity = activity;
    }

    public void stop() {
        isStop = true;
        if (isError) {
            ToastUtil.toast(mActivity, String.valueOf(R.string.Receiver_stop_toast_text));
        }
    }

    private int count = 0;
    private int count2 = 0;

    private class Operation extends Thread {
        @Override
        public void run() {
            super.run();
            while (!isStop) {
                temp = IntranetChatApplication.getmDatasQueue().poll();
                if (temp == null) continue;
                count++;
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
                        if (temp.length > 7 && tag == 0) {
                            TLog.d(TAG, "first video 2: " + temp[0] + " " + temp[1] + " " + temp[2] + " " + temp[3] + " " + temp[4] + " " + temp[5] + " " + temp[6] + "  count1 " + count + "  count2 " + count2 + " length " + length);
                        }
                        if (length == -1) {
                            rightData = temp;
                            length = redLength;
                            break;
                        }
                        try {
                            buffer = ByteBuffer.allocate(length);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                            isError = true;
                            TLog.d(TAG, "ByteBuffer.allocate(length)  " + temp[0] + " " + temp[1] + " " + temp[2] + " " + temp[3] + " " + temp[4] + " " + temp[5] + " " + temp[6] + "  count1 " + count + "  count2 " + count2 + " length " + length);
                            mActivity.finish();
                            return;
                        }
                        redLength = 0;
                        leftLength -= 7;
                        byte[] temp2 = temp;
                        temp = new byte[temp2.length - 7];
                        for (int i = 0; i < temp.length; i++) {
                            temp[i] = temp2[i + 7];
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
                        if (tag == 0) {
                            count2++;
                            mVCDecoder.inputFrameToEncoder(data);
                            if (count2 == 1) {
                                TLog.d(TAG, "first video 3: " + data[0] + " " + data[1] + " " + data[2] + " " + data[3] + " " + data[4] + " " + data[5] + " " + data[6] + "  count1 " + count + "  count2 " + count2 + " length " + data.length);

                            }else {
                                TLog.d(TAG, "run: first video 4: "+data.length+ " count "+count2);
                            }
                        } else {
                            mADecoder.inputFrameToEncoder(data);
                        }
                    } else {
                        if (leftLength < 0) {
                            break;
                        }
                        buffer.put(temp, 0, temp.length);
                        redLength += temp.length;
                        leftLength -= temp.length;
                    }
                }
            }
            IntranetChatApplication.getmDatasQueue().clear();
        }
    }


    private int getLength(byte[] data1) {
        if (data1.length < 7) {
            rightLength = data1.length;
            return -1;
        }
        int sum = 0;
        for (int i = 1; i < 7; i++) {
            sum += (data1[i] - 48) * (100000 / Math.pow(10, i - 1));
        }
        tag = data1[0] - 48;
        return sum;
    }

}
