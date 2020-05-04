package com.skysoft.smart.intranetchat.model.net_model;

import android.os.RemoteException;

import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.model.network.bean.AskResourceBean;
import com.skysoft.smart.intranetchat.tools.GsonTools;

public class SendRequest {
    public static void sendMonitorRequest(int monitorRequest, String identifier, String host){
        AskResourceBean askResourceBean = new AskResourceBean();
        askResourceBean.setResourceType(monitorRequest);
        askResourceBean.setResourceUniqueIdentifier(identifier);
        sendAskResourceBean(askResourceBean,host);
    }

    public static void sendHeartbeat(String identifier, String host){
        AskResourceBean askResourceBean = new AskResourceBean();
        askResourceBean.setResourceUniqueIdentifier(identifier);
        askResourceBean.setResourceType(Config.HEARTBEAT);
        sendAskResourceBean(askResourceBean,host);
    }

    public static void sendRequestHeartbeat(String identifier, String host){
        AskResourceBean askResourceBean = new AskResourceBean();
        askResourceBean.setResourceUniqueIdentifier(identifier);
        askResourceBean.setResourceType(Config.REQUEST_HEARTBEAT);
        sendAskResourceBean(askResourceBean,host);
    }

    private static void sendAskResourceBean(AskResourceBean askResourceBean, String host){
        try {
            IntranetChatApplication.sAidlInterface.askResource(GsonTools.toJson(askResourceBean),host);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
