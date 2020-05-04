/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Voice or video communication function
 */
package com.skysoft.smart.intranetchat.app.impl;

import com.skysoft.smart.intranetchat.model.network.bean.UserInfoBean;

public interface HandleInfo {
    void onReceiveVoiceCall(UserInfoBean userInfoBean,String host);

    void onReceiveVideoCall(UserInfoBean userInfoBean, String host);

    void onReceiveAndSaveFile(String sender,String receiver,String identifier,String path);

    void onReceiveFile(String fileJson, String host);
}
