/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/6
 * Description: [Intranet Chat] [APP][UI] Group chat
 */
package com.skysoft.smart.intranetchat.database.dao;

import com.skysoft.smart.intranetchat.database.table.GroupEntity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface GroupDao {
    @Insert
    void insert(GroupEntity groupEntity);

    @Update
    void update(GroupEntity... groupEntities);

    @Delete
    void delete(GroupEntity... groupEntities);

    @Query("select * from group_entity")
    List<GroupEntity> getAllGroup();

    @Query("select * from group_entity where group_identifier =:identifier")
    GroupEntity getGroupEntity(String identifier);
}
