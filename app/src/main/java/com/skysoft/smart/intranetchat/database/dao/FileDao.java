/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/4
 * Description: [Intranet Chat] [APP][UI] Chat Room
 */
package com.skysoft.smart.intranetchat.database.dao;

import com.skysoft.smart.intranetchat.database.table.FileEntity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface FileDao {
    @Insert
    void insert(FileEntity entity);

    @Update
    void update(FileEntity entity);

    @Update
    void updateList(List<FileEntity> entities);

    @Query("select * from file_entity")
    List<FileEntity> getAll();

    @Query("select * from file_entity where identifier = :identifier")
    List<FileEntity> getFileEntity(String identifier);

    @Query("select * from file_entity where step != 85 and time > :timeLine")
    List<FileEntity> getAllFailure(long timeLine);

    @Delete
    void delete(FileEntity... entities);
}
