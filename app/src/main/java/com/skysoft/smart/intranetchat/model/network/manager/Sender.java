/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Create a file service for file recipients to download files.
 */
package com.skysoft.smart.intranetchat.model.network.manager;

import android.text.TextUtils;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.model.network.bean.DataPacketBean;
import com.skysoft.smart.intranetchat.model.network.send.SendDataPacket;
import com.skysoft.smart.intranetchat.tools.GsonTools;

import java.util.List;

public class Sender {
    public static void sender(String data,int code,String host){
        if(TextUtils.isEmpty(data)){
            return;
        }
        DataPacketBean dataPacketBean = new DataPacketBean();
        dataPacketBean.setCode(code);
        dataPacketBean.setData(data);
        TLog.d("Sender", "sender: ");
        SendDataPacket sdp = new SendDataPacket(GsonTools.toJson(dataPacketBean),host);
        sdp.start();
    }

    public static void senderGroup(String data, int code, List<String> hostList){
        if(TextUtils.isEmpty(data)){
            return;
        }
        DataPacketBean dataPacketBean = new DataPacketBean();
        dataPacketBean.setCode(code);
        dataPacketBean.setData(data);
        SendDataPacket sdp = new SendDataPacket(GsonTools.toJson(dataPacketBean),hostList);
        sdp.start();
    }
}
