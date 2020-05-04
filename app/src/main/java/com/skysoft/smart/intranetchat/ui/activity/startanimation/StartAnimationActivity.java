/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/15
 * Description: [PT-38][Intranet Chat] [APP][UI]Program launch page and animation
 */
package com.skysoft.smart.intranetchat.ui.activity.startanimation;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import com.skysoft.smart.intranetchat.MainActivity;
import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.model.net_model.Login;
import com.skysoft.smart.intranetchat.database.MyDataBase;
import com.skysoft.smart.intranetchat.database.table.ContactEntity;
import com.skysoft.smart.intranetchat.database.table.EquipmentInfoEntity;
import com.skysoft.smart.intranetchat.database.table.FileEntity;
import com.skysoft.smart.intranetchat.database.table.LatestChatHistoryEntity;
import com.skysoft.smart.intranetchat.database.table.MineInfoEntity;
import com.skysoft.smart.intranetchat.database.table.RefuseGroupEntity;
import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.server.IntranetChatServer;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomActivity;
import com.skysoft.smart.intranetchat.ui.activity.login.LoginActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class StartAnimationActivity extends AppCompatActivity {
    private final String TAG = "StartAnimationActivity";

    @BindView(R.id.start_animation_logo)
    ImageView sAnimation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startanimation);
        CustomStatusBarBackground.customStatusBarTransparent(this);
        ButterKnife.bind(this);

        rotateyAnimRun(sAnimation);

        File images = this.getExternalFilesDir("images");
        File avatar = this.getExternalFilesDir("avatar");
        File voice = this.getExternalFilesDir("voice");
        File video = this.getExternalFilesDir("video");
        File receive = this.getExternalFilesDir("receive");

        Timer timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MineInfoEntity mineInfoEntity = MyDataBase.getInstance().getMineInfoDao().Query();
                if (mineInfoEntity != null){
                    TLog.d(TAG, "run: " + mineInfoEntity.toString());
                    IntranetChatApplication.setMineUserInfo(mineInfoEntity,mineInfoEntity.getMineName());
                    Login.login(IntranetChatApplication.getsMineUserInfo());
                    MainActivity.go(StartAnimationActivity.this);
                    finish();
                    return;
                }
                Intent login = new Intent(StartAnimationActivity.this, LoginActivity.class);
                startActivity(login);
                finish();
            }
        },2001);

        new Thread(new Runnable() {
            @Override
            public void run() {
                String host = IntranetChatServer.getHostIP();
                IntranetChatApplication.setHostIp(host);
                EquipmentInfoEntity equipmentInfo = MyDataBase.getInstance().getEquipmentInfoDaoDao().getEquipmentInfo();
                if (equipmentInfo != null){
                    IntranetChatApplication.setsEquipmentInfoEntity(equipmentInfo);
                    TLog.d(TAG, "run: equipmentInfo = " + equipmentInfo.toString());
                }
                List<LatestChatHistoryEntity> allHistory = MyDataBase.getInstance().getLatestChatHistoryDao().getAllHistory();
                List<ContactEntity> allContact = MyDataBase.getInstance().getContactDao().getAllContact();
                List<String> contactEntities = new ArrayList<>();
                List<String> groupContactEntities = new ArrayList<>();
                Iterator<ContactEntity> iterator = allContact.iterator();
                Map<String ,String > identifiers = new HashMap<>();
                List<ContactEntity> deleteContact = new ArrayList<>();
                int i = 0;
                while (iterator.hasNext()){     //便利数据库获得的联系人
                    ContactEntity next = iterator.next();
                    next.setNotifyId(i++);      //联系人消息通知的唯一ID

                    identifiers.put(next.getIdentifier(),next.getAvatarPath());
                    if (next.getGroup() == 0){
                        contactEntities.add(next.getIdentifier());
                        //记录此联系人
                        IntranetChatApplication.sContactMap.put(next.getIdentifier(),next);
                    }else {
                        groupContactEntities.add(next.getIdentifier());
                        //记录此群
                        IntranetChatApplication.sGroupContactMap.put(next.getIdentifier(),next);
                    }
                }
                //添加联系人
                IntranetChatApplication.setsGroupContactList(groupContactEntities);
                IntranetChatApplication.initContactList(contactEntities);

                Iterator<LatestChatHistoryEntity> historyIterator = allHistory.iterator();      //便利数据库的聊天消息
                List<String> history = new ArrayList<>();
                int total = 0;
                while (historyIterator.hasNext()){
                    LatestChatHistoryEntity next = historyIterator.next();
                    total += next.getUnReadNumber();
                    if (next.getContentTimeMill() != 0 && ChatRoomActivity.initMillToTmie(next.getContentTimeMill())){
                        next.setContentTime(ChatRoomActivity.millToFullTime(next.getContentTimeMill()));
                    }
                    history.add(next.getUserIdentifier());
                    IntranetChatApplication.sLatestChatHistoryMap.put(next.getUserIdentifier(),next);
                }
                //设置总的未读数
                IntranetChatApplication.setmTotalUnReadNumber(total);
                IntranetChatApplication.initLatestChatHistoryList(history);     //初始化消息界面
                TLog.d(TAG, "run: allHistory.size() = " + allHistory.size() + ", allContact.size() = " + allContact.size());
                //获取拒绝接收群名单
                List<RefuseGroupEntity> all = MyDataBase.getInstance().getRefuseGroupDao().getAll();
                IntranetChatApplication.setsRefuseGroupList(all);
                //清楚多余的联系人
                if (deleteContact.size() != 0){
                    Iterator<ContactEntity> deletes = deleteContact.iterator();
                    while (deletes.hasNext()){
                        TLog.d(TAG, "run: delete: " + deletes.next().toString());
                    }
                    MyDataBase.getInstance().getContactDao().delete(deleteContact);
                }
                //获取历史未下载成功文件
                List<FileEntity> allFailure = MyDataBase.getInstance().getFileDao().getAllFailure(System.currentTimeMillis() - 10 * 60 * 1000);
                Map<String ,FileEntity> downFailure = new HashMap<>();
                if (allFailure != null && allFailure.size() != 0){
                    for (FileEntity entity : allFailure){
                        entity.setType(Config.STEP_DOWN_LOAD_FAILURE);
                        downFailure.put(entity.getIdentifier(),entity);
                    }
                    IntranetChatApplication.initMonitorReceiveFile(downFailure);
                    MyDataBase.getInstance().getFileDao().updateList(allFailure);
                }
            }
        }).start();
    }

    public void rotateyAnimRun(View view)
    {
        AnimatorSet set=new AnimatorSet();
        ObjectAnimator animatorTranslate=ObjectAnimator.ofFloat(sAnimation,"translationY",0,0);
        ObjectAnimator animatorScaleX=ObjectAnimator.ofFloat(sAnimation,"ScaleX",1f,2f);
        ObjectAnimator animatorScaleY=ObjectAnimator.ofFloat(sAnimation,"ScaleY",1f,2f);
        ObjectAnimator animatorAlpha=ObjectAnimator.ofFloat(sAnimation,"alpha",1f,2f);
        set.play(animatorTranslate)
                .with(animatorScaleX).with(animatorScaleY).with(animatorAlpha);
        set.setDuration(2000);
        set.setInterpolator(new AccelerateInterpolator());
        set.start();
    }
    public void onBackPressed(){
    }
}
