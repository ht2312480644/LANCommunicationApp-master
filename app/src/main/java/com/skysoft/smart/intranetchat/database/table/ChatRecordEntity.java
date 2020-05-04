/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/30
 * Description: [PT-48][Intranet Chat] [APP][Database] Create Database
 */
package com.skysoft.smart.intranetchat.database.table;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_record")
public class ChatRecordEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo
    private String receiver;
    @ColumnInfo
    private String sender;
    @ColumnInfo
    private long time;
    @ColumnInfo
    private int type;
    @ColumnInfo
    private String path;        //1：语音、图片、视频预览图地址；2：回复名 | 回复内容
    @ColumnInfo(name = "file_name")
    private String fileName;    //1：文件名；2：视频地址；3：@数组
    @ColumnInfo
    private long length;        //1：文件长度；2：回复类型
    @ColumnInfo
    private String content;
    @ColumnInfo(name = "is_receive")
    private int isReceive;
    //B:记录文件是否已经接收失败,Oliver Ou,2019/11/28
    @ColumnInfo(name = "receive_success")
    private boolean receiveSuccess = true;
    //E:记录文件是否已经接收失败,Oliver Ou,2019/11/28

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isReceiveSuccess() {
        return receiveSuccess;
    }

    public void setReceiveSuccess(boolean receiveSuccess) {
        this.receiveSuccess = receiveSuccess;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getIsReceive() {
        return isReceive;
    }

    public void setIsReceive(int isReceive) {
        this.isReceive = isReceive;
    }

    //B:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/11/4

    @Override
    public String toString() {
        return "ChatRecordEntity{" +
                "id=" + id +
                ", receiver='" + receiver + '\'' +
                ", sender='" + sender + '\'' +
                ", time=" + time +
                ", type=" + type +
                ", path='" + path + '\'' +
                ", fileName='" + fileName + '\'' +
                ", length=" + length +
                ", content='" + content + '\'' +
                ", isReceive=" + isReceive +
                '}';
    }

    //E:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/11/4

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
