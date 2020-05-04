/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Create a UDP data sender
 */
package com.skysoft.smart.intranetchat.model.network.send;

import android.text.TextUtils;

import com.skysoft.smart.intranetchat.model.network.Config;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;

public class SendDataPacket extends Thread{
    private String TAG = SendDataPacket.class.getSimpleName();
    private String data = null;
    private String host = null;
    private List<String> hostList = null;

    public SendDataPacket(String data,String host){
        this.data = data;
        this.host = host;
    }

    public SendDataPacket(String data, List<String> hostList){
        this.data = data;
        this.hostList = hostList;
    }

    @Override
    public void run() {
        super.run();
        if (TextUtils.isEmpty(data)){
            return;
        }
        if (host != null){
            sendDataPacket(data.getBytes(), host);
        }else if (hostList != null){
            sendDataPacketGroup(data.getBytes(),hostList);
        }else{
            return;
        }
    }

    /*向某个用户发送数据*/
    public void sendDataPacket(byte[] data,String host){
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket();
            DatagramPacket dp = new DatagramPacket(data,data.length,InetAddress.getByName(host), Config.PORT_UDP_RECEIVE);
            ds.send(dp);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ds != null){
                ds.close();
            }
        }
    }

    /*在群组中广播数据*/
    public void sendDataPacketGroup(byte[] data,List<String> hostList){
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (hostList.size() == 0){
            return;
        }
        Iterator<String> iterator = hostList.iterator();
        while (iterator.hasNext()){
            String host = iterator.next();
            try {
                DatagramPacket dp = new DatagramPacket(data,data.length,InetAddress.getByName(host),Config.PORT_UDP_RECEIVE);
                ds.send(dp);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
