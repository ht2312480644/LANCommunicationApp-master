/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/6
 * Description: [Intranet Chat] [APP][UI] Group chat
 */
package com.skysoft.smart.intranetchat.database.dao;

import com.skysoft.smart.intranetchat.database.table.GroupMemberEntity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface GroupMemberDao {
    @Insert
    void insert(GroupMemberEntity... groupMemberEntities);

    @Update
    void update(GroupMemberEntity... groupMemberEntities);

    @Delete
    void delete(GroupMemberEntity... groupMemberEntities);

    @Query("select * from group_member where group_identifier = :groupIdentifier")
    List<GroupMemberEntity> getAllGroupMember(String groupIdentifier);

    @Query("select * from group_member where group_identifier =:groupIdentifier and group_member_identifier =:memberIdentifier")
    GroupMemberEntity getGroupMember(String groupIdentifier,String memberIdentifier);
}
