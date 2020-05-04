/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/30
 * Description: [PT-48][Intranet Chat] [APP][Database] Create Database
 */
package com.skysoft.smart.intranetchat.database.table;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "mine_info")
public class MineInfoEntity {

    @PrimaryKey
    private int id;
    @ColumnInfo(name = "mine_name")
    private String mineName;
    @ColumnInfo(name = "mine_head_path")
    private String mineHeadPath;
    @ColumnInfo(name = "mine_head_identifier")
    private String mineHeadIdentifier;
    @ColumnInfo(name = "mine_identifier")
    private String mineIdentifier;

    public String getMineName() {
        return mineName;
    }

    public void setMineName(String mineName) {
        this.mineName = mineName;
    }

    public String getMineHeadPath() {
        return mineHeadPath;
    }

    public void setMineHeadPath(String mineHeadPath) {
        this.mineHeadPath = mineHeadPath;
    }

    public String getMineIdentifier() {
        return mineIdentifier;
    }

    public void setMineIdentifier(String mineIdentifier) {
        this.mineIdentifier = mineIdentifier;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMineHeadIdentifier() {
        return mineHeadIdentifier;
    }

    public void setMineHeadIdentifier(String mineHeadIdentifier) {
        this.mineHeadIdentifier = mineHeadIdentifier;
    }

    @Override
    public String toString() {
        return "MineInfoEntity{" +
                "id=" + id +
                ", mineName='" + mineName + '\'' +
                ", mineHeadPath='" + mineHeadPath + '\'' +
                ", mineHeadIdentifier='" + mineHeadIdentifier + '\'' +
                ", mineIdentifier='" + mineIdentifier + '\'' +
                '}';
    }
}
