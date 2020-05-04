/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/30
 * Description: [PT-48][Intranet Chat] [APP][Database] Create Database
 */
package com.skysoft.smart.intranetchat.database;

import android.content.Context;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.database.dao.ContactDao;
import com.skysoft.smart.intranetchat.database.dao.FileDao;
import com.skysoft.smart.intranetchat.database.dao.GroupDao;
import com.skysoft.smart.intranetchat.database.dao.GroupMemberDao;
import com.skysoft.smart.intranetchat.database.dao.LatestChatHistoryDao;
import com.skysoft.smart.intranetchat.database.dao.MineInfoDao;
import com.skysoft.smart.intranetchat.database.dao.ChatRecordDao;
import com.skysoft.smart.intranetchat.database.dao.EquipmentInfoDao;
import com.skysoft.smart.intranetchat.database.dao.RefuseGroupDao;
import com.skysoft.smart.intranetchat.database.table.ContactEntity;
import com.skysoft.smart.intranetchat.database.table.FileEntity;
import com.skysoft.smart.intranetchat.database.table.GroupEntity;
import com.skysoft.smart.intranetchat.database.table.GroupMemberEntity;
import com.skysoft.smart.intranetchat.database.table.LatestChatHistoryEntity;
import com.skysoft.smart.intranetchat.database.table.MineInfoEntity;
import com.skysoft.smart.intranetchat.database.table.ChatRecordEntity;
import com.skysoft.smart.intranetchat.database.table.EquipmentInfoEntity;
import com.skysoft.smart.intranetchat.database.table.RefuseGroupEntity;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {ChatRecordEntity.class, MineInfoEntity.class, ContactEntity.class, LatestChatHistoryEntity.class
        , FileEntity.class, GroupEntity.class, GroupMemberEntity.class
        , EquipmentInfoEntity.class, RefuseGroupEntity.class},version = 1,exportSchema = false)
public abstract class MyDataBase extends RoomDatabase {
    private static final String DB_NAME = "chat.db";
    public MyDataBase(){}
    public static MyDataBase sInstance;
    public static void init(Context context){
        TLog.d("MyDatabase", "init: ");
        sInstance = Room.databaseBuilder(context, MyDataBase.class,DB_NAME).build();
    }

    public static MyDataBase getInstance(){
        if (sInstance == null){
            throw new NullPointerException("sInstance == null");
        }
        return sInstance;
    }

    public abstract ChatRecordDao getChatRecordDao();
    public abstract MineInfoDao getMineInfoDao();
    public abstract ContactDao getContactDao();
    public abstract LatestChatHistoryDao getLatestChatHistoryDao();
    //B:[Intranet Chat] [APP][UI] Chat Room,Oliver Ou,2019/11/4
    public abstract FileDao getFileDao();
    //E:[Intranet Chat] [APP][UI] Chat Room,Oliver Ou,2019/11/4
    //B:[Intranet Chat] [APP][UI] Group chat,Oliver Ou,2019/11/6
    public abstract GroupDao getGroupDao();
    public abstract GroupMemberDao getGroupMemberDao();
    //E:[Intranet Chat] [APP][UI] Group chat,Oliver Ou,2019/11/6
    public abstract EquipmentInfoDao getEquipmentInfoDaoDao();
    public abstract RefuseGroupDao getRefuseGroupDao();
}
