/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/6
 * Description: [Intranet Chat] [APP][UI] Group chat
 */
package com.skysoft.smart.intranetchat.bean;

import com.skysoft.smart.intranetchat.database.table.GroupMemberEntity;

import java.util.List;

public class GroupMemberList {
    List<GroupMemberEntity> memberEntities;

    public GroupMemberList(List<GroupMemberEntity> memberEntities) {
        this.memberEntities = memberEntities;
    }

    public List<GroupMemberEntity> getMemberEntities() {
        return memberEntities;
    }

    public void setMemberEntities(List<GroupMemberEntity> memberEntities) {
        this.memberEntities = memberEntities;
    }
}
