package com.skysoft.smart.intranetchat.bean;

/**
 * 转发消息时记载当时最近聊天*/
public class TransmitBean {
    private String mAvatarPath;
    private String mUseName;
    private String mUserIdentifier;
    private boolean isGroup;
    private String mHost;

    public TransmitBean(String mAvatarPath, String mUseName, String mUserIdentifier, boolean isGroup, String host) {
        this.mAvatarPath = mAvatarPath;
        this.mUseName = mUseName;
        this.mUserIdentifier = mUserIdentifier;
        this.isGroup = isGroup;
        this.mHost = host;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public String getmHost() {
        return mHost;
    }

    public void setmHost(String mHost) {
        this.mHost = mHost;
    }

    public String getmAvatarPath() {
        return mAvatarPath;
    }

    public void setmAvatarPath(String mAvatarPath) {
        this.mAvatarPath = mAvatarPath;
    }

    public String getmUseName() {
        return mUseName;
    }

    public void setmUseName(String mUseName) {
        this.mUseName = mUseName;
    }

    public String getmUserIdentifier() {
        return mUserIdentifier;
    }

    public void setmUserIdentifier(String mUserIdentifier) {
        this.mUserIdentifier = mUserIdentifier;
    }
}
