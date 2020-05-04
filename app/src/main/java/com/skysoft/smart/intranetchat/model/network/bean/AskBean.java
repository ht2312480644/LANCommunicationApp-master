/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Create a UDP data wrapper and parser
 */
package com.skysoft.smart.intranetchat.model.network.bean;

public class AskBean {
    private int requestType;

    public int getRequestType() {
        return requestType;
    }

    public void setRequestType(int requestType) {
        this.requestType = requestType;
    }

    @Override
    public String toString() {
        return "AskBean{" +
                "requestType=" + requestType +
                '}';
    }
}
