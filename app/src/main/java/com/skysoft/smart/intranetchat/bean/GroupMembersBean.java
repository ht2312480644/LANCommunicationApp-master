/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/6
 * Description: [Intranet Chat] [APP][UI] Group chat
 */
package com.skysoft.smart.intranetchat.bean;

import java.util.List;

public class GroupMembersBean {
    private String mMemberAvatarPath;
    private String mMemberName;

    public String getmMemberAvatarPath() {
        return mMemberAvatarPath;
    }

    public void setmMemberAvatarPath(String mMemberAvatarPath) {
        this.mMemberAvatarPath = mMemberAvatarPath;
    }

    public String getmMemberName() {
        return mMemberName;
    }

    public void setmMemberName(String mMemberName) {
        this.mMemberName = mMemberName;
    }

    @Override
    public String toString() {
        return "GroupMembersBean{" +
                "mMemberAvatarPath='" + mMemberAvatarPath + '\'' +
                ", mMemberName='" + mMemberName + '\'' +
                '}';
    }
}
