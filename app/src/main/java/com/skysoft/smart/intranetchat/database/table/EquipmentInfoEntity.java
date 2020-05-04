/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/11
 * Description: [Intranet Chat] [APP][UI] Group chat
 */
package com.skysoft.smart.intranetchat.database.table;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "equipment_info")
public class EquipmentInfoEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "soft_input_height")
    private int softInputHeight;
    @ColumnInfo
    private String mac;
    @ColumnInfo(name = "screen_size")
    private int screenSize;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSoftInputHeight() {
        return softInputHeight;
    }

    public void setSoftInputHeight(int softInputHeight) {
        this.softInputHeight = softInputHeight;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Override
    public String toString() {
        return "EquipmentInfoEntity{" +
                "id=" + id +
                ", softInputHeight=" + softInputHeight +
                ", mac='" + mac + '\'' +
                ", screenSize=" + screenSize +
                '}';
    }

    public int getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(int screenSize) {
        this.screenSize = screenSize;
    }

}
