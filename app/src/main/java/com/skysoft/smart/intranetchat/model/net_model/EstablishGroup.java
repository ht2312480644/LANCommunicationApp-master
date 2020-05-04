package com.skysoft.smart.intranetchat.model.net_model;

import android.os.RemoteException;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.database.MyDataBase;
import com.skysoft.smart.intranetchat.database.table.ContactEntity;
import com.skysoft.smart.intranetchat.database.table.GroupEntity;
import com.skysoft.smart.intranetchat.database.table.GroupMemberEntity;
import com.skysoft.smart.intranetchat.database.table.LatestChatHistoryEntity;
import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.model.network.bean.EstablishGroupBean;
import com.skysoft.smart.intranetchat.model.network.bean.UserInfoBean;
import com.skysoft.smart.intranetchat.tools.GsonTools;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EstablishGroup {

    private static String TAG = EstablishGroup.class.getSimpleName();
    public static void establishGroup(EstablishGroupBean establishGroupBean,List<String> hostList){
        try {
            TLog.d(TAG, "establishGroup: ");
            IntranetChatApplication.sAidlInterface.establishGroup(GsonTools.toJson(establishGroupBean),hostList);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static UserInfoBean adapter(ContactEntity contactEntity){
        UserInfoBean userInfoBean = new UserInfoBean();
        userInfoBean.setAvatarIdentifier(contactEntity.getAvatarIdentifier());
        userInfoBean.setName(contactEntity.getName());
        userInfoBean.setIdentifier(contactEntity.getIdentifier());
        userInfoBean.setStatus(contactEntity.getStatus());
        return userInfoBean;
    }

    public static LatestChatHistoryEntity LatestChatFromGroup(EstablishGroupBean establishGroupBean){
        LatestChatHistoryEntity latestChatHistoryEntity = new LatestChatHistoryEntity();
        latestChatHistoryEntity.setUserName(establishGroupBean.getmGroupName());
        latestChatHistoryEntity.setStatus(Config.STATUS_GROUP);
        latestChatHistoryEntity.setContentTimeMill(System.currentTimeMillis());
        latestChatHistoryEntity.setContentTime(ChatRoomActivity.millsToTime(System.currentTimeMillis()));
        latestChatHistoryEntity.setUserIdentifier(establishGroupBean.getmGroupIdentifier());
        latestChatHistoryEntity.setUserHeadIdentifier(establishGroupBean.getmGroupAvatarIdentifier());
        return latestChatHistoryEntity;
    }

    public static GroupEntity groupFromEstablish(EstablishGroupBean establishGroupBean){
        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setGroupHolder(establishGroupBean.getmHolderIdentifier());
        groupEntity.setGroupIdentifier(establishGroupBean.getmGroupIdentifier());
        groupEntity.setMemberNumber(establishGroupBean.getmUsers().size());
        return groupEntity;
    }

    public static ContactEntity contactFromEstablish(EstablishGroupBean establishGroupBean){
        ContactEntity contactEntity = new ContactEntity();
        contactEntity.setAvatarIdentifier(establishGroupBean.getmGroupAvatarIdentifier());
        contactEntity.setIdentifier(establishGroupBean.getmGroupIdentifier());
        contactEntity.setGroup(1);
        contactEntity.setName(establishGroupBean.getmGroupName());
        return contactEntity;
    }

    public static GroupMemberEntity[] groupMembersFromEstablish(EstablishGroupBean establishGroupBean){
        if (establishGroupBean.getmUsers().size() == 0){
            return null;
        }
        GroupMemberEntity[] groupMemberEntities = new GroupMemberEntity[establishGroupBean.getmUsers().size()];
        int i = 0;
        Iterator<UserInfoBean> iterator = establishGroupBean.getmUsers().iterator();
        while (iterator.hasNext()){
            UserInfoBean next = iterator.next();
            GroupMemberEntity groupMemberEntity = new GroupMemberEntity();
            groupMemberEntity.setGroupIdentifier(establishGroupBean.getmGroupIdentifier());
            groupMemberEntity.setGroupMemberIdentifier(next.getIdentifier());
            groupMemberEntities[i++] = groupMemberEntity;
        }
        return groupMemberEntities;
    }

    public static void storageGroup(LatestChatHistoryEntity latestChatHistoryEntity, EstablishGroupBean establishGroupBean,boolean groupExits,boolean latestChatExist){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //刷新最近聊天数据表
                MyDataBase.getInstance().getLatestChatHistoryDao().insert(latestChatHistoryEntity);
                if (!latestChatExist && !groupExits){
                    //刷新群数据表
                    GroupEntity groupEntity = EstablishGroup.groupFromEstablish(establishGroupBean);
                    MyDataBase.getInstance().getGroupDao().insert(groupEntity);
                    //刷新联系数据表
                    ContactEntity contactEntity = EstablishGroup.contactFromEstablish(establishGroupBean);
                    MyDataBase.getInstance().getContactDao().insert(contactEntity);
                    //刷新群成员数据表
                    GroupMemberEntity[] groupMembers = EstablishGroup.groupMembersFromEstablish(establishGroupBean);
                    MyDataBase.getInstance().getGroupMemberDao().insert(groupMembers);
                }
            }
        }).start();
    }

    public static void addGroupToList(EstablishGroupBean establishGroupBean){
        ContactEntity contactEntity = new ContactEntity();
        contactEntity.setIdentifier(establishGroupBean.getmGroupIdentifier());
        contactEntity.setName(establishGroupBean.getmGroupName());
        contactEntity.setGroup(1);
        contactEntity.setAvatarPath(establishGroupBean.getmGroupAvatarIdentifier());
        contactEntity.setNotifyId((int) (System.currentTimeMillis() - IntranetChatApplication.getsBaseTimeLine()));
        IntranetChatApplication.getsGroupContactList().add(contactEntity.getIdentifier());
        IntranetChatApplication.sGroupContactMap.put(contactEntity.getIdentifier(),contactEntity);
    }

    public static void establishGroup(EstablishGroupBean establishGroupBean,String host){
        List<String> hostList = new ArrayList<>();
        hostList.add(host);
        establishGroup(establishGroupBean,hostList);
    }
}
