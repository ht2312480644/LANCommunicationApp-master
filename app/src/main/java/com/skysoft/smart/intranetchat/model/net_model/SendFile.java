/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Create a file service for file recipients to download files.
 */
package com.skysoft.smart.intranetchat.model.net_model;

import android.os.Environment;
import android.os.RemoteException;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.model.network.bean.FileBean;
import com.skysoft.smart.intranetchat.model.network.bean.ReceiveAndSaveFileBean;
import com.skysoft.smart.intranetchat.tools.GsonTools;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

public class SendFile {
    private static String TAG = SendFile.class.getSimpleName();

    public int sendFile(ReceiveAndSaveFileBean rafb, String host,int type){
        return sendFile(rafb,host,type,0);
    }

    public int sendFile(ReceiveAndSaveFileBean rafb, String host, int type, int length){
        File file = getFile(rafb.getPath(),type);
        if (file.length() > 1024*1024*50){
            TLog.d(TAG, "sendFile: 文件太大");
            return Config.SEND_FILE_BIG;
        }else if (file.length() == 0){
            TLog.d(TAG, "sendFile: 文件为0k");
            return Config.SEND_FILE_ZEOR;
        }
        FileBean fileBean = generatorFileBean(file,rafb.getIdentifier(),rafb.getReceiver(),rafb.getSender(),type);
        if (fileBean == null){
            TLog.d(TAG, "sendFile: 没有找到文件，文件路径错误");
            return Config.SEND_FILE_NOT_FOUND;
        }
        if (length != 0){
            fileBean.setFileLength(length);
        }
        try {
            IntranetChatApplication.sAidlInterface.sendFile(GsonTools.toJson(fileBean),file.getPath(),host);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return Config.SEND_SUCCESS;
    }

    public void sendFileGroup(String path, String resourceUniqueIdentifier, List<String> hostList, String receiver,String sender, int type){
        File file = getFile(path,type);
        FileBean fileBean = generatorFileBean(file,resourceUniqueIdentifier,sender,receiver,type);
        try {
            IntranetChatApplication.sAidlInterface.sendGroupFile(GsonTools.toJson(fileBean),file.getPath(),hostList);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public FileBean generatorFileBean(File file,String resourceUniqueIdentifier,String receiver,String sender,int type){
        if (file == null || file.isDirectory() || !file.exists()){
            return null;
        }
        String md5 = getFileMd5(file);

        FileBean fileBean = new FileBean();
        fileBean.setType(type);
        fileBean.setFileLength(file.length());
        if(fileBean.getFileLength() == 0){
            return null;
        }
        fileBean.setFileName(file.getName());
        fileBean.setFileUniqueIdentifier(resourceUniqueIdentifier);
        fileBean.setMd5(md5);
        fileBean.setReceiver(receiver);
        fileBean.setSender(sender);
        return fileBean;
    }

    public static File getFile(String filePath,int type){
        File file = null;
        if (type == Config.FILE_VOICE || type == Config.FILE_PICTURE || type == Config.FILE_VIDEO || type == Config.FILE_COMMON || type == Config.FILE_AVATAR){
            file = new File(filePath);
        }else {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            file = new File(path + filePath);
        }
        return file;
    }

    public static File getFile(String filePath){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(path + filePath);
        return file;
    }

    /*获得文件的文件名，md5，长度和生成唯一标识符*/
    public static String getFileMd5(File file){
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());

        return bigInt.toString(Config.RADIX_16);
    }
}
