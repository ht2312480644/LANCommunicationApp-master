/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/30
 * Description: [PT-48][Intranet Chat] [APP][Database] Create Database
 */
package com.skysoft.smart.intranetchat.database.table;

import androidx.annotation.IntegerRes;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "latest_chat_history",primaryKeys = "user_identifier")
public class LatestChatHistoryEntity {
    //0为不置顶，1为置顶。
    @ColumnInfo(name = "top")
    private int top = 0;
    @ColumnInfo(name = "user_name")
    private String userName;
    @ColumnInfo(name = "user_identifier")
    @NonNull
    private String userIdentifier;
    @ColumnInfo(name = "sender_identifier")
    private String senderIdentifier;
    @ColumnInfo(name = "user_head_path")
    private String userHeadPath;
    @ColumnInfo(name = "user_head_identifier")
    private String userHeadIdentifier;
    @ColumnInfo(name = "content_time")
    private String contentTime;
    @ColumnInfo(name = "content_time_mill")
    private long contentTimeMill;
    @ColumnInfo(name = "content")
    private String content;
    @ColumnInfo(name = "un_read_number")
    private int unReadNumber;
    @ColumnInfo
    private int group;      //单聊为0，群聊为1
    @Ignore
    private int status;
    //B:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/11/4
    @ColumnInfo
    //E:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/11/4
    private String host;
    //B:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/11/4
    @Ignore
    private int type;
    @Ignore
    private long length;
    //E:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/11/4

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public String getUserHeadPath() {
        return userHeadPath;
    }

    public void setUserHeadPath(String userHeadPath) {
        this.userHeadPath = userHeadPath;
    }

    public String getUserHeadIdentifier() {
        return userHeadIdentifier;
    }

    public void setUserHeadIdentifier(String userHeadIdentifier) {
        this.userHeadIdentifier = userHeadIdentifier;
    }

    public String getContentTime() {
        return contentTime;
    }

    public void setContentTime(String contentTime) {
        this.contentTime = contentTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getUnReadNumber() {
        return unReadNumber;
    }

    public void setUnReadNumber(int unReadNumber) {
        this.unReadNumber = unReadNumber;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    //B:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/11/4
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    //E:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/11/4

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public String getSenderIdentifier() {
        return senderIdentifier;
    }

    public void setSenderIdentifier(String senderIdentifier) {
        this.senderIdentifier = senderIdentifier;
    }

    public long getContentTimeMill() {
        return contentTimeMill;
    }

    public void setContentTimeMill(long contentTimeMill) {
        this.contentTimeMill = contentTimeMill;
    }

    @Override
    public String toString() {
        return "LatestChatHistoryEntity{" +
                "top=" + top +
                ", userName='" + userName + '\'' +
                ", userIdentifier='" + userIdentifier + '\'' +
                ", senderIdentifier='" + senderIdentifier + '\'' +
                ", userHeadPath='" + userHeadPath + '\'' +
                ", userHeadIdentifier='" + userHeadIdentifier + '\'' +
                ", contentTime='" + contentTime + '\'' +
                ", contentTimeMill=" + contentTimeMill +
                ", content='" + content + '\'' +
                ", unReadNumber=" + unReadNumber +
                ", group=" + group +
                ", status=" + status +
                ", host='" + host + '\'' +
                ", type=" + type +
                ", length=" + length +
                '}';
    }
}
