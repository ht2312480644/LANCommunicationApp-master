/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/4
 * Description: [Intranet Chat] [APP][UI] Chat Room
 */
package com.skysoft.smart.intranetchat.model.network.bean;

public class ReceiveAndSaveFileBean {
    private String sender;
    private String receiver;
    private String identifier;
    private String path;
    private String host;

    public ReceiveAndSaveFileBean() {
    }

    public ReceiveAndSaveFileBean(String sender, String receiver, String identifier, String path) {
        this.sender = sender;
        this.receiver = receiver;
        this.identifier = identifier;
        this.path = path;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "ReceiveAndSaveFileBean{" +
                "sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", identifier='" + identifier + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
