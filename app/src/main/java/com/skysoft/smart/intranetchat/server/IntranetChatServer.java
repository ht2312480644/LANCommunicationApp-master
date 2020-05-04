/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication] Create a new process service with message sending and receiving.
 */
package com.skysoft.smart.intranetchat.server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.model.network.bean.UserInfoBean;
import com.skysoft.smart.intranetchat.model.network.receive.MonitorCallThread;
import com.skysoft.smart.intranetchat.model.network.receive.MonitorTcpReceivePortThread;
import com.skysoft.smart.intranetchat.model.network.receive.MonitorUdpReceivePortThread;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class IntranetChatServer extends Service {

    private String TAG = IntranetChatServer.class.getSimpleName();
    private IntranetChatAidl mBinder = new IntranetChatAidl();
    public static UserInfoBean sUserInfo;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            setForeground();
        }else {
            setForegroundService();
        }
        return mBinder;
    }

    //Channel ID 必须保证唯一
    private static final String CHANNEL_ID = "com.skysoft.smart.intranetchat.server.IntranetChatServer";
    /**
     *创建通知渠道
     */
    private void createNotificationChannel() {
        // 在API>=26的时候创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //设定的通知渠道名称
            String channelName = getString(R.string.channel_name);
            //设置通知的重要程度
            int importance = NotificationManager.IMPORTANCE_LOW;
            //构建通知渠道
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(getString(R.string.description));
            //向系统注册通知渠道，注册后不能改变重要性以及其他通知行为
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        int pid = android.os.Process.myPid();
        IntranetChatApplication.setsCreateProgress(pid);
        TLog.d(TAG, "onCreate: pid = " + pid);
        String myHost = getHostIP();
        MonitorUdpReceivePortThread murpt = new MonitorUdpReceivePortThread(mBinder.getRemoteCallbackList(),mBinder.getUserInfoBean(),myHost);
        murpt.start();
        MonitorTcpReceivePortThread mtrpt = new MonitorTcpReceivePortThread(mBinder.getRemoteCallbackList(),myHost);
        mtrpt.start();
        MonitorCallThread monitorCallThread = new MonitorCallThread();
        monitorCallThread.start();
    }

    @Override
    public void onDestroy() {
//        Intent service = new Intent(this,IntranetChatServer.class);
//        this.startService(service);
        super.onDestroy();
        stopForeground(true);
    }

    public static String getHostIP() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            TLog.i("yao", "SocketException");
            e.printStackTrace();
        }
        return hostIp;
    }

    private void setForeground(){
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext());
        Intent nfIntent = new Intent(this, IntranetChatApplication.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,nfIntent,0);
        builder.setContentTitle("content title")
                .setContentText("context text")
                .setContentIntent(pendingIntent)
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),R.mipmap.ic_launcher));
        Notification notification = builder.build();
        startForeground(110, notification);
    }

    /**
     *通过通知启动服务
     */
    @androidx.annotation.RequiresApi(api = Build.VERSION_CODES.O)
    public void  setForegroundService() {
        //设定的通知渠道名称
        String channelName = getString(R.string.channel_name);
        //设置通知的重要程度
        int importance = NotificationManager.IMPORTANCE_LOW;
        //构建通知渠道
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
        channel.setDescription(getString(R.string.description));
        //在创建的通知渠道上发送通知
        //跳转到应用权限界面
        Intent intent = new Intent();
        intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + this.getPackageName()));
        PendingIntent pendingIntent = PendingIntent.getActivity(IntranetChatServer.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.logo) //设置通知图标
                .setContentTitle("“天软通”正在运行")//设置通知标题
                .setContentText("点击即可了解详情或停止应用")//设置通知内容
                .setAutoCancel(true) //用户触摸时，自动关闭
                .setContentIntent(pendingIntent)
                .setOngoing(true);//设置处于运行状态
        //向系统注册通知渠道，注册后不能改变重要性以及其他通知行为
        NotificationManager notificationManager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        //将服务置于启动状态 NOTIFICATION_ID指的是创建的通知的ID
        startForeground(110,builder.build());
    }
}
