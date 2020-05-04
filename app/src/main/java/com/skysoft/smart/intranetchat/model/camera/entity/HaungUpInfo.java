/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Jesse Ou on 2019/11/19
 * Description: [Intranet Chat] [APP]
 */
package com.skysoft.smart.intranetchat.model.camera.entity;

public class HaungUpInfo {
    private String host;
    private String identifier;
    private boolean answer;

    public HaungUpInfo(String host, String identifier, boolean answer) {
        this.host = host;
        this.identifier = identifier;
        this.answer = answer;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isAnswer() {
        return answer;
    }

    public void setAnswer(boolean answer) {
        this.answer = answer;
    }
}
