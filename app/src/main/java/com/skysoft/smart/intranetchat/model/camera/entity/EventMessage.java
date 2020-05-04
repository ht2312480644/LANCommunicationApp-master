/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Liu on 2019/10/28
 * Description: PG1-Smart Team-CT PT-29 [MM] Viewing Pictures Coding
 ***/
package com.skysoft.smart.intranetchat.model.camera.entity;


public class EventMessage {
    /**
     * 消息 例如：url
     */
    private String message;
    /**
     * 类型： 1.拍照url  2.视频url  3.音频url 4.头像url
     */
    private int type;
    /**
     * 时长
     */
    private long length;

    public EventMessage(String message, long length, int type) {
        this.message = message;
        this.length = length;
        this.type = type;
    }

    public EventMessage(String message,int type) {
        this.message = message;
        this.type = type;
    }

    public EventMessage() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
}
