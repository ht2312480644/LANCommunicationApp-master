/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication] Create a new process service with message sending and receiving.
 */
// IIntranetChatAidlInterface.aidl
package com.skysoft.smart.intranetchat;

import com.skysoft.smart.intranetchat.IIntranetChatAidlInterfaceCallback;

interface IIntranetChatAidlInterface {
    /*在局域网中广播userInfo*/
    void broadcastUserInfo (in String userInfoJson);

    /*向某个用户发送userInfo*/
    void sendUserInfo(in String userInfoJson,in String host);

    /*在群组中广播userInfo*/
    void sendGroupUserInfo(in String userInfoJson,in List<String> hostList);

    /*在局域网中广播message*/
    void broadcastMessage(in String messageJson);

    /*向某个用户发送messaeg*/
    void sendMessage(in String messageJson,String host);

    /*在群组中广播userInfo*/
    void sendGroupMessage(in String messageJson,in List<String> hostList);

    /*在局域网中广播@消息*/
    void broadcastAtMessage(in String atMessageJson);

    /*在局域网中广播回复消息*/
    void broadcastReplayMessage(in String replayMessageJson);

    /*向所有用户请求用户信息*/
    void requestAllUserInfo(in String requestInfo);

    /*向某个用户请求用户信息*/
    void requestUserInfo(in String requestJson,in String host);

    /*在局域网中广播文件*/
    void broadcastFile(in String fileJson,in String path);

    /*在群组中广播文件*/
    void sendGroupFile(in String fileJson,in String path,in List<String> hostList);

    /*向某个用户发送文件*/
    void sendFile(in String fileJson,in String path,in String host);

    /*向某个用户请求资源*/
    void askResource(in String askResourceJson,in String host);

    /*向某个用户发送答复*/
    void sendResponse(in String responseJson,in String host);

    void downFile(in String fileJson,in String host);

    void setUserInfoBean(in String userInfoJson);

    /*发起语音电话*/
    void requestVoiceCall(in String userInfoJson,in String host);

    /*发起视频电话*/
    void requestVideoCall(in String userInfoJson,in String host);

    /*发送数据*/
    void sendVoiceCallData(in byte[] data);

    /*在通话过程中挂断电话*/
    void hungUpOnCall(in String host);

    /*请求接听电话*/
    void requestConsentCall(in String requestJson,in String host);

    /*向子进程的添加资源*/
    void addResourceManagerBean(in String fileBeanJson,in String path,in boolean group,in boolean receiver);

    void establishGroup(in String establishBeanJson,in List<String> hostList);

     /**
          * 数据自动反馈注册
          * @param sCallback
          */
     void registerCallback (IIntranetChatAidlInterfaceCallback sCallback);

    /**
         * 数据自动反馈解除
         * @param sCallback
         */
     void unregisterCallback (IIntranetChatAidlInterfaceCallback sCallback);

     void killProgress();

     void hostChanged(in String host);

     /*广播identifier用户离线*/
     void broadcastUserOutLine(String identifier);

     void initUserInfoBean(String userInfoJson);
}
