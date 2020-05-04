/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication] Create a new process service with message sending and receiving.
 */
package com.skysoft.smart.intranetchat.model.network.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class UserInfoBean implements Parcelable {
    /*用户唯一标识符*/
    private String identifier;
    private String name;
    /*用户头像唯一标识符*/
    private String avatarIdentifier;
    /*用户状态*/
    private int status;
    private String remark;
    private int monitor;
    private int beMonitored;

    public int getMonitor() {
        return monitor;
    }

    public void setMonitor(int monitor) {
        this.monitor = monitor;
    }

    public int getBeMonitored() {
        return beMonitored;
    }

    public void setBeMonitored(int beMonitored) {
        this.beMonitored = beMonitored;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarIdentifier() {
        return avatarIdentifier;
    }

    public void setAvatarIdentifier(String avatarIdentifier) {
        this.avatarIdentifier = avatarIdentifier;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "UserInfoBean{" +
                "identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                ", avatarIdentifier='" + avatarIdentifier + '\'' +
                ", status=" + status +
                ", remark='" + remark + '\'' +
                '}';
    }

    public UserInfoBean() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.identifier);
        dest.writeString(this.name);
        dest.writeString(this.avatarIdentifier);
        dest.writeInt(this.status);
        dest.writeString(this.remark);
        dest.writeInt(this.monitor);
        dest.writeInt(this.beMonitored);
    }

    protected UserInfoBean(Parcel in) {
        this.identifier = in.readString();
        this.name = in.readString();
        this.avatarIdentifier = in.readString();
        this.status = in.readInt();
        this.remark = in.readString();
        this.monitor = in.readInt();
        this.beMonitored = in.readInt();
    }

    public static final Creator<UserInfoBean> CREATOR = new Creator<UserInfoBean>() {
        @Override
        public UserInfoBean createFromParcel(Parcel source) {
            return new UserInfoBean(source);
        }

        @Override
        public UserInfoBean[] newArray(int size) {
            return new UserInfoBean[size];
        }
    };
}
