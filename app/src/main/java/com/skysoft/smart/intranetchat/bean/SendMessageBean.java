package com.skysoft.smart.intranetchat.bean;

/**封装发送消息时需要的数据*/
public class SendMessageBean {
    private String mMessage;
    private String mReceiver;
    private String mHost;
    private String mAvatar;
    private String mName;

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    private boolean isGroup;

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public String getReciever() {
        return mReceiver;
    }

    public void setReciever(String reciever) {
        this.mReceiver = reciever;
    }

    public String getHost() {
        return mHost;
    }

    public void setHost(String host) {
        this.mHost = host;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public void setAvatar(String avatar) {
        this.mAvatar = avatar;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public SendMessageBean(String mMessage, String mReceiver, String mHost, String mAvatar, String mName, boolean isGroup) {
        this.mMessage = mMessage;
        this.mReceiver = mReceiver;
        this.mHost = mHost;
        this.mAvatar = mAvatar;
        this.mName = mName;
        this.isGroup = isGroup;
    }
}
