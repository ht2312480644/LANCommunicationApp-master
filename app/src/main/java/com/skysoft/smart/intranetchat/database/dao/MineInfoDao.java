/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/30
 * Description: [PT-48][Intranet Chat] [APP][Database] Create Database
 */
package com.skysoft.smart.intranetchat.database.dao;


import com.skysoft.smart.intranetchat.database.table.MineInfoEntity;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface MineInfoDao {

    @Insert
    void insert(MineInfoEntity entities);

    @Update
    void update(MineInfoEntity... entities);

    @Query("SELECT * FROM mine_info")
    MineInfoEntity Query();
}
