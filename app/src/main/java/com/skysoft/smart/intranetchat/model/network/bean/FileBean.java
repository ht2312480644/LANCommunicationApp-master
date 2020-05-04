/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description:[Intranet Chat] [APP] [Communication]File message sending and receiving (excluding file data transfer).
 */
package com.skysoft.smart.intranetchat.model.network.bean;

public class FileBean {
    private String fileName;
    private String md5;
    private long fileLength;
    private String fileUniqueIdentifier;
    private String sender;
    private String receiver;
    private int type;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getFileUniqueIdentifier() {
        return fileUniqueIdentifier;
    }

    public void setFileUniqueIdentifier(String fileUniqueIdentifier) {
        this.fileUniqueIdentifier = fileUniqueIdentifier;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "FileBean{" +
                "fileName='" + fileName + '\'' +
                ", md5='" + md5 + '\'' +
                ", fileLength=" + fileLength +
                ", fileUniqueIdentifier='" + fileUniqueIdentifier + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", type=" + type +
                '}';
    }
}
