/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Voice or video communication function
 */
package com.skysoft.smart.intranetchat.model.net_model;

import android.os.RemoteException;

import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.model.network.bean.AskBean;
import com.skysoft.smart.intranetchat.model.network.bean.ResponseBean;
import com.skysoft.smart.intranetchat.model.network.bean.UserInfoBean;
import com.skysoft.smart.intranetchat.tools.GsonTools;

public class VoiceCall {
    public static void refuseVoiceCall(String host){
        VoiceCall voiceCall = new VoiceCall();
        voiceCall.responseVoiceCall(false,host);
    }

    public static void consentVoiceCall(String host){
        VoiceCall voiceCall = new VoiceCall();
        voiceCall.responseVoiceCall(true,host);
    }

    public static void hungUpVoiceCall(String host){
        ResponseBean responseBean = new ResponseBean(Config.RESPONSE_HUNG_UP_CALL,null);
        try {
            IntranetChatApplication.sAidlInterface.sendResponse(GsonTools.toJson(responseBean),host);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void responseVoiceCall(boolean consent,String host){
        ResponseBean responseBean = null;
        if (consent){
            responseBean = new ResponseBean(Config.RESPONSE_CONSENT_VOICE_CALL,null);
        }else {
            responseBean = new ResponseBean(Config.RESPONSE_REFUSE_VOICE_CALL,null);
        }
        try {
            IntranetChatApplication.sAidlInterface.sendResponse(GsonTools.toJson(responseBean),host);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void startVoiceCall(UserInfoBean userInfoBean, String host){
        if (userInfoBean == null){
            return;
        }
        try {
            IntranetChatApplication.sAidlInterface.requestVoiceCall(GsonTools.toJson(userInfoBean),host);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void sendVoiceCallData(byte[] data){
        try {
            IntranetChatApplication.sAidlInterface.sendVoiceCallData(data);
        } catch (RemoteException e) {
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public static void requestConsentVoiceCall(String host){
        AskBean askBean = new AskBean();
        askBean.setRequestType(Config.REQUEST_CONSENT_CALL);
        try {
            IntranetChatApplication.sAidlInterface.requestConsentCall(GsonTools.toJson(askBean),host);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void responseConsentOutTime(String host){
        ResponseBean responseBean = new ResponseBean(Config.RESPONSE_CONSENT_OUT_TIME,null);
        try {
            IntranetChatApplication.sAidlInterface.sendResponse(GsonTools.toJson(responseBean),host);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void startVideoCall(UserInfoBean userInfoBean,String host){
        if (userInfoBean == null){
            return;
        }
        try {
            IntranetChatApplication.sAidlInterface.requestVideoCall(GsonTools.toJson(userInfoBean),host);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void responseInCall(String host){
        ResponseBean responseBean = new ResponseBean(Config.RESPONSE_IN_CALL,IntranetChatApplication.getsMineUserInfo().getIdentifier());
        try {
            IntranetChatApplication.sAidlInterface.sendResponse(GsonTools.toJson(responseBean),host);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
