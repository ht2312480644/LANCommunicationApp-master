/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Create a file service for file recipients to download files.
 */
package com.skysoft.smart.intranetchat.model.network.file;

import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.model.network.bean.ResourceManagerBean;
import com.skysoft.smart.intranetchat.model.network.manager.FileWaitToSend;
import com.skysoft.smart.intranetchat.model.network.manager.ResponseSender;
import com.skysoft.smart.intranetchat.model.network.bean.ResponseBean;
import com.skysoft.smart.intranetchat.model.network.manager.Sender;
import com.skysoft.smart.intranetchat.model.network.manager.SocketManager;
import com.skysoft.smart.intranetchat.tools.GsonTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class SendFileThread extends Thread {
    private String TAG = SendFileThread.class.getSimpleName();

    private String host;
    private Socket socket;
    public SendFileThread(Socket socket){
        this.socket = socket;
        this.host = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        super.run();
        ResourceManagerBean waitToSend = FileWaitToSend.getInstance().getWaitToSend(host);

        if (waitToSend == null){
            ResponseSender.response(Config.RESPONSE_NOTHING_ASK,null,host);
            return;
        }
        String path = waitToSend.getPath();
        String resourceUniqueIdentifier = waitToSend.getFileBean().getFileUniqueIdentifier();

        File file = new File(path);
        if (!file.exists() || !file.isFile()){
            ResponseBean responseBean = new ResponseBean(Config.RESPONSE_NOT_FOUND_FILE,resourceUniqueIdentifier);
            Sender.sender(GsonTools.toJson(responseBean),Config.CODE_RESPONSE,host);
            TLog.d(TAG, "run: 文件不存在");
            return;
        }

        FileInputStream fis = null;
        OutputStream os = null;
        int count = 0;
        byte[] buf = new byte[8096];
        byte[] header = resourceUniqueIdentifier.getBytes();
        int multiple = header.length / 128;
        int remainder = header.length % 128;
        buf[0] = (byte) multiple;
        buf[1] = (byte) remainder;

        //填充唯一标识符到数据包中
        for (int i = 0; i < header.length; i++){
            buf[i + 2] = header[i];
        }
        try {
            fis = new FileInputStream(file);
            os = socket.getOutputStream();
            //读取文件
            if ((count = fis.read(buf,header.length+2,buf.length - header.length - 2)) != -1){
                os.write(buf,0,count + header.length + 2);
                os.flush();
            }
            while ((count = fis.read(buf)) != -1){
                os.write(buf,0,count);
                os.flush();
            }
            TLog.d(TAG, "run: 发送文件结束");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SocketManager.getInstance().reduce();
            }
            if (fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
