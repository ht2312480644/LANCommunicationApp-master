/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/6
 * Description: [Intranet Chat] [APP][UI] Group chat
 */
package com.skysoft.smart.intranetchat.ui.activity.chatroom.EstablishGroup;

import android.content.Context;
import android.text.TextUtils;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.model.net_model.EstablishGroup;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.database.table.ContactEntity;
import com.skysoft.smart.intranetchat.database.table.GroupMemberEntity;
import com.skysoft.smart.intranetchat.database.table.LatestChatHistoryEntity;
import com.skysoft.smart.intranetchat.model.network.bean.EstablishGroupBean;
import com.skysoft.smart.intranetchat.model.network.bean.UserInfoBean;
import com.skysoft.smart.intranetchat.tools.Identifier;
import com.skysoft.smart.intranetchat.tools.QuickClickListener;
import com.skysoft.smart.intranetchat.tools.toastutil.ToastUtil;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomActivity;
import com.skysoft.smart.intranetchat.ui.activity.userinfoshow.UserInfoShowActivity;
import com.skysoft.smart.intranetchat.ui.fragment.main.message.MessageListAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;

public class EstablishGroupAdapter extends BaseAdapter {
    private static final String TAG = "ContactListAdapter";

    private LayoutInflater mInflater;
    private List<ContactEntity> mContactBeanList = new ArrayList<>();
    private static Context mContext;
    private boolean isGroup = false;

    public EstablishGroupAdapter(List<ContactEntity> mContactBeanList, Context mContext) {
        this.mContext = mContext;
        this.mContactBeanList.addAll(mContactBeanList);
        mInflater = LayoutInflater.from(mContext);
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mContactBeanList == null?0: mContactBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return mContactBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ContactEntity contactBean = (ContactEntity) getItem(position);
        EstablishGroupAdapter.ViewHolder holder;
        if (convertView == null) {
            holder = new EstablishGroupAdapter.ViewHolder();
            convertView = mInflater.inflate(R.layout.listview_main_contact, null);

            /*得到各个控件的对象*/
            holder.name = (TextView) convertView.findViewById(R.id.contact_name);
            holder.contactItem = (ConstraintLayout) convertView.findViewById(R.id.contact_list_item) ;
            holder.userState = (View) convertView.findViewById(R.id.contact_state);
            holder.head = convertView.findViewById(R.id.contact_head);
            holder.checkBox = convertView.findViewById(R.id.contact_check);

            convertView.setTag(holder); //绑定ViewHolder对象
        }else {
            holder = (EstablishGroupAdapter.ViewHolder) convertView.getTag(); //取出ViewHolder对象
        }
        //不加载离线用户
        holder.checkBox.setVisibility(View.VISIBLE);
        holder.name.setText(String.valueOf(contactBean.getName()));

        if (!TextUtils.isEmpty(contactBean.getAvatarPath())){
            Glide.with(mContext).load(contactBean.getAvatarPath()).into(holder.head);
        }else {
            Glide.with(mContext).load(R.drawable.default_head).into(holder.head);
        }

        if (!isGroup){
            holder.checkBox.setVisibility(View.VISIBLE);
        }else if (!contactBean.isShowCheck()){
            holder.checkBox.setVisibility(View.GONE);
        }

        holder.checkBox.setChecked(contactBean.isCheck());

        holder.contactItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGroup) {
                    if (QuickClickListener.isFastClick(300)) {
                        UserInfoShowActivity.go(mContext, contactBean.getName(), contactBean.getAvatarPath(), contactBean.getIdentifier());
                    }
                }
                if (contactBean.isShowCheck()){
                    contactBean.setCheck(!holder.checkBox.isChecked());
                    holder.checkBox.setChecked(!holder.checkBox.isChecked());
                }
            }
        });
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                TLog.d(TAG, "onCheckedChanged: " + isChecked);
                holder.checkBox.setChecked(isChecked);
                contactBean.setCheck(isChecked);
            }
        });
        return convertView;
    }

    private class ViewHolder {
        TextView name;
        ImageView head;
        View userState;
        ConstraintLayout contactItem;
        CheckBox checkBox;
    }

    public List<ContactEntity> getGroup(){
        List<ContactEntity> selectContact = new ArrayList<>();
        Iterator<ContactEntity> iterator = mContactBeanList.iterator();
        while (iterator.hasNext()){
            ContactEntity next = iterator.next();
            if (next.isCheck()){
                selectContact.add(next);
            }
        }
        return selectContact;
    }

    public static EstablishGroupBean establish(List<ContactEntity> selectContacts,String groupName,String groupAvatarIdentifier){
        List<UserInfoBean> group = new ArrayList<>();
        List<String> hostList = new ArrayList<>();
        if (selectContacts.size() == 0){
            return null;
        }
        Iterator<ContactEntity> iterator = selectContacts.iterator();
        String[] identifiers = new String[selectContacts.size() + 1];
        int i = 0;
        //生成群用户
        while (iterator.hasNext()){
            ContactEntity next = iterator.next();
            group.add(EstablishGroup.adapter(next));
            hostList.add(next.getHost());
            identifiers[i++] = next.getIdentifier();
        }
        identifiers[identifiers.length - 1] = IntranetChatApplication.getsMineUserInfo().getIdentifier();
        group.add(IntranetChatApplication.getsMineUserInfo());
        EstablishGroupBean establishGroupBean = new EstablishGroupBean();
        //设置群唯一标识符
        Identifier identifier = new Identifier();
        String groupIdentifier = identifier.getGroupIdentifier(identifiers);
        ContactEntity next = IntranetChatApplication.sGroupContactMap.get(groupIdentifier);
        if (null != next){
            ToastUtil.toast(mContext,mContext.getString(R.string.repeated_construction_group));
            establishGroupBean.setmName(next.getName());
            establishGroupBean.setmGroupIdentifier(groupIdentifier);
            establishGroupBean.setmGroupAvatarIdentifier(next.getAvatarPath());
            establishGroupBean.setRemark(true);
            return establishGroupBean;
        }
        //发送群建立通知

        establishGroupBean.setmUsers(group);
        establishGroupBean.setmGroupIdentifier(groupIdentifier);
        establishGroupBean.setmName(groupName);
        establishGroupBean.setmHolderIdentifier(IntranetChatApplication.getsMineUserInfo().getIdentifier());
        establishGroupBean.setmGroupAvatarIdentifier(groupAvatarIdentifier);
        EstablishGroup.establishGroup(establishGroupBean,hostList);
        //显示群聊
        LatestChatHistoryEntity latestChatHistoryEntity = EstablishGroup.LatestChatFromGroup(establishGroupBean);
        String content = establishGroupBean.getmGroupName() + "成为好友，请开始聊天吧";
        latestChatHistoryEntity.setContent(content);
        //记录此消息
        IntranetChatApplication.getMessageList().add(latestChatHistoryEntity.getUserIdentifier());
        IntranetChatApplication.sLatestChatHistoryMap.put(latestChatHistoryEntity.getUserIdentifier(),latestChatHistoryEntity);
        //刷新消息
        MessageListAdapter messageListAdapter = IntranetChatApplication.getsMessageListAdapter();
        if (messageListAdapter != null){
            messageListAdapter.notifyDataSetChanged();
        }
        //存数据
        latestChatHistoryEntity.setGroup(1);
        EstablishGroup.storageGroup(latestChatHistoryEntity,establishGroupBean,false,false);
        EstablishGroup.addGroupToList(establishGroupBean);
        if (IntranetChatApplication.getsMessageListAdapter() != null){
            IntranetChatApplication.getsMessageListAdapter().notifyDataSetChanged();
        }
        ChatRoomActivity.go(mContext,establishGroupBean.getmGroupName()
                ,IntranetChatApplication.getsMineAvatarPath(),"255.255.255.255"
                ,establishGroupBean.getmGroupIdentifier(),true);
        return establishGroupBean;
    }

    public void showGroupMember(List<GroupMemberEntity> memberEntities){
        TLog.d(TAG, "showGroupMember: memberEntities.size() = " + memberEntities.size());
        isGroup = true;
        Iterator<GroupMemberEntity> iterator = memberEntities.iterator();
        while (iterator.hasNext()){
            GroupMemberEntity next = iterator.next();
            ContactEntity contactEntity = IntranetChatApplication.sContactMap.get(next.getGroupMemberIdentifier());
            if (null != contactEntity){
                contactEntity.setCheck(true);
                contactEntity.setShowCheck(false);
                mContactBeanList.add(contactEntity);
            }
        }
        notifyDataSetChanged();
    }
}
