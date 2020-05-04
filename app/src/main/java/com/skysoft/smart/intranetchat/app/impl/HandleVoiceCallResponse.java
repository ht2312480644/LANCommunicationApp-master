/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Voice or video communication function
 */
package com.skysoft.smart.intranetchat.app.impl;

public interface HandleVoiceCallResponse {
    void onReceiveConsentVoiceCall(String host);

    void onReceiveRefuseVoiceCall(String host);

    void onReceiveWaitingConsentCall();

    void onReceiveConsentOutTime();

    void onReceiveInCall(String host);
}
