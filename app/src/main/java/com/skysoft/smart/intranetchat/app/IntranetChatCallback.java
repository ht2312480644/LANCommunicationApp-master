/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Create a UDP data sender
 */
package com.skysoft.smart.intranetchat.app;

import android.os.RemoteException;
import android.text.TextUtils;

import com.skysoft.smart.intranetchat.IIntranetChatAidlInterfaceCallback;
import com.skysoft.smart.intranetchat.app.impl.OnEstablishCallConnect;
import com.skysoft.smart.intranetchat.app.impl.OnReceiveCallHungUp;
import com.skysoft.smart.intranetchat.app.impl.HandleVoiceCallResponse;
import com.skysoft.smart.intranetchat.app.impl.HandleInfo;
import com.skysoft.smart.intranetchat.app.impl.OnReceiveCallBean;
import com.skysoft.smart.intranetchat.app.impl.OnReceiveMessage;
import com.skysoft.smart.intranetchat.app.impl.OnReceiveRequestConsent;
import com.skysoft.smart.intranetchat.bean.FoundUserOutLineBean;
import com.skysoft.smart.intranetchat.bean.InCallBean;
import com.skysoft.smart.intranetchat.bean.LoadResourceBean;
import com.skysoft.smart.intranetchat.database.MyDataBase;
import com.skysoft.smart.intranetchat.database.dao.ChatRecordDao;
import com.skysoft.smart.intranetchat.database.table.ChatRecordEntity;
import com.skysoft.smart.intranetchat.database.table.ContactEntity;
import com.skysoft.smart.intranetchat.database.table.FileEntity;
import com.skysoft.smart.intranetchat.database.table.GroupEntity;
import com.skysoft.smart.intranetchat.database.table.GroupMemberEntity;
import com.skysoft.smart.intranetchat.database.table.LatestChatHistoryEntity;
import com.skysoft.smart.intranetchat.database.table.RefuseGroupEntity;
import com.skysoft.smart.intranetchat.model.net_model.EstablishGroup;
import com.skysoft.smart.intranetchat.model.net_model.Login;
import com.skysoft.smart.intranetchat.model.net_model.SendMessage;
import com.skysoft.smart.intranetchat.model.net_model.SendRequest;
import com.skysoft.smart.intranetchat.model.net_model.SendResponse;
import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.model.network.bean.AskBean;
import com.skysoft.smart.intranetchat.model.network.bean.AskResourceBean;
import com.skysoft.smart.intranetchat.model.network.bean.EstablishGroupBean;
import com.skysoft.smart.intranetchat.model.network.bean.FileBean;
import com.skysoft.smart.intranetchat.model.network.bean.MessageBean;
import com.skysoft.smart.intranetchat.model.network.bean.NotificationMessageBean;
import com.skysoft.smart.intranetchat.model.network.bean.ReceiveAndSaveFileBean;
import com.skysoft.smart.intranetchat.model.network.bean.ReplayMessageBean;
import com.skysoft.smart.intranetchat.model.network.bean.UserInfoBean;
import com.skysoft.smart.intranetchat.model.network.bean.VoiceCallDataBean;
import com.skysoft.smart.intranetchat.tools.GsonTools;
import com.skysoft.smart.intranetchat.tools.HandleReceivedUserInfo;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomConfig;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IntranetChatCallback extends IIntranetChatAidlInterfaceCallback.Stub {
    private final String TAG = IntranetChatCallback.class.getSimpleName();
    private HandleInfo handleInfo;
    private HandleVoiceCallResponse handleVoiceCallResponse;
    private OnReceiveCallHungUp hungUpInAnswer;
    private OnReceiveCallBean onReceiveCallBean;
    private OnReceiveMessage onReceiveMessage;
    private OnEstablishCallConnect onEstablishCallConnect;
    private OnReceiveRequestConsent onReceiveRequestConsent;

    public void setHandleInfo(HandleInfo handleInfo) {
        this.handleInfo = handleInfo;
    }

    public void setHungUpInAnswer(OnReceiveCallHungUp hungUpInAnswer) {
        this.hungUpInAnswer = hungUpInAnswer;
    }

    public void setHandleVoiceCallResponse(HandleVoiceCallResponse handleVoiceCallResponse) {
        this.handleVoiceCallResponse = handleVoiceCallResponse;
    }

    public void setOnReceiveCallBean(OnReceiveCallBean onReceiveCallBean) {
        this.onReceiveCallBean = onReceiveCallBean;
    }

    public void setOnReceiveMessage(OnReceiveMessage onReceiveMessage) {
        this.onReceiveMessage = onReceiveMessage;
    }

    public void setOnEstablishCallConnect(OnEstablishCallConnect onEstablishCallConnect) {
        this.onEstablishCallConnect = onEstablishCallConnect;
    }

    public void setOnReceiveRequestConsent(OnReceiveRequestConsent onReceiveRequestConsent) {
        this.onReceiveRequestConsent = onReceiveRequestConsent;
    }

    @Override
    public void onReceiveUserInfo(String userInfoJson, String host) throws RemoteException {
        TLog.d(TAG, "onReceiveUserInfo: userInfoJson: " + userInfoJson + ", host: " + host);
        if (host.equals(IntranetChatApplication.getHostIp())){
            return;
        }
        UserInfoBean userInfoBean = (UserInfoBean) GsonTools.formJson(userInfoJson,UserInfoBean.class);
        //请求监视对方
        if (IntranetChatApplication.sMonitor.size() < 4     //我监视的人数小于4
                && userInfoBean.getBeMonitored() < 4        //监视对方的人数小于4
                //对方没有被添加到监视名单
                && !IntranetChatApplication.sMonitor.containsKey(userInfoBean.getIdentifier())){        //最多被4人监听
            //请求对方同意被我监听
            if (!userInfoBean.getIdentifier().equals(IntranetChatApplication.getsMineUserInfo().getIdentifier())){
                IntranetChatApplication.sMonitor.put(userInfoBean.getIdentifier(),System.currentTimeMillis());
                SendRequest.sendMonitorRequest(Config.REQUEST_MONITOR,
                        IntranetChatApplication.getsMineUserInfo().getIdentifier(),
                        host);
            }
        }

        //请求被对方监视
        if (IntranetChatApplication.sBeMonitored.size() < 4     //监视我的人数小于4
                && userInfoBean.getMonitor() < 4                //对方监视的人数小于4
                //对方没有被添加到监视我的名单
                && !IntranetChatApplication.sBeMonitored.containsKey(userInfoBean.getIdentifier())){
            //请求对方监听我
            if (!userInfoBean.getIdentifier().equals(IntranetChatApplication.getsMineUserInfo().getIdentifier())){
                IntranetChatApplication.sBeMonitored.put(userInfoBean.getIdentifier(),System.currentTimeMillis());
                SendRequest.sendMonitorRequest(Config.REQUEST_BE_MONITOR,
                        IntranetChatApplication.getsMineUserInfo().getIdentifier(),
                        host);
            }
        }
        HandleReceivedUserInfo.handleReceivedUserInfo(userInfoBean,host);
    }

    @Override
    public void onReceiveMessage(String messageJson, String host) throws RemoteException {
        TLog.d(TAG, "notification: messageJson: " + messageJson + ", host: " + host);
        if (host.equals(IntranetChatApplication.getHostIp())){
            return;
        }
//        Login.requestUserInfo(host);  //请求心跳数据
        MessageBean messageBean = (MessageBean) GsonTools.formJson(messageJson,MessageBean.class);
        if (messageBean.getSender().equals(IntranetChatApplication.getsMineUserInfo().getIdentifier())){
            return;
        }

        messageBean.setHost(host);
        handleMessageBean(messageBean);
    }

    @Override
    public void receiveNotifyMessageBean(String notifyMessageJson, String host) throws RemoteException {
        TLog.d(TAG,"notifyMessageJson " + notifyMessageJson);
        NotificationMessageBean notificationMessageBean = (NotificationMessageBean) GsonTools.formJson(notifyMessageJson,NotificationMessageBean.class);
        handleMessageBean(notificationMessageBean);
    }

    @Override
    public void receiveReplayMessageBean(String replayMessageJson, String host) throws RemoteException {
        TLog.d(TAG,"replayMessageJson " + replayMessageJson);
        ReplayMessageBean replayMessageBean = (ReplayMessageBean) GsonTools.formJson(replayMessageJson,ReplayMessageBean.class);
        handleMessageBean(replayMessageBean);
    }

    /**
     * 在已有的所有联系人和群名单中没有找到messageBean对应的人
     * @param messageBean*/
    private void notFoundContact(MessageBean messageBean){
        //查询拒收名单是否有此人
        Iterator<RefuseGroupEntity> refuseIterator = IntranetChatApplication.getsRefuseGroupList().iterator();
        while (refuseIterator.hasNext()){
            if (refuseIterator.next().getIdentifier().equals(messageBean.getReceiver())){
                return;
            }
        }

        if (!messageBean.getReceiver().equals(messageBean.getSender())){    //为记录的群
            AskResourceBean askResourceBean = new AskResourceBean();
            askResourceBean.setResourceType(Config.RESOURCE_GROUP);     //请求群信息
            askResourceBean.setResourceUniqueIdentifier(messageBean.getReceiver());     //群唯一标识符
            try {
                IntranetChatApplication.sAidlInterface.askResource(GsonTools.toJson(askResourceBean),messageBean.getHost());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleMessageBean(MessageBean messageBean){
        //只要能在联系人或者群名单找到对应聊天室
        if (IntranetChatApplication.sContactMap.containsKey(messageBean.getReceiver()) ||
                IntranetChatApplication.sGroupContactMap.containsKey(messageBean.getReceiver())){
            EventBus.getDefault().post(messageBean);
            return;
        }

        notFoundContact(messageBean);
    }

    @Override
    public void onReceiveRequest(String requestJson, String host) throws RemoteException {
        TLog.d(TAG, "onReceiveRequest: requestJson: " + requestJson + ", host: " + host);
        if (host.equals(IntranetChatApplication.getHostIp())){
            return;
        }
        AskBean askBean = (AskBean) GsonTools.formJson(requestJson, AskBean.class);
        switch (askBean.getRequestType()){
            case Config.REQUEST_USERINFO:
                UserInfoBean userInfoBean = IntranetChatApplication.getsMineUserInfo();
                userInfoBean.setMonitor(IntranetChatApplication.sMonitor.size());
                userInfoBean.setBeMonitored(IntranetChatApplication.sBeMonitored.size());
                userInfoBean.setRemark(null);
                if (userInfoBean != null){
                    IntranetChatApplication.sAidlInterface.sendUserInfo(GsonTools.toJson(userInfoBean),host);
                }
                break;
            case Config.REQUEST_CONSENT_CALL:
                if (onReceiveRequestConsent != null){
                    onReceiveRequestConsent.onReceiveRequestConsent();
                }
                break;
        }
    }

    @Override
    public void onReceiveFile(String fileJson, String host) throws RemoteException {
        TLog.d(TAG, "onReceiveFile: fileJson" + fileJson + ",host: " + host);
//        Login.requestUserInfo(host);
        //向host请求文件
        if (host.equals(IntranetChatApplication.getHostIp())){
            return;
        }
        FileBean fileBean = (FileBean) GsonTools.formJson(fileJson,FileBean.class);
        if (fileBean.getSender().equals(IntranetChatApplication.getsMineUserInfo().getIdentifier())){
            return;
        }
        if (fileBean.getType() == Config.FILE_AVATAR){
            return;
        }

        //记录收到的文件，除了头像
        if (TextUtils.isEmpty(fileBean.getFileUniqueIdentifier())){
            return;
        }
        FileEntity fileEntity = new FileEntity();
        fileEntity.setIdentifier(fileBean.getFileUniqueIdentifier());
        fileEntity.setType(fileBean.getType());
        fileEntity.setFileName(fileBean.getFileName());
        fileEntity.setSender(fileBean.getSender());
        fileEntity.setReceiver(fileBean.getReceiver());
        fileEntity.setMd5(fileBean.getMd5());
        fileEntity.setStep(Config.STEP_RECEIVE_NOTIFY);
        fileEntity.setTime(System.currentTimeMillis());
        IntranetChatApplication.getMonitorReceiveFile().put(fileEntity.getIdentifier(),fileEntity);
        new Thread(new Runnable() {
            @Override
            public void run() {
                MyDataBase.getInstance().getFileDao().insert(fileEntity);
            }
        }).start();

        EventBus.getDefault().post(fileBean);

        if (fileBean.getType() == Config.FILE_VIDEO){
            return;
        }

        if (onReceiveMessage != null){
            onReceiveMessage.onReceiveFile(fileBean,host);
        }
    }

    @Override
    public void onReceiveAskResource(String askResourceJson, String host) throws RemoteException {
        TLog.d(TAG, "onReceiveAskResource: askResourceJson" + askResourceJson + ",host: " + host);
        if (host.equals(IntranetChatApplication.getHostIp())){
            return;
        }
//        Login.requestUserInfo(host);
        AskResourceBean askResourceBean = (AskResourceBean) GsonTools.formJson(askResourceJson,AskResourceBean.class);
        switch (askResourceBean.getResourceType()){
            case Config.RESOURCE_AVATAR:        //某个用户向我请求头像数据
                Login.notifyChangeAvatar(host);
                break;
            case Config.REQUEST_MONITOR:        //某个用户请求做我的监视者
                if (IntranetChatApplication.sBeMonitored.size() < 4){
                    IntranetChatApplication.sBeMonitored.put(askResourceBean.getResourceUniqueIdentifier(),
                            System.currentTimeMillis());
                }else {     //拒绝被他监视
                    SendResponse.sendMonitorResponse(Config.RESPONSE_REFUSE_MONITOR,
                            IntranetChatApplication.getsMineUserInfo().getIdentifier(),
                            host);
                }
                break;
            case Config.REQUEST_BE_MONITOR:     //对方请求我监听他
                if (IntranetChatApplication.sMonitor.size() < 4){       //我监视的人数小于4，同意
                    IntranetChatApplication.sMonitor.put(askResourceBean.getResourceUniqueIdentifier(),
                            System.currentTimeMillis());
                }else {     //拒绝被他监视
                    SendResponse.sendMonitorResponse(Config.RESPONSE_REFUSE_BE_MONITOR,
                            IntranetChatApplication.getsMineUserInfo().getIdentifier(),
                            host);
                }
                break;
            case Config.HEARTBEAT:      //收到对方发送的心跳
                //更新心跳
                IntranetChatApplication.sMonitor.put(askResourceBean.getResourceUniqueIdentifier(),System.currentTimeMillis());
                break;
            case Config.REQUEST_HEARTBEAT:      //对方认为我假死，向我请求心跳
                SendRequest.sendHeartbeat(IntranetChatApplication.getsMineUserInfo().getIdentifier(),host);
                //如果对方不在我的监视者列表中
                if (null == IntranetChatApplication.sBeMonitored.get(askResourceBean.getResourceUniqueIdentifier())){
                    if (IntranetChatApplication.sBeMonitored.size() < 4){       //如果我的监视者小于4人
                        //接收对方为监视者
                        IntranetChatApplication.sBeMonitored.put(askResourceBean.getResourceUniqueIdentifier(),System.currentTimeMillis());
                    }else {
                        //否则拒绝对方成为我的监视者
                        SendResponse.sendMonitorResponse(Config.RESPONSE_REFUSE_BE_MONITOR,
                                IntranetChatApplication.getsMineUserInfo().getIdentifier(),
                                host);
                    }
                }
                break;
        }
    }

    @Override
    public void onReceiveAndSaveFile(String sender,String receiver,String identifier,String path,String host) throws RemoteException {
        TLog.d(TAG, "onReceiveAndSaveFile: sender: " + sender + ",receiver: " + receiver + ",identifier: " + identifier + ",path: " + path);
        if (host.equals(IntranetChatApplication.getHostIp())){
            return;
        }
        if (sender.equals(IntranetChatApplication.getsMineUserInfo().getIdentifier())){
            return;
        }
        ReceiveAndSaveFileBean receiveAndSaveFileBean = new ReceiveAndSaveFileBean(sender,receiver,identifier,path);
        receiveAndSaveFileBean.setHost(host);
        EventBus.getDefault().post(receiveAndSaveFileBean);
        if (onReceiveMessage != null){
            onReceiveMessage.onReceiveAndSaveFile(sender,receiver,identifier,path);
        }
        //记录文件下载成功
        setMonitorReceiveFile(identifier,path,Config.STEP_SUCCESS);
    }

    @Override
    public void onReceiveVoiceCall(String userInfoJson, String host) throws RemoteException {
        TLog.d(TAG, "onReceiveVoiceCall: userInfoJson: " + userInfoJson + ",host: " + host);
//        Login.requestUserInfo(host);
        if (handleInfo != null){
            handleInfo.onReceiveVoiceCall((UserInfoBean) GsonTools.formJson(userInfoJson,UserInfoBean.class),host);
        }
    }

    @Override
    public void onReceiveVideoCall(String userInfoJson, String host) throws RemoteException {
//        Login.requestUserInfo(host);
        TLog.d(TAG, "onReceiveVideoCall: userInfoJson = " + userInfoJson + ", host = " + host);
        if (handleInfo != null){
            handleInfo.onReceiveVideoCall((UserInfoBean) GsonTools.formJson(userInfoJson,UserInfoBean.class),host);
        }
    }

    @Override
    public void onReceiveConsentVoiceCall(String host) throws RemoteException {
//        Login.requestUserInfo(host);
        if (handleVoiceCallResponse != null){
            handleVoiceCallResponse.onReceiveConsentVoiceCall(host);
        }
    }

    @Override
    public void onReceiveRefuseVoiceCall(String host) throws RemoteException {
//        Login.requestUserInfo(host);
        if (handleVoiceCallResponse != null){
            handleVoiceCallResponse.onReceiveRefuseVoiceCall(host);
        }
    }

    @Override
    public void onReceiveVoiceCallData(String callBeanJson) throws RemoteException {
        VoiceCallDataBean data = (VoiceCallDataBean) GsonTools.formJson(callBeanJson, VoiceCallDataBean.class);
        if (onReceiveCallBean != null){
            onReceiveCallBean.onReceiveVoiceCallData(data.getData());
        }
    }

    @Override
    public void onReceiveHungUpVoiceCall(String host) throws RemoteException {
        TLog.d(TAG, "onReceiveHungUpVoiceCall: " + host);
//        Login.requestUserInfo(host);
        if (hungUpInAnswer != null){
            hungUpInAnswer.onReceiveHungUpVoiceCall(host);
        }
    }

    @Override
    public void onEstablishCallConnect() throws RemoteException {
        if (onEstablishCallConnect != null){
            onEstablishCallConnect.onEstablishCallConnect();
        }
    }

    @Override
    public void onReceiveWaitingConsentCall() throws RemoteException {
        if(handleVoiceCallResponse != null){
            handleVoiceCallResponse.onReceiveWaitingConsentCall();
        }
    }

    @Override
    public void onReceiveConsentOutTime() throws RemoteException {
        if (handleVoiceCallResponse != null){
            handleVoiceCallResponse.onReceiveConsentOutTime();
        }
    }

    @Override
    public void onReceiveEstablishBeanJson(String establishBeanJson, String host) throws RemoteException {
        TLog.d(TAG, "onReceiveEstablishBeanJson: establishBeanJson = " + establishBeanJson + ", host = " + host);
        if (host.equals(IntranetChatApplication.getHostIp())){
            return;
        }
//        Login.requestUserInfo(host);
        EstablishGroupBean establishGroupBean = (EstablishGroupBean) GsonTools.formJson(establishBeanJson,EstablishGroupBean.class);
        if (establishGroupBean != null){
            Iterator<UserInfoBean> iterator = establishGroupBean.getmUsers().iterator();
            String identifier = IntranetChatApplication.getsMineUserInfo().getIdentifier();
            //判断‘我’是否在群聊的成员中
            while (iterator.hasNext()){
                UserInfoBean next = iterator.next();
                if (next.getIdentifier().equals(identifier)){
                    //在群的成员中
                    EventBus.getDefault().post(establishGroupBean);
                    return;
                }
            }
            //不在群的成员中
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RefuseGroupEntity refuseGroupEntity = new RefuseGroupEntity();
                    refuseGroupEntity.setIdentifier(establishGroupBean.getmGroupIdentifier());
                    Iterator<RefuseGroupEntity> refuseIterator = IntranetChatApplication.getsRefuseGroupList().iterator();
                    while (refuseIterator.hasNext()){
                        RefuseGroupEntity next = refuseIterator.next();
                        if (next.getIdentifier().equals(establishGroupBean.getmGroupIdentifier())){
                            return;
                        }
                    }
                    MyDataBase.getInstance().getRefuseGroupDao().insert(refuseGroupEntity);
                    IntranetChatApplication.getsRefuseGroupList().add(refuseGroupEntity);
                }
            }).start();
        }
    }

    @Override
    public void onReceiveAskResourceNotFound(String askResourceJson, String host) throws RemoteException {
        TLog.d(TAG, "onReceiveAskResourceNotFound: askResourceJson = " + askResourceJson + ", host = " + host);
        if (host.equals(IntranetChatApplication.getHostIp())){
            return;
        }
        AskResourceBean askResourceBean = (AskResourceBean) GsonTools.formJson(askResourceJson,AskResourceBean.class);
        if (askResourceBean.getResourceType() == Config.RESOURCE_AVATAR){
            Login.notifyChangeAvatar(host);
        }else if (askResourceBean.getResourceType() == Config.RESOURCE_GROUP){
            TLog.d(TAG, "onReceiveAskResource: ask resource group");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    GroupEntity groupEntity = MyDataBase.getInstance().getGroupDao().getGroupEntity(askResourceBean.getResourceUniqueIdentifier());
                    EstablishGroupBean establishGroupBean = new EstablishGroupBean();
                    List<UserInfoBean> members = new ArrayList<>();
                    establishGroupBean.setmHolderIdentifier(groupEntity.getGroupHolder());
                    establishGroupBean.setmGroupIdentifier(groupEntity.getGroupIdentifier());
                    //填充群名字和头像
                    ContactEntity next = IntranetChatApplication.sGroupContactMap.get(groupEntity.getGroupIdentifier());
                    if (null != next){
                        establishGroupBean.setmName(next.getName());
                        establishGroupBean.setmGroupAvatarIdentifier(next.getAvatarIdentifier());
                    }
                    List<GroupMemberEntity> allGroupMember = MyDataBase.getInstance().getGroupMemberDao().getAllGroupMember(groupEntity.getGroupIdentifier());
                    Iterator<GroupMemberEntity> memberIterator = allGroupMember.iterator();
                    //填充群成员信息
                    while (memberIterator.hasNext()){
                        GroupMemberEntity nextGroupMember = memberIterator.next();
                        UserInfoBean userInfoBean = new UserInfoBean();
                        userInfoBean.setIdentifier(nextGroupMember.getGroupMemberIdentifier());
                        ContactEntity contactEntity = IntranetChatApplication.sGroupContactMap.get(nextGroupMember.getGroupMemberIdentifier());
                        if (null != contactEntity){
                            userInfoBean.setStatus(1);
                            userInfoBean.setName(contactEntity.getName());
                            userInfoBean.setAvatarIdentifier(contactEntity.getAvatarIdentifier());
                        }
                        members.add(userInfoBean);
                    }
                    establishGroupBean.setmUsers(members);
                    EstablishGroup.establishGroup(establishGroupBean,host);
                }
            }).start();
        }
    }

    @Override
    public void onReceiveInCall(String host) throws RemoteException {
        TLog.d(TAG, "onReceiveInCall: host = " + host);
        if (handleVoiceCallResponse != null){
            handleVoiceCallResponse.onReceiveInCall(host);
        }
        EventBus.getDefault().post(new InCallBean(true));
    }

    @Override
    public void notifyClearQueue() throws RemoteException {
        TLog.d(TAG, "notifyClearQueue: size = " + IntranetChatApplication.getmDatasQueue().size());
        IntranetChatApplication.getmDatasQueue().clear();
    }

    /**
     * 记录开始下载文件*/
    @Override
    public void askFile(String identifier, String path) throws RemoteException {
        TLog.d(TAG, "askFile: identifier = " + identifier + ", path = " + path);
        setMonitorReceiveFile(identifier,path,Config.STEP_ASK_FILE);
    }

    /**
     * 记录文件下载失败*/
    @Override
    public void receiveFileFailure(String identifier) throws RemoteException {
        TLog.d(TAG, "askFile: identifier = " + identifier);
        setMonitorReceiveFile(identifier,null,Config.STEP_DOWN_LOAD_FAILURE);
    }

    @Override
    public void receiveMonitorResponse(int monitorResponse, String identifier) throws RemoteException {
        if (monitorResponse == Config.RESPONSE_REFUSE_MONITOR &&
                IntranetChatApplication.sMonitor.containsKey(identifier)){
            IntranetChatApplication.sMonitor.remove(identifier);
        }else if (monitorResponse == Config.RESPONSE_REFUSE_BE_MONITOR &&
                IntranetChatApplication.sBeMonitored.containsKey(identifier)){
            IntranetChatApplication.sBeMonitored.remove(identifier);
        }
    }

    @Override
    public void receiveUserOutLine(String identifier) throws RemoteException {

        ContactEntity contactEntity = IntranetChatApplication.sContactMap.get(identifier);
        if (null != contactEntity){
            TLog.d(TAG, "receiveUserOutLine: " + contactEntity.getName());
            if (IntranetChatApplication.sMonitor.containsKey(identifier)){      //如果在监视列表中，从列表中移除
                IntranetChatApplication.sMonitor.remove(identifier);
            }

            if (IntranetChatApplication.sBeMonitored.containsKey(identifier)){  //如果在监视者（监视我的人）列表中，从列表中移出
                IntranetChatApplication.sBeMonitored.remove(identifier);
            }

            //如果监视列表和监视者列表其中一项为空，广播我的信息，重新与其他用户建立链接
            if (IntranetChatApplication.sMonitor.size() == 0 || IntranetChatApplication.sBeMonitored.size() == 0){
                Login.broadcastUserInfo();
            }
            EventBus.getDefault().post(new FoundUserOutLineBean(identifier,1));     //刷新联系人界面
        }else if (identifier.equals(IntranetChatApplication.getsMineUserInfo().getIdentifier())){
            Login.broadcastUserInfo();      //别人以为我下线，广播我的信息，告知其他用户我在线
        }
    }

    /**记录文件接收情况*/
    private void setMonitorReceiveFile(String identifier, String path, int step){
        if (step == Config.STEP_SUCCESS){
            IntranetChatApplication.getMonitorReceiveFile().remove(identifier);
        }
        FileEntity fileEntity = IntranetChatApplication.getMonitorReceiveFile().get(identifier);
        if (fileEntity != null){
            fileEntity.setStep(step);
            fileEntity.setTime(System.currentTimeMillis());
            if (!TextUtils.isEmpty(path)){
                fileEntity.setPath(path);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MyDataBase.getInstance().getFileDao().update(fileEntity);
                    if (step == Config.STEP_DOWN_LOAD_FAILURE){
                        ChatRecordDao chatRecordDao = MyDataBase.getInstance().getChatRecordDao();
                        ChatRecordEntity recordByFileIdentifier = chatRecordDao.getRecordByFileIdentifier(fileEntity.getReceiver(), identifier);
                        if (recordByFileIdentifier != null){
                            recordByFileIdentifier.setReceiveSuccess(false);
                            chatRecordDao.update(recordByFileIdentifier);

                            //刷新聊天室
                            ReceiveAndSaveFileBean receiveAndSaveFileBean = new ReceiveAndSaveFileBean();
                            if (IntranetChatApplication.getsChatRoomMessageAdapter() != null && !TextUtils.isEmpty(IntranetChatApplication.getsChatRoomMessageAdapter().getReceiverIdentifier()) && IntranetChatApplication.getsChatRoomMessageAdapter().getReceiverIdentifier().equals(recordByFileIdentifier.getReceiver())) {
                                EventBus.getDefault().post(new LoadResourceBean(receiveAndSaveFileBean,recordByFileIdentifier));
                            }
                        }
                    }
                }
            }).start();
        }
    }

    private LatestChatHistoryEntity generateLatestChatHistoryEntity(long time,String content,
                                                                    String avatarIdentifier,
                                                                    String avatarPath,String receiver,String sender,
                                                                    String userName,String host){
        LatestChatHistoryEntity latestChatHistoryEntity = new LatestChatHistoryEntity();
        latestChatHistoryEntity.setContentTime(ChatRoomActivity.millsToTime(time));
        latestChatHistoryEntity.setContent(content);
        latestChatHistoryEntity.setUserHeadIdentifier(avatarIdentifier);
        latestChatHistoryEntity.setUserHeadPath(avatarPath);
        latestChatHistoryEntity.setUserIdentifier(receiver);
        latestChatHistoryEntity.setSenderIdentifier(sender);
        latestChatHistoryEntity.setUserName(userName);
        latestChatHistoryEntity.setStatus(Config.STATUS_ONLINE);
        latestChatHistoryEntity.setHost(host);
        return latestChatHistoryEntity;
    }
}
