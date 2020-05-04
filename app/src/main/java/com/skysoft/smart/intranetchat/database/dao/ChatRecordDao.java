/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/30
 * Description: [PT-48][Intranet Chat] [APP][Database] Create Database
 */
package com.skysoft.smart.intranetchat.database.dao;


import com.skysoft.smart.intranetchat.database.table.ChatRecordEntity;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomConfig;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ChatRecordDao {
    @Insert
    void insert(ChatRecordEntity... entities);
////
    @Update
    void update(ChatRecordEntity... entities);

    @Query("select * from chat_record where receiver = :identifier limit :page,:num")
    List<ChatRecordEntity> getAll(String identifier,int page,int num);

    @Query("select count(*) from chat_record where receiver = :identifier")
    int getNumber(String identifier);

    @Query("select * from chat_record where receiver = :userIdentifier and content = :fileIdentifier")
    ChatRecordEntity getRecordByFileIdentifier(String userIdentifier,String fileIdentifier);

    @Query("select time from chat_record where receiver =:userIdentifier order by time desc limit 1")
    long getLatestRecordTime(String userIdentifier);

    @Delete
    void delete(ChatRecordEntity... entities);

    @Query("select * from chat_record where receiver =:userIdentifier and time <=:time and id < :id order by time desc limit 1")
    ChatRecordEntity getLatestRecordBeforeTime(String userIdentifier,long time,int id);
}
