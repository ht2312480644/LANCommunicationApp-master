/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Voice or video communication function
 */
package com.skysoft.smart.intranetchat.model.network.receive;

import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.model.network.call.VoiceCallThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MonitorCallThread extends Thread {
    private String TAG = MonitorCallThread.class.getSimpleName();
    private ServerSocket serverSocket;

    @Override
    public void run() {
        super.run();
        try {
            serverSocket = new ServerSocket(Config.PORT_TCP_CALL);
            while (true){
                Socket accept = serverSocket.accept();
                TLog.d(TAG, "run: VoiceCallThread");
                VoiceCallThread.init(accept);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
