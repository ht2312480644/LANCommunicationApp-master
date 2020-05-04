package com.skysoft.smart.intranetchat.model.net_model;

import android.os.RemoteException;

import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.model.network.bean.ResponseBean;
import com.skysoft.smart.intranetchat.tools.GsonTools;

public class SendResponse {
    public static void sendMonitorResponse(int monitorResponse, String identifier,String host){
        ResponseBean responseBean = new ResponseBean(monitorResponse,identifier);
        try {
            IntranetChatApplication.sAidlInterface.sendResponse(GsonTools.toJson(responseBean),host);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
