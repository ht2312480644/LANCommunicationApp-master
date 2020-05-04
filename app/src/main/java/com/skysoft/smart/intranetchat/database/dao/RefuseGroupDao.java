/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/12
 * Description: [Intranet Chat] [APP][UI] Group chat
 */
package com.skysoft.smart.intranetchat.database.dao;

import com.skysoft.smart.intranetchat.database.table.RefuseGroupEntity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface RefuseGroupDao {
    @Update
    void update(RefuseGroupEntity entity);

    @Delete
    void delete(RefuseGroupEntity entity);

    @Insert
    void insert(RefuseGroupEntity entity);

    @Query("select * from refuse_group")
    List<RefuseGroupEntity> getAll();
}
