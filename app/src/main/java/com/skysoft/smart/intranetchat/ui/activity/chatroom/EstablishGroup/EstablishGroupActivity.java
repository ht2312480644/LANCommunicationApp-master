/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/6
 * Description: [Intranet Chat] [APP][UI] Group chat
 */
package com.skysoft.smart.intranetchat.ui.activity.chatroom.EstablishGroup;

import androidx.annotation.Nullable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.app.BaseActivity;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.bean.GroupMemberList;
import com.skysoft.smart.intranetchat.database.MyDataBase;
import com.skysoft.smart.intranetchat.database.table.ContactEntity;
import com.skysoft.smart.intranetchat.database.table.GroupMemberEntity;
import com.skysoft.smart.intranetchat.model.network.bean.EstablishGroupBean;
import com.skysoft.smart.intranetchat.tools.QuickClickListener;
import com.skysoft.smart.intranetchat.tools.toastutil.ToastUtil;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomActivity;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomConfig;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EstablishGroupActivity extends BaseActivity {
    private static String TAG = EstablishGroupActivity.class.getSimpleName();
    private ListView mContactListView;
    private EstablishGroupAdapter mAdapter;
    private TextView mCancel;
    private TextView mConfirm;
    private String receiverIdentifier;
    private boolean isGroup;
    private List<GroupMemberEntity> groupMemberEntities;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_establish_group);
        setStatusView(R.id.custom_status_bar_background);
        mContactListView = findViewById(R.id.establish_group_list);
        mCancel = findViewById(R.id.establish_group_cancel);
        mConfirm = findViewById(R.id.establish_group_confirm);
        EventBus.getDefault().register(this);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        isGroup = bundle.getBoolean(ChatRoomConfig.GROUP);
        receiverIdentifier = bundle.getString(ChatRoomConfig.IDENTIFIER);
        List<ContactEntity> contactEntityList = new ArrayList<>();
        TLog.d(TAG, "onCreate: isGroup = " + isGroup);
        if (isGroup){
            //查看群成员
            mConfirm.setVisibility(View.GONE);
            mCancel.setText(getText(R.string.back));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<GroupMemberEntity> allGroupMember = MyDataBase.getInstance().getGroupMemberDao().getAllGroupMember(receiverIdentifier);
                    GroupMemberList groupMembersBean = new GroupMemberList(allGroupMember);
                    EventBus.getDefault().post(groupMembersBean);
                }
            }).start();
        }else {
            //新建群
            Iterator<String> iterator = IntranetChatApplication.getsContactList().iterator();
            while (iterator.hasNext()){
                ContactEntity next = IntranetChatApplication.sContactMap.get(iterator.next());
                next.setCheck(false);
                next.setShowCheck(true);
                contactEntityList.add(next);
            }
        }
        mAdapter = new EstablishGroupAdapter(contactEntityList,this);
        mContactListView.setAdapter(mAdapter);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (QuickClickListener.isFastClick()) {
                    List<ContactEntity> selectContacts = mAdapter.getGroup();
                    TLog.d(TAG, "onClick: selectContacts.size() = " + selectContacts.size());
                    if (selectContacts.size() < 2) {
                        ToastUtil.toast(EstablishGroupActivity.this, getString(R.string.build_group_number));
                        return;
                    }

                    TLog.d(TAG, "onClick: selectContacts.size() = " + selectContacts.size());
                    setGroupName(selectContacts);
                }
            }
        });
    }

    private void setGroupName(List<ContactEntity> selectContacts){
        View inflate = LayoutInflater.from(this).inflate(R.layout.dialog_input_name, null);
        TextView inputNewName = inflate.findViewById(R.id.dialog_input);
        //默认群名
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(IntranetChatApplication.getsMineUserInfo().getName());
        stringBuilder.append("、");
        Iterator<ContactEntity> iterator = selectContacts.iterator();
        int i = 0;
        while (iterator.hasNext()){
            ContactEntity next = iterator.next();
            if (stringBuilder.length() + next.getName().length() > 20){
                stringBuilder.append("...");
                break;
            }
            if (i == 3){
                break;
            }
            stringBuilder.append(next.getName());
            stringBuilder.append("、");
            i++;
        }
        stringBuilder.delete(stringBuilder.length() - 1,stringBuilder.length());
        inputNewName.setText(stringBuilder.toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alertDialog = builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = inputNewName.getText().toString();
                //群名不能为空，并且不能以自己的名字命名
                if (TextUtils.isEmpty(newName) || newName.equals(IntranetChatApplication.getsMineUserInfo().getName())){
                    return;
                }
                if (newName.length() <= 4 || newName.length() > 23){
                    ToastUtil.toast(EstablishGroupActivity.this, getString(R.string.limit_name_length));
                    return;
                }
                if (!IntranetChatApplication.isNetWortState()){
                    ToastUtil.toast(EstablishGroupActivity.this, getString(R.string.Toast_text_non_lan));
                }
                EstablishGroupBean establish = EstablishGroupAdapter.establish(selectContacts, newName, IntranetChatApplication.getsMineUserInfo().getAvatarIdentifier());
                if (establish != null && establish.isRemark()){
                    ChatRoomActivity.go(EstablishGroupActivity.this,establish.getmGroupName(),establish.getmGroupAvatarIdentifier(),"255.255.255.255",establish.getmGroupIdentifier(),true);
                }
                finish();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setView(inflate);
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        IntranetChatApplication.setEstablishGroupAdapter(null);
    }

    public static void go(Activity activity, String receiverIdentifier, boolean group){
        Intent intent = new Intent(activity,EstablishGroupActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(ChatRoomConfig.IDENTIFIER,receiverIdentifier);
        bundle.putBoolean(ChatRoomConfig.GROUP,group);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveGroupMembers(GroupMemberList membersBean){
        groupMemberEntities = membersBean.getMemberEntities();
        mAdapter.showGroupMember(membersBean.getMemberEntities());
        IntranetChatApplication.setEstablishGroupAdapter(mAdapter);
    }
}
