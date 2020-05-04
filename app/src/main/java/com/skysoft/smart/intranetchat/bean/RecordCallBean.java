/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/14
 * Description: [Intranet Chat] [APP][UI] Group chat
 */
package com.skysoft.smart.intranetchat.bean;

public class RecordCallBean {
    private String identifier;
    private int type;
    private String host;
    private boolean voice;

    public RecordCallBean() {
    }

    public RecordCallBean(String identifier, int type, String host, boolean voice) {
        this.identifier = identifier;
        this.type = type;
        this.host = host;
        this.voice = voice;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isVoice() {
        return voice;
    }

    public void setVoice(boolean voice) {
        this.voice = voice;
    }
}
