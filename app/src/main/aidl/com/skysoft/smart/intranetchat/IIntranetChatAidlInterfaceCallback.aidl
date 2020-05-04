/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication] Create a new process service with message sending and receiving.
 */
// IIntranetChatAidlInterfaceCallback.aidl
package com.skysoft.smart.intranetchat;

// Declare any non-default types here with import statements

interface IIntranetChatAidlInterfaceCallback {

    void onReceiveUserInfo(String userInfoJson,String host);

    void onReceiveMessage(String messageJson,String host);

    void onReceiveRequest(String requestJson,String host);

    /*当接受到文件*/
    void onReceiveFile(String fileJson,String host);

    /*当接受到资源请求*/
    void onReceiveAskResource(String askResourceJson,String host);

    /*当接收文件并存储到本地*/
    void onReceiveAndSaveFile(String sender,String receiver,String identifier,String path,String host);

    /*当收到别人的语音电话请求*/
    void onReceiveVoiceCall(String userInfoJson,String host);

    /*当收到别人的视频电话请求*/
    void onReceiveVideoCall(String userInfoJson,String host);

    /*当别人接收语音电话*/
    void onReceiveConsentVoiceCall(String host);

    /*当对方拒绝语音电话*/
    void onReceiveRefuseVoiceCall(String host);

    /*当接受到语音数据*/
    void onReceiveVoiceCallData(String callBeanJson);

    /*当接收到挂断通知*/
    void onReceiveHungUpVoiceCall(String host);

    /*当建立好电话连接*/
    void onEstablishCallConnect();

    void onReceiveWaitingConsentCall();

    void onReceiveConsentOutTime();

    /*当收到*/
    void onReceiveEstablishBeanJson(String establishBeanJson,String host);

    void onReceiveAskResourceNotFound(String askResourceJson,String host);

    void onReceiveInCall(String host);

    void notifyClearQueue();

    /*当开始下载文件*/
    void askFile(String identifier,String path);

    /*当接收文件失败*/
    void receiveFileFailure(String identifier);

    /*当收到关于monitor的返回*/
    void receiveMonitorResponse(int monitorResponse, String identifier);

    /*接收到@消息*/
    void receiveNotifyMessageBean(String notifyMessageJson, String host);

    /*接收到回复消息*/
    void receiveReplayMessageBean(String replayMessageJson, String host);

    /*收到Identifier用户下线广播*/
    void receiveUserOutLine(String identifier);
}
