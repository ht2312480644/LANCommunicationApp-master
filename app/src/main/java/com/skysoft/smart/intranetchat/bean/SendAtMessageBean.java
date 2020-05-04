package com.skysoft.smart.intranetchat.bean;

public class SendAtMessageBean extends SendMessageBean {
    private String[] mAt;

    public SendAtMessageBean(String mMessage, String mReceiver, String mHost,
                             String mAvatar, String mName, boolean isGroup,
                             String[] mAt) {
        super(mMessage, mReceiver, mHost, mAvatar, mName, isGroup);
        this.mAt = mAt;
    }

    public String[] getmAt() {
        return mAt;
    }

    public void setmAt(String[] mAt) {
        this.mAt = mAt;
    }
}
