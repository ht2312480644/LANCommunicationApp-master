/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/6
 * Description: [Intranet Chat] [APP][UI] Group chat
 */
package com.skysoft.smart.intranetchat.database.table;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "group_member",primaryKeys = {"group_identifier","group_member_identifier"}
        ,indices = @Index(value = "group_identifier"))
public class GroupMemberEntity {
    @ColumnInfo(name = "group_identifier")
    @NonNull
    private String groupIdentifier;
    @ColumnInfo(name = "group_member_identifier")
    @NonNull
    private String groupMemberIdentifier;
    @Ignore
    private String host;

    public String getGroupIdentifier() {
        return groupIdentifier;
    }

    public void setGroupIdentifier(String groupIdentifier) {
        this.groupIdentifier = groupIdentifier;
    }

    public String getGroupMemberIdentifier() {
        return groupMemberIdentifier;
    }

    public void setGroupMemberIdentifier(String groupMemberIdentifier) {
        this.groupMemberIdentifier = groupMemberIdentifier;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return "GroupMemberEntity{" +
                ", groupIdentifier='" + groupIdentifier + '\'' +
                ", groupMemberIdentifier='" + groupMemberIdentifier + '\'' +
                '}';
    }
}
