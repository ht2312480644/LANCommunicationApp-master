/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/30
 * Description: [PT-48][Intranet Chat] [APP][Database] Create Database
 */
package com.skysoft.smart.intranetchat.database.table;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "contact_info",primaryKeys = "user_identifier",indices = {@Index(value = "user_identifier",unique = true)})
public class ContactEntity {
    //B:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/10/31
    @ColumnInfo(name = "user_name")
    private String name;
    @ColumnInfo(name = "user_identifier")
    @NonNull
    private String identifier;
    @ColumnInfo(name = "user_head_path")
    private String avatarPath;
    @ColumnInfo(name = "user_head_identifier")
    private String avatarIdentifier;
    @ColumnInfo
    private int group;
    @Ignore
    private int status = 3;
    @Ignore
    private String host;
    @Ignore
    private boolean check = false;
    @Ignore
    private int notifyId;
    @Ignore
    private boolean showCheck = true;

    public boolean isShowCheck() {
        return showCheck;
    }

    public void setShowCheck(boolean showCheck) {
        this.showCheck = showCheck;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public String getAvatarIdentifier() {
        return avatarIdentifier;
    }

    public void setAvatarIdentifier(String avatarIdentifier) {
        this.avatarIdentifier = avatarIdentifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    //EB:[Intranet Chat] [APP][UI] Group chat, Oliver Ou 2019/11/7
    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }
    //E:[Intranet Chat] [APP][UI] Group chat, Oliver Ou 2019/11/7
//E:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/10/31

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getNotifyId() {
        return notifyId;
    }

    public void setNotifyId(int notifyId) {
        this.notifyId = notifyId;
    }

    @Override
    public String toString() {
        return "ContactEntity{" +
                ", name='" + name + '\'' +
                ", identifier='" + identifier + '\'' +
                ", avatarPath='" + avatarPath + '\'' +
                ", avatarIdentifier='" + avatarIdentifier + '\'' +
                ", group=" + group +
                ", status=" + status +
                ", host='" + host + '\'' +
                ", check=" + check +
                ", notifyId=" + notifyId +
                '}';
    }
}
