/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication] Create a new process service with message sending and receiving.
 */
package com.skysoft.smart.intranetchat.model.network.receive;

import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.model.network.bean.AskResourceBean;
import com.skysoft.smart.intranetchat.model.network.bean.DataPacketBean;
import com.skysoft.smart.intranetchat.model.network.bean.ResourceManagerBean;
import com.skysoft.smart.intranetchat.model.network.bean.ResponseBean;
import com.skysoft.smart.intranetchat.model.network.call.VoiceCallThread;
import com.skysoft.smart.intranetchat.model.network.file.AskFileThread;
import com.skysoft.smart.intranetchat.model.network.manager.ResourceManager;
import com.skysoft.smart.intranetchat.model.network.manager.Sender;
import com.skysoft.smart.intranetchat.tools.GsonTools;

import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class OnReceiveResponseBean {
    private String TAG = OnReceiveResponseBean.class.getSimpleName();
    /*当从数据包获取到ResponseBean时的处理方法*/
    public void onReceiveResponseBean(DataPacketBean dataPacketBean, String host) {
        ResponseBean responseBean = (ResponseBean) GsonTools.formJson(dataPacketBean.getData(),ResponseBean.class);

        MonitorUdpReceivePortThread.broadcastReceive(Config.CODE_RESPONSE,dataPacketBean.getData(),host);

        switch (responseBean.getCode()){
            case Config.RESPONSE_RECEIVE_FILE_SUCCESS:
                /*对方向我请求*/
                onResponseReceiveFileSuccess(responseBean,host);
                break;
            case Config.RESPONSE_RECEIVE_FILE_FAILURE_AND_RESEND:
                //接收文件失败，并且请求重新发送
                break;
            case Config.RESPONSE_BUSY:
                //对方忙碌，100毫秒后重新请求文件
                onResponseBusy();
                break;
            case Config.RESPONSE_BUSY_TRANSPOND:
                //对方忙碌，让我向另一个人请求此文件,remark是另一个人的地址
                onResponseBusyTranspond(responseBean);
                break;
            case Config.RESPONSE_RECEIVE_FILE_FAILURE:
                //对方接受文件失败
                onResponseReceiveFileFailure(responseBean);
                break;
            case Config.RESPONSE_NOT_REQUEST_THIS_RESOURCE:
                //对方没有向我请求此文件
                break;
            case Config.RESPONSE_NOT_FOUND_FILE:
                //对方没有找到我请求的文件
                ResourceManager.getInstance().remove(responseBean.getIdentifier());
                break;
            case Config.RESPONSE_RESOURCE_OK:
                //对方表示该资源可以被下载
                AskFileThread askFileThread = new AskFileThread(host);
                askFileThread.start();
                break;
            case Config.RESPONSE_NOTHING_ASK:
                TLog.d(TAG, "onReceiveResponseBean: RESPONSE_NOTHING_ASK");
                break;
            case Config.RESPONSE_REFUSE_VOICE_CALL:
                TLog.d(TAG, "onReceiveResponseBean: RESPONSE_REFUSE_VOICE_CALL");
                MonitorUdpReceivePortThread.broadcastReceive(Config.RESPONSE_REFUSE_VOICE_CALL,null,host);
                break;
            case Config.RESPONSE_CONSENT_VOICE_CALL:
                TLog.d(TAG, "onReceiveResponseBean: RESPONSE_CONSENT_VOICE_CALL");
                onResponseConsentVoiceCall(host);
                break;
            case Config.RESPONSE_REFUSE_VIDEO_CALL:
                TLog.d(TAG, "onReceiveResponseBean: RESPONSE_REFUSE_VIDEO_CALL");
                break;
            case Config.RESPONSE_CONSENT_VIDEO_CALL:
                TLog.d(TAG, "onReceiveResponseBean: RESPONSE_CONSENT_VIDEO_CALL");
                break;
            case Config.RESPONSE_HUNG_UP_CALL:
                TLog.d(TAG, "onReceiveResponseBean: RESPONSE_HUNG_UP_CALL");
                onResponseHungUpCall(host);
                VoiceCallThread.getInstance().close();
                break;
            case Config.RESPONSE_WAITING_CONSENT:
                MonitorUdpReceivePortThread.broadcastReceive(Config.RESPONSE_WAITING_CONSENT,null,host);
                break;
            case Config.RESPONSE_CONSENT_OUT_TIME:
                MonitorUdpReceivePortThread.broadcastReceive(Config.RESPONSE_CONSENT_OUT_TIME,null,host);
                break;
            case Config.RESPONSE_IN_CALL:
                MonitorUdpReceivePortThread.broadcastReceive(Config.RESPONSE_IN_CALL,null,host);
                break;
            case Config.RESPONSE_REFUSE_MONITOR:
                MonitorUdpReceivePortThread.broadcastReceive(Config.RESPONSE_REFUSE_MONITOR,responseBean.getIdentifier(),null);
                break;
            case Config.RESPONSE_REFUSE_BE_MONITOR:
                MonitorUdpReceivePortThread.broadcastReceive(Config.RESPONSE_REFUSE_BE_MONITOR,responseBean.getIdentifier(),null);
                break;
        }
    }

    private void onResponseHungUpCall(String host) {
        MonitorUdpReceivePortThread.broadcastReceive(Config.RESPONSE_HUNG_UP_CALL,null,host);
    }

    private void onResponseConsentVoiceCall(String host) {
        MonitorUdpReceivePortThread.broadcastReceive(Config.RESPONSE_CONSENT_VOICE_CALL,null,host);
        try {
            Socket socket = new Socket(host,Config.PORT_TCP_CALL);
            VoiceCallThread.init(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onResponseReceiveFileFailure(ResponseBean responseBean) {
        if (!ResourceManager.getInstance().exist(responseBean.getIdentifier())){
            return;
        }
        ResourceManagerBean resourceManagerBean = ResourceManager.getInstance().getResource(responseBean.getIdentifier());
        if (!resourceManagerBean.isGroup()){
            ResourceManager.getInstance().remove(responseBean.getIdentifier());
        }
    }

    private void onResponseBusyTranspond(ResponseBean responseBean) {
        AskResourceBean askResourceBean = new AskResourceBean();
        askResourceBean.setResourceType(Config.RESOURCE_FILE);
        askResourceBean.setResourceUniqueIdentifier(responseBean.getIdentifier());
        Sender.sender(GsonTools.toJson(askResourceBean),Config.CODE_ASK_RESOURCE,responseBean.getRemark());
    }

    private void onResponseBusy() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
            }
        };
        timer.schedule(timerTask,30);
    }

    private void onResponseReceiveFileSuccess(ResponseBean responseBean,String host) {
        if (!ResourceManager.getInstance().exist(responseBean.getIdentifier())){
            return;
        }
        ResourceManagerBean resourceManagerBean = ResourceManager.getInstance().getResource(responseBean.getIdentifier());
        if (resourceManagerBean.isGroup()){
            resourceManagerBean.setDownLoadList(host);
        }else {
            ResourceManager.getInstance().remove(responseBean.getIdentifier());
        }
    }
}