/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/30
 * Description: [Intranet Chat] [APP][UI] Chat Room
 */
package com.skysoft.smart.intranetchat.app.impl;

import com.skysoft.smart.intranetchat.model.network.bean.FileBean;
import com.skysoft.smart.intranetchat.model.network.bean.MessageBean;

public interface OnReceiveMessage {
    void onReceiveMessage(MessageBean message, String host);

    void onReceiveFile(FileBean fileBean, String host);

    void onReceiveAndSaveFile(String sender,String receiver,String identifier,String path);
}
