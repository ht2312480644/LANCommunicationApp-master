/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/31
 * Description: [Intranet Chat] [APP][UI] Chat Room
 */
package com.skysoft.smart.intranetchat.tools;

import android.os.RemoteException;
import android.text.TextUtils;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.database.table.ContactEntity;
import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.model.network.bean.AskResourceBean;
import com.skysoft.smart.intranetchat.model.network.bean.FileBean;
import com.skysoft.smart.intranetchat.model.network.bean.UserInfoBean;
import com.skysoft.smart.intranetchat.model.network.manager.PathManager;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class HandleReceivedUserInfo {

    private static String TAG = HandleReceivedUserInfo.class.getSimpleName();

    public static void handleReceivedUserInfo(UserInfoBean userInfo,String host){
        int index = isContainsUserIdentifier(userInfo.getIdentifier());
        if (index == -1){
            ContactEntity contactEntity = new ContactEntity();
            contactEntity.setIdentifier(userInfo.getIdentifier());
            contactEntity.setName(userInfo.getName());
            contactEntity.setStatus(1);
            contactEntity.setStatus(Config.STATUS_ONLINE);
            contactEntity.setAvatarIdentifier(userInfo.getAvatarIdentifier());
            contactEntity.setHost(host);
            EventBus.getDefault().post(contactEntity);
        }else {
            ContactEntity temp = IntranetChatApplication.sContactMap.get(IntranetChatApplication.getsContactList().get(index));

            ContactEntity contactEntity = new ContactEntity();
            contactEntity.setAvatarPath(temp.getAvatarPath());
            contactEntity.setAvatarIdentifier(temp.getAvatarIdentifier());
            contactEntity.setIdentifier(temp.getIdentifier());
            contactEntity.setName(temp.getName());

            contactEntity.setHost(host);
            //改状态
            if (userInfo.getStatus() == Config.STATUS_LOGIN){
                contactEntity.setStatus(Config.STATUS_ONLINE);
            }else {
                contactEntity.setStatus(userInfo.getStatus());
            }
            if (!TextUtils.isEmpty(IntranetChatApplication.getFilterIdentifier())){
                //更改名字
                if (!contactEntity.getName().equals(userInfo.getName())){
                    contactEntity.setName(userInfo.getName());
                }
                //更换头像
                if (!contactEntity.getAvatarIdentifier().equals(userInfo.getAvatarIdentifier())){
                    TLog.d(TAG, "handleReceivedUserInfo: change avatar");
                    IntranetChatApplication.setRequestAvatar(true);
                    if (!TextUtils.isEmpty(userInfo.getRemark())){
                        //请求头像
                        contactEntity.setAvatarIdentifier(userInfo.getAvatarIdentifier());
                        addResource(userInfo);
                    }else if (userInfo.getAvatarIdentifier().equals(new Identifier().getDefaultAvatarIdentifier())){
                        contactEntity.setAvatarIdentifier(userInfo.getAvatarIdentifier());
                    }
                    askAvatar(userInfo,host);
                }
            }
            EventBus.getDefault().post(contactEntity);
        }
    }

    public static int isContainsUserIdentifier(String identifier){
        if (TextUtils.isEmpty(identifier)){
            return -1;
        }
        return IntranetChatApplication.getsContactList().indexOf(identifier);
    }

    public static void askAvatar(UserInfoBean userInfo,String host){
        AskResourceBean askResourceBean = new AskResourceBean();
        askResourceBean.setResourceType(Config.RESOURCE_AVATAR);
        askResourceBean.setResourceUniqueIdentifier(userInfo.getAvatarIdentifier());
        try {
            IntranetChatApplication.sAidlInterface.askResource(GsonTools.toJson(askResourceBean),host);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void addResource(UserInfoBean userInfo){
        FileBean fileBean = new FileBean();
        fileBean.setType(Config.FILE_AVATAR);
        fileBean.setMd5(userInfo.getRemark());
        fileBean.setReceiver(userInfo.getIdentifier());
        fileBean.setSender(userInfo.getIdentifier());
        fileBean.setFileUniqueIdentifier(userInfo.getAvatarIdentifier());
        fileBean.setFileName(userInfo.getAvatarIdentifier() + ".jpg");
        String path = PathManager.fromType(Config.FILE_AVATAR);
        try {
            IntranetChatApplication.sAidlInterface.addResourceManagerBean(GsonTools.toJson(fileBean),path,true,true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
