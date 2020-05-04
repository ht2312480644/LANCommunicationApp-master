/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Create a file service for file recipients to download files.
 */
package com.skysoft.smart.intranetchat.model.network.file;

import android.text.TextUtils;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.model.net_model.SendFile;
import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.model.network.bean.FileBean;
import com.skysoft.smart.intranetchat.model.network.bean.ResourceManagerBean;
import com.skysoft.smart.intranetchat.model.network.bean.ResponseBean;
import com.skysoft.smart.intranetchat.model.network.manager.ResourceManager;
import com.skysoft.smart.intranetchat.model.network.manager.ResponseSender;
import com.skysoft.smart.intranetchat.model.network.manager.Sender;
import com.skysoft.smart.intranetchat.model.network.manager.SocketManager;
import com.skysoft.smart.intranetchat.model.network.receive.MonitorTcpReceivePortThread;
import com.skysoft.smart.intranetchat.model.network.receive.MonitorUdpReceivePortThread;
import com.skysoft.smart.intranetchat.tools.GsonTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class AskFileThread extends Thread {
    private String TAG = AskFileThread.class.getSimpleName();
    private Socket socket;
    private String host;
    public AskFileThread(String host){
        this.host = host;
        try {
            socket = new Socket(host,Config.PORT_TCP_FILE);
            SocketManager.getInstance().add();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        if (socket == null){
            return;
        }
        InputStream is = null;
        byte[] buf = new byte[8096];
        int count = 0;
        String resourceUniqueIdentifier = null;
        File file = null;
        FileOutputStream fos = null;
        String resourceMd5 = null;
        boolean isSuccess = false;
        try {
            is = socket.getInputStream();
            if ((count = is.read(buf)) != -1){
                int multiple = buf[0];
                int remainder = buf[1];
                int length = multiple*128 + remainder;
                resourceUniqueIdentifier = new String(buf,2,length);
                /*该资源不在请求下载列表中*/
                if (!ResourceManager.getInstance().exist(resourceUniqueIdentifier)){
                    ResponseSender.response(Config.RESPONSE_NOT_REQUEST_THIS_RESOURCE,resourceUniqueIdentifier,host);
                    return;
                }
                ResourceManagerBean resource = ResourceManager.getInstance().getResource(resourceUniqueIdentifier);
                if (!resource.isReceive()){
                    TLog.d(TAG, "run: resource.isReceive() = false");
                    return;
                }
                resourceMd5 = resource.getFileBean().getMd5();

                File parentFile = SendFile.getFile(resource.getPath());
                if (!parentFile.exists()){
                    parentFile.mkdirs();
                }
                int i = 1;
                file = new File(parentFile,resource.getFileBean().getFileName());
                while (true){
                    if (!file.exists()){
                        break;
                    }
                    file = new File(parentFile,i + "_" + resource.getFileBean().getFileName());
                    i ++;
                }
                file.createNewFile();
                resource.setPath(file.getPath());
                //通知主进程开始接收文件数据
                MonitorUdpReceivePortThread.broadcastReceive(Config.STEP_ASK_FILE,resourceUniqueIdentifier,file.getPath());
                fos = new FileOutputStream(file);
                fos.write(buf,length+2,count - length - 2);
                fos.flush();
            }
            while ((count = is.read(buf)) != -1){
                fos.write(buf,0,count);
                fos.flush();
            }
            String md5 = SendFile.getFileMd5(file);
            ResponseBean responseBean = null;
            if (TextUtils.isEmpty(md5) || !md5.equals(resourceMd5)){
                /*接收文件失败*/
                file.deleteOnExit();
                TLog.d(TAG, "run: receive file failure");
                responseBean = new ResponseBean(Config.RESPONSE_RECEIVE_FILE_FAILURE,resourceUniqueIdentifier);
            }else {
                TLog.d(TAG, "run: receive file success");
                /*call back*/
                ResourceManagerBean resource = ResourceManager.getInstance().getResource(resourceUniqueIdentifier);
                FileBean fileBean = resource.getFileBean();
                MonitorTcpReceivePortThread.broadcastSaveFile(fileBean.getSender(),fileBean.getReceiver(),resourceUniqueIdentifier,file.getPath(),host);

                /*remove resource record*/
                responseBean = new ResponseBean(Config.RESPONSE_RECEIVE_FILE_SUCCESS,resourceUniqueIdentifier);

                //记录文件接收成功
                isSuccess = true;
            }
            Sender.sender(GsonTools.toJson(responseBean),Config.CODE_RESPONSE,host);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //如果接受失败（原因在对方），通知主进程
            if (!isSuccess){
                MonitorUdpReceivePortThread.broadcastReceive(Config.STEP_DOWN_LOAD_FAILURE,resourceUniqueIdentifier,null);
            }

            if (socket != null){
                try {
                    socket.close();
                    SocketManager.getInstance().reduce();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (is != null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
