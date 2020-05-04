/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/15
 * Description: [PT-38][Intranet Chat] [APP][UI]Program launch page and animation
 */
package com.skysoft.smart.intranetchat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.ashokvarma.bottomnavigation.TextBadgeItem;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.model.net_model.Login;
import com.skysoft.smart.intranetchat.model.net_model.VoiceCall;
import com.skysoft.smart.intranetchat.app.impl.HandleInfo;
import com.skysoft.smart.intranetchat.model.camera.entity.EventMessage;
import com.skysoft.smart.intranetchat.database.table.ContactEntity;
import com.skysoft.smart.intranetchat.model.network.bean.UserInfoBean;
import com.skysoft.smart.intranetchat.server.IntranetChatServer;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;
import com.skysoft.smart.intranetchat.tools.toastutil.ToastUtil;
import com.skysoft.smart.intranetchat.ui.activity.videocall.AnswerVideoCallActivity;
import com.skysoft.smart.intranetchat.ui.activity.voicecall.AnswerVoiceCallActivity;
import com.skysoft.smart.intranetchat.ui.fragment.main.contact.ContactFragment;
import com.skysoft.smart.intranetchat.ui.fragment.main.message.MessageFragment;
import com.skysoft.smart.intranetchat.ui.fragment.main.mine.MineFragment;
import com.skysoft.smart.intranetchat.ui.fragment.main.tool.ToolsFragment;

import org.greenrobot.eventbus.EventBus;


public class MainActivity extends AppCompatActivity{

    public static final int CALL_FROM_OTHER = 10019;
    public static boolean IS_CALL_FROM_OTHER = false;
    private BottomNavigationBar mBottomNavigationBar;
    private TextBadgeItem textBadgeItem;
    private static FragmentManager sFragmentManager;
    private MessageFragment sMessageFragment;
    private ContactFragment sContactFragment;
    private ToolsFragment sToolsFragment;
    private MineFragment sMineFragment;
    private String TAG = MainActivity.class.getSimpleName();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        IntranetChatApplication.getsCallback().setHandleInfo(handleInfo);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CustomStatusBarBackground.customStatusBarTransparent(this);
        initView();
        setEnterFragment();
        setView();
        //B: 监听网络状况 ,Oliver Ou,2019/11/15
        initReceiver();
        //E: 监听网络状况 ,Oliver Ou,2019/11/15
    }
    private long time;
    private boolean quit = false;
    @Override
    public void onBackPressed() {
        if (quit == false){
            time = System.currentTimeMillis();
            quit = true;
            ToastUtil.timingToast(MainActivity.this,getString(R.string.quitApp_toast_text),1000);
            return;
        }
        if (System.currentTimeMillis() - time <= 1000){
            super.onBackPressed();
            ToastUtil.cancelToast(MainActivity.this);
            if (IntranetChatApplication.getsNotificationManager() != null){
                IntranetChatApplication.getsNotificationManager().cancelAll();
            }
            try {
                IntranetChatApplication.sAidlInterface.killProgress();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
            Login.broadcastUserOutLine(IntranetChatApplication.getsMineUserInfo().getIdentifier());
        }else{
            time = System.currentTimeMillis();
            ToastUtil.timingToast(MainActivity.this,getString(R.string.quitApp_toast_text),1000);
        }
    }
    private void initView() {
        mBottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar_main);
        IntranetChatApplication.setmTextBadgeItem(new TextBadgeItem());
        textBadgeItem = IntranetChatApplication.getmTextBadgeItem();
        //B: [PT-80][Intranet Chat] [APP][UI] TextBadgeItem 一直为红色,Allen Luo,2019/11/12

        if (IntranetChatApplication.getmTotalUnReadNumber() == 0){
            textBadgeItem.hide();
        }else {
            textBadgeItem.show().setText(String.valueOf(IntranetChatApplication.getmTotalUnReadNumber()));

        }
        //E: [PT-80][Intranet Chat] [APP][UI] TextBadgeItem 一直为红色,Allen Luo,2019/11/12
    }
    private void setView() {
        mBottomNavigationBar.setMode(BottomNavigationBar. MODE_SHIFTING)
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC)
                .setActiveColor(R.color.color_white);

        mBottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.ic_fragment_main_message_click, "消息").setActiveColorResource(R.color.color_blue)
                .setInactiveIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_fragment_main_message_default)).setInActiveColorResource(R.color.color_black)
                .setBadgeItem(textBadgeItem))
                .addItem(new BottomNavigationItem(R.drawable.ic_fragment_main_contact_click, "联系人").setActiveColorResource(R.color.color_blue)
                        .setInactiveIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_fragment_main_contact_default)).setInActiveColorResource(R.color.color_black))
                .addItem(new BottomNavigationItem(R.drawable.ic_fragment_main_tools_click, "工具").setActiveColorResource(R.color.color_blue)
                        .setInactiveIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_fragment_main_tools_default)).setInActiveColorResource(R.color.color_black))
                .addItem(new BottomNavigationItem(R.drawable.ic_fragment_main_mine_click, "个人").setActiveColorResource(R.color.color_blue)
                        .setInactiveIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_fragment_main_mine_default)).setInActiveColorResource(R.color.color_black))
                .setFirstSelectedPosition(0)
                .initialise();

        mBottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                //B: [PT-79] [Intranet Chat] [APP][UI] BottomNavigationBar 控件优化,Allen Luo,2019/11/13
                FragmentTransaction sFragmentTransaction = sFragmentManager.beginTransaction();
                hideFragment(sFragmentTransaction);

                switch (position) {
                    case 0:
                        if (sMessageFragment == null) {
                            sMessageFragment = new MessageFragment();
                            sFragmentTransaction.add(R.id.frameContent, sMessageFragment);
                        } else {
                            sFragmentTransaction.show(sMessageFragment);
                        }
                        break;
                    case 1:
                        if (sContactFragment == null) {
                            sContactFragment = new ContactFragment();
                            sFragmentTransaction.add(R.id.frameContent, sContactFragment);
                        } else {
                            sFragmentTransaction.show(sContactFragment);
                        }
                        break;
                    case 2:
                        if (sToolsFragment == null) {
                            sToolsFragment = new ToolsFragment();
                            sFragmentTransaction.add(R.id.frameContent, sToolsFragment);
                        } else {
                            sFragmentTransaction.show(sToolsFragment);
                        }
                        break;
                    case 3:
                        if (sMineFragment == null) {
                            sMineFragment = new MineFragment();
                            sFragmentTransaction.add(R.id.frameContent, sMineFragment);
                        } else {
                            sFragmentTransaction.show(sMineFragment);
                        }
                        break;
                }
                sFragmentTransaction.commit();
                //E: [PT-79] [Intranet Chat] [APP][UI] BottomNavigationBar 控件优化,Allen Luo,2019/11/13
            }

            @Override
            public void onTabUnselected(int position) {

            }

            @Override
            public void onTabReselected(int position) {

            }
        });
    }
    //B: [PT-79] [Intranet Chat] [APP][UI] BottomNavigationBar 控件优化,Allen Luo,2019/11/13
    public void setEnterFragment(){
        sFragmentManager = getSupportFragmentManager();
        FragmentTransaction sFragmentTransaction = getSupportFragmentManager().beginTransaction();
        sMessageFragment = new MessageFragment();
        sFragmentTransaction.add(R.id.frameContent,sMessageFragment);
        sFragmentTransaction.commit();
    }

    private void hideFragment(FragmentTransaction transaction){
        if (sMessageFragment != null){
            transaction.hide(sMessageFragment);
        }
        if (sContactFragment != null){
            transaction.hide(sContactFragment);
        }
        if (sToolsFragment != null){
            transaction.hide(sToolsFragment);
        }
        if (sMineFragment != null){
            transaction.hide(sMineFragment);
        }
    }
    //E: [PT-79] [Intranet Chat] [APP][UI] BottomNavigationBar 控件优化,Allen Luo,2019/11/13
    public static void startActivity(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        context.startActivity(intent);
    }

    private HandleInfo handleInfo = new HandleInfo() {

        @Override
        public void onReceiveVoiceCall(UserInfoBean userInfoBean, String host) {
            TLog.d(TAG, "onReceiveVoiceCall: isInCall = " + IntranetChatApplication.isInCall());
            if (IntranetChatApplication.isInCall()){
                VoiceCall.responseInCall(host);
                return;
            }
            EventBus.getDefault().post(new EventMessage(null,CALL_FROM_OTHER));
            IntranetChatApplication.setInCall(true);
            ContactEntity next = IntranetChatApplication.sContactMap.get(userInfoBean.getIdentifier());
            if(null != next){
                String name = next.getName();
                String avatarPath = next.getAvatarPath();
                AnswerVoiceCallActivity.go(MainActivity.this,host,name,avatarPath,userInfoBean.getIdentifier());
                return;
            }
        }

        @Override
        public void onReceiveVideoCall(UserInfoBean userInfoBean, String host) {
            TLog.d(TAG, "onReceiveVideoCall: isInCall = " + IntranetChatApplication.isInCall());
            if (IntranetChatApplication.isInCall()){
                VoiceCall.responseInCall(host);
                return;
            }
            EventBus.getDefault().post(new EventMessage(null,CALL_FROM_OTHER));
            IntranetChatApplication.setInCall(true);
            ContactEntity next = IntranetChatApplication.sContactMap.get(userInfoBean.getIdentifier());
            if(null != next){
                String name = next.getName();
                String avatarPath = next.getAvatarPath();
                AnswerVideoCallActivity.go(MainActivity.this,host,name,avatarPath,userInfoBean.getIdentifier());
                return;
            }
        }

        @Override
        public void onReceiveAndSaveFile(String sender, String receiver, String identifier, String path) {

        }

        @Override
        public void onReceiveFile(String fileJson, String host) {

        }
    };

    public static void go(Activity activity){
        Intent intent = new Intent(activity,MainActivity.class);
        activity.startActivity(intent);
    }

    //B: 监听网络状况 ,Oliver Ou,2019/11/15
    public BroadcastReceiver netReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                if (networkInfo != null && networkInfo.isAvailable()){
                    int type2 = networkInfo.getType();
                    String typeName = networkInfo.getTypeName();
                    switch (type2){
                        case 0:
//                            正在使用流量
                            IntranetChatApplication.setNetWorkState(false);
                            IntranetChatApplication.setsNetWorkType(0);
                            ToastUtil.toast(MainActivity.this, getString(R.string.MainActivity_BroadcastReceiver_flow_toast_text));
                            break;
                        case 1:
//                            正在使用wifi
                            if (!IntranetChatApplication.isNetWortState()){
                                IntranetChatApplication.setNetWorkState(true);
                                String host = IntranetChatServer.getHostIP();
                                if (!TextUtils.isEmpty(host) && !host.equals(IntranetChatApplication.getHostIp())){
                                    IntranetChatApplication.setHostIp(host);
                                    Login.hostChanged(host);
                                }
                                Login.login(IntranetChatApplication.getsMineUserInfo());
                            }
                            ToastUtil.toast(MainActivity.this, getString(R.string.MainActivity_BroadcastReceiver_wifi_toast_text));
                            break;
                        case 9:
                            IntranetChatApplication.setNetWorkState(false);
                            IntranetChatApplication.setsNetWorkType(0);
                            break;
                    }
                }else {
//                    无网络
                    IntranetChatApplication.setsNetWorkType(0);
                    IntranetChatApplication.setNetWorkState(false);
                    ToastUtil.toast(MainActivity.this, getString(R.string.MainActivity_BroadcastReceiver_nonet_toast_text));
                }
            }
        }
    };

    private void initReceiver() {
        IntentFilter timeFilter = new IntentFilter();
        timeFilter.addAction("android.net.ethernet.ETHERNET_STATE_CHANGED");
        timeFilter.addAction("android.net.ethernet.STATE_CHANGE");
        timeFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        timeFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        timeFilter.addAction("android.net.wifi.STATE_CHANGE");
        timeFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(netReceiver, timeFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (netReceiver != null){
            unregisterReceiver(netReceiver);
            netReceiver = null;
        }
    }

    //E: 监听网络状况 ,Oliver Ou,2019/11/15
}
