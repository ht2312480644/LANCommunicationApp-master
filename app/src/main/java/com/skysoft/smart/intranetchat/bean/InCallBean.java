/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/13
 * Description: [Intranet Chat] [APP][UI] Group chat
 */
package com.skysoft.smart.intranetchat.bean;

public class InCallBean {
    private boolean isInCall;

    public boolean isInCall() {
        return isInCall;
    }

    public void setInCall(boolean inCall) {
        isInCall = inCall;
    }

    public InCallBean(boolean isInCall) {
        this.isInCall = isInCall;
    }
}
