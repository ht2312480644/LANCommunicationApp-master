/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/11
 * Description: [Intranet Chat] [APP][UI] Group chat
 */
package com.skysoft.smart.intranetchat.database.dao;

import com.skysoft.smart.intranetchat.database.table.EquipmentInfoEntity;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface EquipmentInfoDao {
    @Update
    void update(EquipmentInfoEntity entity);

    @Insert
    void insert(EquipmentInfoEntity entity);

    @Query("select * from equipment_info")
    EquipmentInfoEntity getEquipmentInfo();
}
