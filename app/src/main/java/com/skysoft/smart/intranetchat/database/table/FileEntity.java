/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/4
 * Description: [Intranet Chat] [APP][UI] Chat Room
 */
package com.skysoft.smart.intranetchat.database.table;
//B:[Intranet Chat] [APP][UI] Chat Room,Oliver Ou,2019/11/4
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "file_entity",indices = @Index(value = {"identifier","sender","step"}),primaryKeys = "identifier")
public class FileEntity {
    @ColumnInfo
    private String path;
    @ColumnInfo(name = "file_name")
    private String fileName;
    @ColumnInfo
    @NonNull
    private String identifier;
    @ColumnInfo
    private String sender;
    @ColumnInfo
    private String receiver;
    @ColumnInfo
    private int step;
    @ColumnInfo
    private int type;
    @ColumnInfo
    private String md5;
    @ColumnInfo
    private long time;

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

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    @Override
    public String toString() {
        return "FileEntity{" +
                ", path='" + path + '\'' +
                ", fileName='" + fileName + '\'' +
                ", identifier='" + identifier + '\'' +
                ", sender='" + sender + '\'' +
                ", step=" + step +
                ", type=" + type +
                ", md5='" + md5 + '\'' +
                '}';
    }
}
//E:[Intranet Chat] [APP][UI] Chat Room,Oliver Ou,2019/11/4
