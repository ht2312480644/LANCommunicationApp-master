/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Create a file service for file recipients to download files.
 */
package com.skysoft.smart.intranetchat.model.network.receive;

import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.skysoft.smart.intranetchat.IIntranetChatAidlInterfaceCallback;
import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.model.network.bean.VoiceCallDataBean;
import com.skysoft.smart.intranetchat.model.network.file.SendFileThread;
import com.skysoft.smart.intranetchat.model.network.manager.SocketManager;
import com.skysoft.smart.intranetchat.tools.GsonTools;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MonitorTcpReceivePortThread extends Thread {
    private ServerSocket serverSocket;
    private static String TAG = MonitorTcpReceivePortThread.class.getSimpleName();

    private static RemoteCallbackList<IIntranetChatAidlInterfaceCallback> callbackList;
    public MonitorTcpReceivePortThread(RemoteCallbackList<IIntranetChatAidlInterfaceCallback> callbackList,String myHost){
        this.callbackList = callbackList;
    }

    @Override
    public void run() {
        super.run();
        try {
            serverSocket = new ServerSocket(Config.PORT_TCP_FILE);
            while (true){
                Socket accept = serverSocket.accept();
                SocketManager.getInstance().add();
                SendFileThread sendFileThread = new SendFileThread(accept);
                sendFileThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcastSaveFile(String sender, String receiver, String identifier, String path,String host){
        synchronized (callbackList){
            int count = callbackList.beginBroadcast();
            for (int i = 0; i < count; i++){
                IIntranetChatAidlInterfaceCallback broadcastItem = callbackList.getBroadcastItem(i);
                try {
                    broadcastItem.onReceiveAndSaveFile(sender,receiver,identifier,path,host);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            callbackList.finishBroadcast();
        }
    }

    public static void broadcastReceive(byte[] data,int length){
        synchronized (callbackList){
            int count = callbackList.beginBroadcast();
            for (int i = 0; i < count; i++) {
                IIntranetChatAidlInterfaceCallback broadcastItem = callbackList.getBroadcastItem(i);
                try {
                    VoiceCallDataBean callBean = new VoiceCallDataBean();
                    if (length < data.length){
                        byte[] buf = new byte[length];
                        for (int j = 0; j < length; j++){
                            buf[j] = data[j];
                        }
                        callBean.setData(buf);
                    }else {
                        callBean.setData(data);
                    }
                    if (broadcastItem != null && callBean != null){
                        broadcastItem.onReceiveVoiceCallData(GsonTools.toJson(callBean));
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            callbackList.finishBroadcast();
        }
    }
}
