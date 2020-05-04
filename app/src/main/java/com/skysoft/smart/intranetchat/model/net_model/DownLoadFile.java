/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Create a file service for file recipients to download files.
 */
package com.skysoft.smart.intranetchat.model.net_model;

import android.os.RemoteException;

import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.model.network.bean.FileBean;
import com.skysoft.smart.intranetchat.tools.GsonTools;

public class DownLoadFile {
    private String TAG = DownLoadFile.class.getSimpleName();

    public static void downFile(FileBean fileBean, String host){
        try {
            IntranetChatApplication.sAidlInterface.downFile(GsonTools.toJson(fileBean),host);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
