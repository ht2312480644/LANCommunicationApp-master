package com.skysoft.smart.intranetchat.bean;

public class SendReplayMessageBean extends SendAtMessageBean{
    public String replayContent;
    public String replayName;
    public int replayType;

    public SendReplayMessageBean(String mMessage, String mReceiver, String mHost, String mAvatar, String mName,
                                 boolean isGroup, String[] mAt, String replayContent, String replayName, int replayType ) {
        super(mMessage, mReceiver, mHost, mAvatar, mName, isGroup,mAt);
        this.replayContent = replayContent;
        this.replayName = replayName;
        this.replayType = replayType;
    }

    public String getReplayContent() {
        return replayContent;
    }

    public void setReplayContent(String replayContent) {
        this.replayContent = replayContent;
    }

    public String getReplayName() {
        return replayName;
    }

    public void setReplayName(String replayName) {
        this.replayName = replayName;
    }

    public int getReplayType() {
        return replayType;
    }

    public void setReplayType(int replayType) {
        this.replayType = replayType;
    }
}
