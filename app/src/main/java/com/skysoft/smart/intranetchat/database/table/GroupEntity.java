/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/6
 * Description: [Intranet Chat] [APP][UI] Group chat
 */
package com.skysoft.smart.intranetchat.database.table;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "group_entity",primaryKeys = "group_identifier",indices = @Index(value = "group_identifier",unique = true))
public class GroupEntity {
    @ColumnInfo(name = "group_identifier")
    @NonNull
    private String groupIdentifier;
    @ColumnInfo(name = "group_avatar_holder")
    private String groupHolder;
    @ColumnInfo(name = "member_number")
    private int memberNumber;

    public String getGroupIdentifier() {
        return groupIdentifier;
    }

    public void setGroupIdentifier(String groupIdentifier) {
        this.groupIdentifier = groupIdentifier;
    }

    public String getGroupHolder() {
        return groupHolder;
    }

    public void setGroupHolder(String groupHolder) {
        this.groupHolder = groupHolder;
    }

    public int getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(int memberNumber) {
        this.memberNumber = memberNumber;
    }

    @Override
    public String toString() {
        return "GroupEntity{" +
                ", groupIdentifier='" + groupIdentifier + '\'' +
                ", groupHolder='" + groupHolder + '\'' +
                ", memberNumber=" + memberNumber +
                '}';
    }
}
