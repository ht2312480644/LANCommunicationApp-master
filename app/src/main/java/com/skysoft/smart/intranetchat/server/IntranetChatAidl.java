/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Create a UDP data sender
 */
package com.skysoft.smart.intranetchat.server;

import android.nfc.Tag;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.IIntranetChatAidlInterface;
import com.skysoft.smart.intranetchat.IIntranetChatAidlInterfaceCallback;
import com.skysoft.smart.intranetchat.model.network.bean.AskResourceBean;
import com.skysoft.smart.intranetchat.model.network.bean.ResponseBean;
import com.skysoft.smart.intranetchat.model.network.bean.UserInfoBean;
import com.skysoft.smart.intranetchat.model.network.call.VoiceCallThread;
import com.skysoft.smart.intranetchat.model.network.manager.PathManager;
import com.skysoft.smart.intranetchat.model.network.manager.ResourceManager;
import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.model.network.bean.FileBean;
import com.skysoft.smart.intranetchat.model.network.bean.ResourceManagerBean;
import com.skysoft.smart.intranetchat.model.network.manager.Sender;
import com.skysoft.smart.intranetchat.model.network.receive.MonitorUdpReceivePortThread;
import com.skysoft.smart.intranetchat.tools.GsonTools;

import java.util.List;

public class IntranetChatAidl extends IIntranetChatAidlInterface.Stub {
    private static String TAG = IntranetChatAidl.class.getSimpleName();
    private static final String BROADCAST_HOST = "255.255.255.255";
    private static RemoteCallbackList<IIntranetChatAidlInterfaceCallback> remoteCallbackList = new RemoteCallbackList<>();
    private static UserInfoBean userInfoBean;
    private static String callHost = null;

    @Override
    public void broadcastUserInfo(String userInfoJson) throws RemoteException {
        sendUserInfo(userInfoJson,BROADCAST_HOST);
    }

    @Override
    public void sendUserInfo(String userInfoJson, String host) throws RemoteException {
        send(userInfoJson,Config.CODE_USERINFO,host);
        userInfoBean = (UserInfoBean) GsonTools.formJson(userInfoJson,UserInfoBean.class);
    }

    @Override
    public void sendGroupUserInfo(String userInfoJson, List<String> hostList) throws RemoteException {
        sendGroup(userInfoJson,Config.CODE_USERINFO,hostList);
        userInfoBean = (UserInfoBean) GsonTools.formJson(userInfoJson,UserInfoBean.class);
    }

    @Override
    public void broadcastMessage(String messageJson) throws RemoteException {
        send(messageJson,Config.CODE_MESSAGE,"255.255.255.255");
    }

    @Override
    public void sendMessage(String messageJson, String host) throws RemoteException {
        TLog.d(TAG, "sendCommonMessage: " + messageJson);
        send(messageJson,Config.CODE_MESSAGE,host);
    }

    @Override
    public void sendGroupMessage(String messageJson, List<String> hostList) throws RemoteException {
        sendGroup(messageJson,Config.CODE_MESSAGE,hostList);
    }

    @Override
    public void broadcastAtMessage(String atMessageJson) throws RemoteException {
        TLog.d(TAG,"broadcastAtMessage: " + atMessageJson);
        send(atMessageJson,Config.CODE_MESSAGE_NOTIFICATION,BROADCAST_HOST);
    }

    @Override
    public void broadcastReplayMessage(String replayMessageJson) throws RemoteException {
        TLog.d(TAG,"sendReplayMessage: " + replayMessageJson);
        send(replayMessageJson,Config.CODE_MESSAGE_REPLAY,BROADCAST_HOST);
    }

    @Override
    public void requestAllUserInfo(String requestInfo) throws RemoteException {
        requestUserInfo(requestInfo,BROADCAST_HOST);
    }

    @Override
    public void requestUserInfo(String requestJson, String host) throws RemoteException {
        send(requestJson,Config.CODE_REQUEST,host);
    }

    @Override
    public void broadcastFile(String fileJson,String path) throws RemoteException {
        send(fileJson,Config.CODE_FILE,BROADCAST_HOST);
        resourceSendRecord(fileJson, path,false,true);
    }

    @Override
    public void sendGroupFile(String fileJson, String path, List<String> hostList) throws RemoteException {
        sendGroup(fileJson,Config.CODE_FILE,hostList);
        resourceSendRecord(fileJson,path,false,true);
    }

    @Override
    public void sendFile(String fileJson,String path, String host) throws RemoteException {
        TLog.d(TAG, "sendFile: path = " + path);
        send(fileJson,Config.CODE_FILE,host);
        resourceSendRecord(fileJson,path,false,false);
    }

    @Override
    public void askResource(String askResourceJson, String host) throws RemoteException {
        send(askResourceJson,Config.CODE_ASK_RESOURCE,host);
    }

    @Override
    public void sendResponse(String responseJson, String host) throws RemoteException {
        send(responseJson,Config.CODE_RESPONSE,host);
        ResponseBean responseBean = (ResponseBean) GsonTools.formJson(responseJson,ResponseBean.class);
        if (responseBean.getCode() == Config.RESPONSE_HUNG_UP_CALL){
            VoiceCallThread.getInstance().close();
        }
    }

    @Override
    public void downFile(String fileJson, String host) throws RemoteException {
        FileBean fileBean = (FileBean) GsonTools.formJson(fileJson,FileBean.class);
        resourceSendRecord(fileJson,PathManager.fromType(fileBean.getType()),true,false);
        AskResourceBean askResourceBean = new AskResourceBean();
        askResourceBean.setResourceUniqueIdentifier(fileBean.getFileUniqueIdentifier());
        askResourceBean.setResourceType(Config.RESOURCE_FILE);
        Sender.sender(GsonTools.toJson(askResourceBean),Config.CODE_ASK_RESOURCE,host);
    }

    @Override
    public void setUserInfoBean(String userInfoJson) throws RemoteException {

    }

    @Override
    public void requestVoiceCall(String userInfoJson, String host) throws RemoteException {
        send(userInfoJson,Config.CODE_VOICE_CALL,host);
        callHost = host;
    }

    @Override
    public void requestVideoCall(String userInfoJson, String host) throws RemoteException {
        send(userInfoJson,Config.CODE_VIDEO_CALL,host);
        callHost = host;
    }

    @Override
    public void sendVoiceCallData(byte[] data) throws RemoteException {
        VoiceCallThread instance = VoiceCallThread.getInstance();
        if (instance.isClose()){
            MonitorUdpReceivePortThread.broadcastReceive(Config.RESPONSE_HUNG_UP_CALL,null,null);
            return;
        }
        instance.setVoiceCallData(data);
    }

    @Override
    public void hungUpOnCall(String host) throws RemoteException {
        if (VoiceCallThread.getInstance().isCurrentHost(host)){
            VoiceCallThread.getInstance().close();
            TLog.d(TAG, "hungUpOnCall: ");
        }
    }

    @Override
    public void requestConsentCall(String requestJson, String host) throws RemoteException {
        send(requestJson,Config.CODE_REQUEST,host);
    }

    @Override
    public void addResourceManagerBean(String fileBeanJson, String path, boolean group, boolean receive) throws RemoteException {
        resourceSendRecord(fileBeanJson,path,group,receive);
    }

    @Override
    public void establishGroup(String establishBeanJson, List<String> hostList) throws RemoteException {
        sendGroup(establishBeanJson,Config.CODE_ESTABLISH_GROUP,hostList);
    }

    @Override
    public void registerCallback(IIntranetChatAidlInterfaceCallback callback) throws RemoteException {
        if (callback != null){
            remoteCallbackList.register(callback);
        }
    }

    @Override
    public void unregisterCallback(IIntranetChatAidlInterfaceCallback callback) throws RemoteException {
        if (callback != null){
            remoteCallbackList.unregister(callback);
        }

    }

    @Override
    public void killProgress() throws RemoteException {
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
    }

    @Override
    public void hostChanged(String host) throws RemoteException {
        MonitorUdpReceivePortThread.setMyHost(host);
    }

    @Override
    public void broadcastUserOutLine(String identifier) throws RemoteException {
        send(identifier,Config.CODE_USER_OUT_LINE,BROADCAST_HOST);
    }

    @Override
    public void initUserInfoBean(String userInfoJson) throws RemoteException {
        IntranetChatServer.sUserInfo = (UserInfoBean) GsonTools.formJson(userInfoJson,UserInfoBean.class);
        userInfoBean = IntranetChatServer.sUserInfo;
    }

    public RemoteCallbackList<IIntranetChatAidlInterfaceCallback> getRemoteCallbackList() {
        return remoteCallbackList;
    }

    public void setRemoteCallbackList(RemoteCallbackList<IIntranetChatAidlInterfaceCallback> remoteCallbackList) {
        this.remoteCallbackList = remoteCallbackList;
    }

    public static UserInfoBean getUserInfoBean() {
        return userInfoBean;
    }

    public void setUserInfoBean(UserInfoBean userInfoBean) {
        IntranetChatAidl.userInfoBean = userInfoBean;
    }

    /*发送*/
    private void send(String data,int code,String host){
        TLog.d(TAG, "send: host = " + host);
        Sender.sender(data,code,host);
    }

    /*组发送*/
    private void sendGroup(String data,int code,List<String> hostList){
        Sender.senderGroup(data,code,hostList);
    }

    /*文佳发送记录*/
    private void resourceSendRecord(String fileJson, String path,boolean receive,boolean group) {
        FileBean fileBean = (FileBean) GsonTools.formJson(fileJson,FileBean.class);
        if (fileBean == null){
            TLog.d(TAG, "resourceSendRecord: ");
        }
        boolean exist = ResourceManager.getInstance().exist(fileBean.getFileUniqueIdentifier());
        if (!exist){
            ResourceManagerBean resourceManagerBean = new ResourceManagerBean(fileBean, path);
            resourceManagerBean.setReceive(receive);
            resourceManagerBean.setGroup(group);
            ResourceManager.getInstance().setResource(fileBean.getFileUniqueIdentifier(),resourceManagerBean);
        }
    }
}
