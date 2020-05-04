/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Create a file service for file recipients to download files.
 */
package com.skysoft.smart.intranetchat.model.network.manager;

import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

public class SocketManager {
    private int socketNumber;
    public static final int MAX_NUMBER = 10;
    private String TAG = SocketManager.class.getSimpleName();
    private SocketManager(){}
    private static SocketManager sInstance = new SocketManager();
    public static SocketManager getInstance(){
        return sInstance;
    }

    public int getSocketNumber(){
        return this.socketNumber;
    }

    public int add(){
        synchronized (sInstance){
            ++socketNumber;
            TLog.d(TAG, "add: socketNumber = " + socketNumber);
            return socketNumber;
        }
    }

    public int reduce(){
        synchronized (sInstance){
            if (socketNumber <= 0){
                return -1;
            }
            --socketNumber;
            TLog.d(TAG, "reduce: socketNumber = " + socketNumber);
            return socketNumber;
        }
    }
}
