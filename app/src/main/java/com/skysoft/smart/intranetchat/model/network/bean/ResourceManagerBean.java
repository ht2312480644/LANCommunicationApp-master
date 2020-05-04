/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Create a file service for file recipients to download files.
 */
package com.skysoft.smart.intranetchat.model.network.bean;

import java.util.ArrayList;
import java.util.List;

public class ResourceManagerBean {
    private FileBean fileBean;
    private String path;
    private boolean group;
    private List<String> downLoadHostList = new ArrayList<>();
    private boolean receive;

    public ResourceManagerBean(FileBean fileBean, String path) {
        this.fileBean = fileBean;
        this.path = path;
    }

    public FileBean getFileBean() {
        return fileBean;
    }

    public void setFileBean(FileBean fileBean) {
        this.fileBean = fileBean;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isReceive() {
        return receive;
    }

    public void setReceive(boolean receive) {
        this.receive = receive;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public int getDownLoadNum(){
        return downLoadHostList.size();
    }

    public String getDownLoadList() {
        int size = downLoadHostList.size();
        int random = (int) (Math.random() * size);
        return downLoadHostList.get(random);
    }

    public void setDownLoadList(String host) {
        this.downLoadHostList.add(host);
    }
}
