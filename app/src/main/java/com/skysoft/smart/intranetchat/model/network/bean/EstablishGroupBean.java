/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/6
 * Description: [Intranet Chat] [APP][UI] Group chat
 */
package com.skysoft.smart.intranetchat.model.network.bean;

import java.util.List;

public class EstablishGroupBean {
    private String mName;
    private String mHolderIdentifier;
    private String mGroupAvatarIdentifier;
    private String mGroupIdentifier;
    private List<UserInfoBean> mUsers;
    private boolean remark;

    public boolean isRemark() {
        return remark;
    }

    public void setRemark(boolean remark) {
        this.remark = remark;
    }

    public String getmGroupIdentifier() {
        return mGroupIdentifier;
    }

    public String getmGroupName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmHolderIdentifier() {
        return mHolderIdentifier;
    }

    public void setmHolderIdentifier(String mHolderIdentifier) {
        this.mHolderIdentifier = mHolderIdentifier;
    }

    public String getmGroupAvatarIdentifier() {
        return mGroupAvatarIdentifier;
    }

    public void setmGroupAvatarIdentifier(String mGroupAvatarIdentifier) {
        this.mGroupAvatarIdentifier = mGroupAvatarIdentifier;
    }

    public void setmGroupIdentifier(String mGroupIdentifier) {
        this.mGroupIdentifier = mGroupIdentifier;
    }

    public List<UserInfoBean> getmUsers() {
        return mUsers;
    }

    public void setmUsers(List<UserInfoBean> mUsers) {
        this.mUsers = mUsers;
    }

    @Override
    public String toString() {
        return "EstablishGroupBean{" +
                "mName='" + mName + '\'' +
                ", mHolderIdentifier='" + mHolderIdentifier + '\'' +
                ", mGroupAvatarIdentifier='" + mGroupAvatarIdentifier + '\'' +
                ", mGroupIdentifier='" + mGroupIdentifier + '\'' +
                ", mUsers=" + mUsers +
                '}';
    }
}
