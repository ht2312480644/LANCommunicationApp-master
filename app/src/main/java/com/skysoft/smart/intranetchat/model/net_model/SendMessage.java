/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/1
 * Description: [Intranet Chat] [APP][UI] Chat Room
 */
package com.skysoft.smart.intranetchat.model.net_model;

import android.os.RemoteException;
import android.text.TextUtils;

import com.skysoft.smart.intranetchat.bean.SendAtMessageBean;
import com.skysoft.smart.intranetchat.bean.SendReplayMessageBean;
import com.skysoft.smart.intranetchat.model.network.bean.NotificationMessageBean;
import com.skysoft.smart.intranetchat.model.network.bean.ReplayMessageBean;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.bean.SendMessageBean;
import com.skysoft.smart.intranetchat.database.MyDataBase;
import com.skysoft.smart.intranetchat.database.table.ChatRecordEntity;
import com.skysoft.smart.intranetchat.database.table.FileEntity;
import com.skysoft.smart.intranetchat.database.table.LatestChatHistoryEntity;
import com.skysoft.smart.intranetchat.model.camera.entity.EventMessage;
import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.model.network.bean.MessageBean;
import com.skysoft.smart.intranetchat.model.network.bean.ReceiveAndSaveFileBean;
import com.skysoft.smart.intranetchat.tools.GsonTools;
import com.skysoft.smart.intranetchat.tools.Identifier;
import com.skysoft.smart.intranetchat.tools.listsort.MessageListSort;
import com.skysoft.smart.intranetchat.tools.toastutil.ToastUtil;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomActivity;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomConfig;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomMessageAdapter;

import java.io.File;

public class SendMessage {
    private static String TAG = SendMessage.class.getSimpleName();
    public static void sendCommonMessage(MessageBean messageBean, String host){
        try {
            IntranetChatApplication.sAidlInterface.sendMessage(GsonTools.toJson(messageBean),host);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * at消息只有群众存在，所以只需要广播
     * @param messageBean @消息*/
    public static void broadcastAtMessage(NotificationMessageBean messageBean){
        try {
            IntranetChatApplication.sAidlInterface.broadcastAtMessage(GsonTools.toJson(messageBean));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 回复消息只有群中存在，所以只需要广播
     * @param messageBean 回复消息*/
    public static void broadcastReplayMessage(ReplayMessageBean messageBean){
        try {
            IntranetChatApplication.sAidlInterface.broadcastReplayMessage(GsonTools.toJson(messageBean));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void broadMessage(MessageBean messageBean){
        try {
            IntranetChatApplication.sAidlInterface.broadcastMessage(GsonTools.toJson(messageBean));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送消息同时更新消息界面*/
    public static ChatRecordEntity sendCommonMessage(SendMessageBean sendMessageBean){
        MessageBean messageBean = initMessageBean(new MessageBean(), sendMessageBean);     //初始化待发送的MessageBean

        ChatRecordEntity chatRecordEntity = initChatRecordEntity(messageBean,sendMessageBean);
        chatRecordEntity.setIsReceive(ChatRoomConfig.SEND_MESSAGE);     //标记为发消息的类型

        if (!sendMessageBean.isGroup()){
            if (!TextUtils.isEmpty(sendMessageBean.getHost())){
                SendMessage.sendCommonMessage(messageBean,sendMessageBean.getHost());
            }
        }else {
            SendMessage.broadMessage(messageBean);
        }

        refreshMessageFragment(sendMessageBean);
        return chatRecordEntity;
    }

    /**
     * 发送@消息同时更新消息界面*/
    public static ChatRecordEntity broadcastAtMessage(SendAtMessageBean sendMessageBean){
        //初始化NotificationMessageBean
        NotificationMessageBean messageBean = initMessageBean(new NotificationMessageBean(),sendMessageBean);

        messageBean.setmNotificationUsers(sendMessageBean.getmAt());    //装填@对象
        messageBean.setType(1);

        SendMessage.broadcastAtMessage(messageBean);        //广播@消息

        refreshMessageFragment(sendMessageBean);        //刷新消息界面

        //初始化ChatRecordEntity
        ChatRecordEntity chatRecordEntity = initChatRecordEntity(messageBean,sendMessageBean);
        chatRecordEntity.setIsReceive(ChatRoomConfig.SEND_AT_MESSAGE);

        return chatRecordEntity;
    }

    /**
     * 发送回复消息同事更新消息界面
     * @param sendMessageBean 回复消息*/
    public static ChatRecordEntity broadcastReplayMessage(SendReplayMessageBean sendMessageBean){
        //初始化ReplayMessageBean
        ReplayMessageBean messageBean = initMessageBean(new ReplayMessageBean(), sendMessageBean);

        messageBean.setmNotificationUsers(sendMessageBean.getmAt());        //装填@对象
        messageBean.setmReplayContent(sendMessageBean.getReplayContent());      //装填回复内容
        messageBean.setmReplayName(sendMessageBean.getReplayName());            //装填回复对象名称
        messageBean.setmReplayType(sendMessageBean.getReplayType());            //装填回复内容内型
        messageBean.setType(2);

        SendMessage.broadcastReplayMessage(messageBean);        //广播回复消息

        refreshMessageFragment(sendMessageBean);                //刷新消息界面

        //初始化ChatRecordEntity
        ChatRecordEntity chatRecordEntity = initChatRecordEntity(messageBean,sendMessageBean);
        chatRecordEntity.setIsReceive(ChatRoomConfig.SEND_REPLAY_MESSAGE);

        return chatRecordEntity;
    }

    /**
     * SendMessageBean初始化为MessageBean
     * @param sendMessageBean
     * @return */
    public static <T extends MessageBean> T initMessageBean(T messageBean, SendMessageBean sendMessageBean){
        messageBean.setMsg(sendMessageBean.getMessage());       //消息内容
        messageBean.setTimeStamp(System.currentTimeMillis());   //消息时间戳
        messageBean.setSender(IntranetChatApplication.getsMineUserInfo().getIdentifier());      //记录发送者
        if (sendMessageBean.isGroup()){     //群聊接收者为群
            messageBean.setReceiver(sendMessageBean.getReciever());
        }else {         //私聊接受者为自己
            messageBean.setReceiver(messageBean.getSender());
        }
        return messageBean;
    }

    /**
     * 依据SendMessageBean和MessageBean生成ChatRecordEntity
     * @param messageBean
     * @return */
    public static ChatRecordEntity initChatRecordEntity(MessageBean messageBean){
        ChatRecordEntity chatRecordEntity = new ChatRecordEntity();
        chatRecordEntity.setContent(messageBean.getMsg());      //记录发送内容
        chatRecordEntity.setReceiver(messageBean.getReceiver());
        chatRecordEntity.setSender(IntranetChatApplication.getsMineUserInfo().getIdentifier());     //记录发送者
        chatRecordEntity.setTime(messageBean.getTimeStamp());           //记录发送时间

        switch (messageBean.getType()){
            case 0:
                chatRecordEntity.setType(ChatRoomConfig.RECORD_TEXT);//发送消息
                break;
            case 1:
                if (messageBean instanceof NotificationMessageBean){
                    NotificationMessageBean notificationMessageBean = (NotificationMessageBean) messageBean;
                    chatRecordEntity.setFileName(GsonTools.toJson(notificationMessageBean.getmNotificationUsers()));       //记录@列表
                    chatRecordEntity.setType(ChatRoomConfig.RECORD_NOTIFY_MESSAGE);         //记录类型：@
                }
                break;
            case 2:
                if (messageBean instanceof ReplayMessageBean){
                    ReplayMessageBean replayMessageBean = (ReplayMessageBean) messageBean;
                    chatRecordEntity.setFileName(GsonTools.toJson(replayMessageBean.getmNotificationUsers()));       //记录@列表
                    chatRecordEntity.setPath(replayMessageBean.getmReplayName() + "|" + replayMessageBean.getmReplayContent());     //记录回复对象和回复内容
                    chatRecordEntity.setLength(replayMessageBean.getmReplayType());     //记录回复类型
                    chatRecordEntity.setType(ChatRoomConfig.RECORD_REPLAY_MESSAGE);     //记录类型：回复
                }
                break;
        }
        return chatRecordEntity;
    }

    public static ChatRecordEntity initChatRecordEntity(MessageBean messageBean, SendMessageBean sendMessageBean){
        ChatRecordEntity chatRecordEntity = initChatRecordEntity(messageBean);
        chatRecordEntity.setReceiver(sendMessageBean.getReciever());
        return chatRecordEntity;
    }

    /**
     * 接收或者发送消息后更新消息界面
     * @param sendMessageBean 更新消息列表需要的数据
     * content 消息显示的内容
     * receiverAvatarPath 聊天室的头像地址
     * host 聊天室的IP
     *  receiverIdentifier 聊天室的唯一标识符
     *  receiverName 聊天室的名字*/
    public static void refreshMessageFragment(SendMessageBean sendMessageBean) {
        LatestChatHistoryEntity next = IntranetChatApplication.sLatestChatHistoryMap.get(sendMessageBean.getReciever());
        if (null != next) {
            next.setContent(sendMessageBean.getMessage());
            next.setHost(sendMessageBean.getHost());
            next.setUnReadNumber(0);
            next.setContentTime(ChatRoomActivity.millsToTime(System.currentTimeMillis()));
            next.setContentTimeMill(System.currentTimeMillis());
            next.setStatus(com.skysoft.smart.intranetchat.model.network.Config.STATUS_ONLINE);
            next.setUserName(sendMessageBean.getName());
            next.setUserHeadPath(sendMessageBean.getAvatar());
            MessageListSort.CollectionsList(IntranetChatApplication.getMessageList());
            IntranetChatApplication.getsMessageListAdapter().notifyDataSetChanged();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    MyDataBase.getInstance().getLatestChatHistoryDao().update(next);
                }
            }).start();
            return;
        }

        //既有的记录中没有找到对应记录，创建记录，增加记录
        LatestChatHistoryEntity latestChatHistoryEntity = adapterToLatest(sendMessageBean);
        IntranetChatApplication.getMessageList().add(latestChatHistoryEntity.getUserIdentifier());
        IntranetChatApplication.sLatestChatHistoryMap.put(latestChatHistoryEntity.getUserIdentifier(),latestChatHistoryEntity);

        MessageListSort.CollectionsList(IntranetChatApplication.getMessageList());
        IntranetChatApplication.getsMessageListAdapter().notifyDataSetChanged();
        new Thread(new Runnable() {
            @Override
            public void run() {
                MyDataBase.getInstance().getLatestChatHistoryDao().insert(latestChatHistoryEntity);
            }
        }).start();
        //发送消息
    }

    /**构造一个最新的LatestChatHistoryEntity
     * @param sendMessageBean 更新消息列表需要的数据
     * message LatestChatHistoryEntity显示的内容
     * host LatestChatHistoryEntity记录的IP地址
     * receiverName LatestChatHistoryEntity记录的接收者名字
     * receiverIdentifier LatestChatHistoryEntity记录的接收者标识符
     * receiverAvatarPath LatestChatHistoryEntity记录的接收者头像地址
     * @return 构建的LatestChatHistoryEntity*/
    public static LatestChatHistoryEntity adapterToLatest(SendMessageBean sendMessageBean) {
        LatestChatHistoryEntity latestChatHistoryEntity = new LatestChatHistoryEntity();
        latestChatHistoryEntity.setContentTimeMill(System.currentTimeMillis());
        latestChatHistoryEntity.setContentTime(ChatRoomActivity.millsToTime(System.currentTimeMillis()));
        latestChatHistoryEntity.setContent(sendMessageBean.getMessage());
        latestChatHistoryEntity.setHost(sendMessageBean.getHost());
        latestChatHistoryEntity.setStatus(com.skysoft.smart.intranetchat.model.network.Config.STATUS_ONLINE);
        latestChatHistoryEntity.setUnReadNumber(0);
        latestChatHistoryEntity.setUserName(sendMessageBean.getName());
        latestChatHistoryEntity.setUserIdentifier(sendMessageBean.getReciever());
        latestChatHistoryEntity.setUserHeadPath(sendMessageBean.getAvatar());
        return latestChatHistoryEntity;
    }

    /**
     * 发送文件后更新消息界面
     * @param eventMessage 携带文件信息
     * @param sendMessageBean 携带接收者信息
     * @param refresh 是否刷新消息界面
     * @return 发送成功后返回消息记录*/
    public static ChatRecordEntity sendCommonMessage(EventMessage eventMessage, SendMessageBean sendMessageBean, boolean refresh){
        if (!IntranetChatApplication.isNetWortState()){
            ToastUtil.toast(IntranetChatApplication.getContext(), IntranetChatApplication.getContext().getString(R.string.Toast_text_non_lan));
            return null;
        }
        //记录文件
        ChatRecordEntity chatRecordEntity = new ChatRecordEntity();
        chatRecordEntity.setTime(System.currentTimeMillis());
        chatRecordEntity.setSender(IntranetChatApplication.getsMineUserInfo().getIdentifier());
        chatRecordEntity.setReceiver(sendMessageBean.getReciever());
        chatRecordEntity.setPath(eventMessage.getMessage());
        //发送文件
        Identifier identifier = new Identifier();
        String fileIdentifier = identifier.getFileIdentifier(eventMessage.getMessage());
        chatRecordEntity.setContent(fileIdentifier);
        ReceiveAndSaveFileBean rasfb = new ReceiveAndSaveFileBean();
        rasfb.setIdentifier(fileIdentifier);
        rasfb.setPath(eventMessage.getMessage());
        rasfb.setSender(IntranetChatApplication.getsMineUserInfo().getIdentifier());
        if (sendMessageBean.isGroup()){
            rasfb.setReceiver(sendMessageBean.getReciever());
        }else {
            rasfb.setReceiver(rasfb.getSender());
        }
        return handleEventMessage(eventMessage,chatRecordEntity,rasfb,sendMessageBean,refresh);
    }

    /**
     * 处理携带文件信息的EventMessage
     * @param eventMessage 携带文件信息
     * @param sendMessageBean 携带接收者信息
     * @param refresh 是否刷新消息界面
     * @return 发送成功后返回消息记录*/
    public static ChatRecordEntity handleEventMessage(EventMessage eventMessage, ChatRecordEntity chatRecordEntity, ReceiveAndSaveFileBean rasfb, SendMessageBean sendMessageBean,boolean refresh) {
        int type = 0;
        int isReceive = 0;
        String content = null;
        int fileType = 0;
        switch (eventMessage.getType()) {
            case 1:
                type = ChatRoomConfig.RECORD_IMAGE;
                isReceive = ChatRoomConfig.SEND_IMAGE;
                content = IntranetChatApplication.getContext().getString(R.string.image);
                fileType = Config.FILE_PICTURE;
                break;
            case 2:
                type = ChatRoomConfig.RECORD_VIDEO;
                isReceive = ChatRoomConfig.SEND_VIDEO;
                content = IntranetChatApplication.getContext().getString(R.string.video);
                fileType = Config.FILE_VIDEO;
                chatRecordEntity.setPath(ChatRoomMessageAdapter.createVideoThumbnailFile(chatRecordEntity.getPath()));
                chatRecordEntity.setFileName(eventMessage.getMessage());
                break;
            case 3:
                type = ChatRoomConfig.RECORD_VOICE;
                isReceive = ChatRoomConfig.SEND_VOICE;
                content = IntranetChatApplication.getContext().getString(R.string.voice);
                fileType = Config.FILE_VOICE;
                break;
            case 4:
                type = ChatRoomConfig.RECORD_FILE;
                isReceive = ChatRoomConfig.SEND_FILE;
                content = IntranetChatApplication.getContext().getString(R.string.file);
                fileType = Config.FILE_COMMON;
                break;
            default:
                return null;
        }
        chatRecordEntity.setType(type);
        chatRecordEntity.setIsReceive(isReceive);
        chatRecordEntity.setLength((int) eventMessage.getLength());
        //记录文件
        FileEntity fileEntity = new FileEntity();
        fileEntity.setPath(eventMessage.getMessage());
        fileEntity.setIdentifier(rasfb.getIdentifier());
        switch (eventMessage.getType()) {
            case 1:
                fileEntity.setType(Config.FILE_PICTURE);
                break;
            case 2:
                fileEntity.setType(Config.FILE_VIDEO);
                break;
            case 4:
                fileEntity.setType(Config.FILE_COMMON);
                break;
        }
        File file = new File(eventMessage.getMessage());
        fileEntity.setFileName(file.getName());
        if (eventMessage.getType() == 4){
            chatRecordEntity.setLength(file.length());
            chatRecordEntity.setFileName(fileEntity.getFileName());
        }
        //发送录音文件
        SendFile sendFile = new SendFile();
        TLog.d(TAG, "handleEventMessage: fileType = " + fileType);
        int b = 0;
        if (sendMessageBean.isGroup()){
            b = sendFile.sendFile(rasfb, "255.255.255.255", fileType, (int) eventMessage.getLength());
        }else {
            b = sendFile.sendFile(rasfb, sendMessageBean.getHost(), fileType, (int) eventMessage.getLength());
        }
        if (b == Config.SEND_SUCCESS){
            //存储文件
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //存储图片和视频
                    if (chatRecordEntity.getType() != ChatRoomConfig.RECORD_VOICE) {
                        MyDataBase.getInstance().getFileDao().insert(fileEntity);
                    }
                }
            }).start();
            //刷新最近聊天
            if (refresh){
                sendMessageBean.setMessage(content);
                refreshMessageFragment(sendMessageBean);
            }
        }else if (b == Config.SEND_FILE_BIG){
            ToastUtil.toast(IntranetChatApplication.getContext(),IntranetChatApplication.getContext().getString(R.string.file_size_too_big));
        }else if(b == Config.SEND_FILE_NOT_FOUND){
            ToastUtil.toast(IntranetChatApplication.getContext(),IntranetChatApplication.getContext().getString(R.string.file_not_found));
        }else if (b == Config.SEND_FILE_ZEOR){
            ToastUtil.toast(IntranetChatApplication.getContext(),IntranetChatApplication.getContext().getString(R.string.file_size_0B));
        }
        return chatRecordEntity;
    }
}
