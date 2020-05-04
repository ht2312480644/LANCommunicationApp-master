/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/15
 * Description: [Intranet Chat] [APP] [Communication]Create a message receiver to receive UDP data.
 */
package com.skysoft.smart.intranetchat.model.network.receive;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.IIntranetChatAidlInterfaceCallback;
import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.model.network.bean.UserInfoBean;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class MonitorUdpReceivePortThread extends Thread {
    private final String TAG = MonitorUdpReceivePortThread.class.getSimpleName();

    private DatagramSocket mMonitor ;
    private static RemoteCallbackList<IIntranetChatAidlInterfaceCallback> callbackList;
    private static UserInfoBean userInfoBean;
    private static String myHost;

    public MonitorUdpReceivePortThread(RemoteCallbackList<IIntranetChatAidlInterfaceCallback> callbackList,UserInfoBean userInfoBean,String myHost){
        this.callbackList = callbackList;
        this.userInfoBean = userInfoBean;
        this.myHost = myHost;
    }

    @Override
    public void run() {
        super.run();
        try {
            mMonitor = new DatagramSocket(Config.PORT_UDP_RECEIVE);
            monitorUdpRcvPort();
        } catch (SocketException e) {
            TLog.e(TAG, "SocketException: ",e );
        } catch (IOException e) {
            TLog.e(TAG, "run: ", e );
        }
    }

    private void monitorUdpRcvPort() throws IOException {
        while (true){
            byte[] dataPacket = new byte[Config.UDP_RCV_DATA_PACKET_LENGTH];
            DatagramPacket dp = new DatagramPacket(dataPacket,dataPacket.length);
            mMonitor.receive(dp);
            String data = new String( dataPacket,0,dp.getLength());
            String host = dp.getAddress().getHostAddress();
            if (host.equals(myHost)){
                continue;
            }
            ParseDataPacketThread pt = new ParseDataPacketThread(data,host);
            pt.start();
        }
    }

    public static void broadcastReceive(int type,String data,String host){
        synchronized (callbackList){
            int count = callbackList.beginBroadcast();
            for (int i = 0; i < count; i++){
                IIntranetChatAidlInterfaceCallback broadcastItem = callbackList.getBroadcastItem(i);
                try {
                    switch (type){
                        case Config.CODE_MESSAGE:
                            broadcastItem.onReceiveMessage(data,host);
                            break;
                        case Config.CODE_MESSAGE_NOTIFICATION:
                            broadcastItem.receiveNotifyMessageBean(data,host);
                            break;
                        case Config.CODE_MESSAGE_REPLAY:
                            broadcastItem.receiveReplayMessageBean(data,host);
                            break;
                        case Config.CODE_USERINFO:
                            broadcastItem.onReceiveUserInfo(data,host);
                            break;
                        case Config.CODE_REQUEST:
                            broadcastItem.onReceiveRequest(data,host);
                            break;
                        case Config.CODE_FILE:
                            broadcastItem.onReceiveFile(data,host);
                            break;
                        case Config.CODE_ASK_RESOURCE:
                            broadcastItem.onReceiveAskResource(data,host);
                            break;
                        case Config.CODE_VOICE_CALL:
                            broadcastItem.onReceiveVoiceCall(data,host);
                            break;
                        case Config.RESPONSE_CONSENT_VOICE_CALL:
                            broadcastItem.onReceiveConsentVoiceCall(host);
                            break;
                        case Config.RESPONSE_REFUSE_VOICE_CALL:
                            broadcastItem.onReceiveRefuseVoiceCall(host);
                            break;
                        case Config.RESPONSE_HUNG_UP_CALL:
                            broadcastItem.onReceiveHungUpVoiceCall(host);
                            break;
                        case Config.ON_ESTABLISH_CALL_CONNECT:
                            broadcastItem.onEstablishCallConnect();
                            break;
                        case Config.RESPONSE_WAITING_CONSENT:
                            broadcastItem.onReceiveWaitingConsentCall();
                            break;
                        case Config.RESPONSE_CONSENT_OUT_TIME:
                            broadcastItem.onReceiveConsentOutTime();
                            break;
                        case Config.CODE_VIDEO_CALL:
                            broadcastItem.onReceiveVideoCall(data,host);
                            break;
                        case Config.CODE_ESTABLISH_GROUP:
                            broadcastItem.onReceiveEstablishBeanJson(data,host);
                            break;
                        case Config.NOT_FOUND_RESOURCE:
                            broadcastItem.onReceiveAskResourceNotFound(data,host);
                            break;
                        case Config.RESPONSE_IN_CALL:
                            broadcastItem.onReceiveInCall(host);
                            break;
                        case Config.NOTIFY_CLEAR_QUEUE:
                            broadcastItem.notifyClearQueue();
                            break;
                            //开始下载文件
                        case Config.STEP_ASK_FILE:
                            broadcastItem.askFile(data,host);
                            break;
                            //下载文件失败（原因在对方）
                        case Config.STEP_DOWN_LOAD_FAILURE:
                            broadcastItem.receiveFileFailure(data);
                            break;
                        case Config.RESPONSE_REFUSE_BE_MONITOR:
                            broadcastItem.receiveMonitorResponse(Config.RESPONSE_REFUSE_BE_MONITOR,data);
                            break;
                        case Config.RESPONSE_REFUSE_MONITOR:
                            broadcastItem.receiveMonitorResponse(Config.RESPONSE_REFUSE_MONITOR,data);
                            break;
                        case Config.CODE_USER_OUT_LINE:
                            broadcastItem.receiveUserOutLine(data);
                            break;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
            callbackList.finishBroadcast();
        }
    }

    public static void setMyHost(String host){
        myHost = host;
    }
}
