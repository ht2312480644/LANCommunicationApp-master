/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/30
 * Description: [PT-48][Intranet Chat] [APP][Database] Create Database
 */
package com.skysoft.smart.intranetchat.database.dao;

import com.skysoft.smart.intranetchat.database.table.LatestChatHistoryEntity;
import com.skysoft.smart.intranetchat.database.table.MineInfoEntity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface LatestChatHistoryDao {

    @Insert
    void insert(LatestChatHistoryEntity... entities);

    @Update
    void update(LatestChatHistoryEntity... entities);

    @Delete
    void delete(LatestChatHistoryEntity... entities);

    //B:[Intranet Chat] [APP][UI] Chat Room, Oliver Ou, 2019/10/31
    @Query("SELECT * FROM latest_chat_history")
    List<LatestChatHistoryEntity> getAllHistory();
    //E:[Intranet Chat] [APP][UI] Chat Room, Oliver Ou, 2019/10/31

    @Query("select * from latest_chat_history where user_identifier =:identifier")
    LatestChatHistoryEntity getHistory(String identifier);
}
