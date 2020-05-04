/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/30
 * Description: [PT-60][Intranet Chat] [APP][UI] contact list page
 */
package com.skysoft.smart.intranetchat.ui.fragment.main.contact;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.database.table.ContactEntity;
import com.skysoft.smart.intranetchat.tools.QuickClickListener;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomActivity;
import com.skysoft.smart.intranetchat.ui.activity.userinfoshow.UserInfoShowActivity;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactListAdapter extends BaseAdapter {
    private final String TAG = "ContactListAdapter";

    private LayoutInflater inflater;
    private List<String> contactBeanList = null;
    //B:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/10/31
    private Context context;
    public ContactListAdapter(List<String> contactBeanList, Context context) {
        this.context = context;
        this.contactBeanList = contactBeanList;
        inflater = LayoutInflater.from(context);
        this.notifyDataSetChanged();
    }
    //E:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/10/31

    @Override
    public int getCount() {
        return contactBeanList == null?0: contactBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        return IntranetChatApplication.sContactMap.get(contactBeanList.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //B:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/11/4
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    //E:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/11/4

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ContactEntity contactBean = (ContactEntity) getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listview_main_contact, null);
            /*得到各个控件的对象*/
            holder.name = (TextView) convertView.findViewById(R.id.contact_name);
//            holder.swipeLayout = (SwipeLayout) convertView.findViewById(R.id.list_message_item);
            holder.contactItem = (ConstraintLayout) convertView.findViewById(R.id.contact_list_item);
            holder.userState = (View) convertView.findViewById(R.id.contact_state);
            holder.head = convertView.findViewById(R.id.contact_head);
            //E:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/11/6
            //E:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/10/31

            convertView.setTag(holder); //绑定ViewHolder对象
        }else {
            holder = (ContactListAdapter.ViewHolder) convertView.getTag(); //取出ViewHolder对象
        }

        //B:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/10/31
        switch (IntranetChatApplication.sContactMap.get(contactBeanList.get(position)).getStatus()){
            case 3:
                //B: [PT-77][Intranet Chat] [APP][UI] 联系人离线后状态图标颜色错误,Allen Luo,2019/11/12
                holder.userState.setBackgroundResource(R.drawable.bg_circle_gray);
                //E: [PT-77][Intranet Chat] [APP][UI] 联系人离线后状态图标颜色错误,Allen Luo,2019/11/12
                break;
            case 2:
                holder.userState.setBackgroundResource(R.drawable.bg_circle_red);
                break;
            case 1:
                holder.userState.setBackgroundResource(R.drawable.bg_circle_green);
                break;
        }
        //E:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/10/31
        //if(contactBeanList.get(position).getAvatarPath != null)
        holder.name.setText(String.valueOf(contactBean.getName()));
        //B: [PT-78][Intranet Chat] [APP][UI]  联系人列表和消息列表头像串行,Allen Luo,2019/11/12
        if (!TextUtils.isEmpty(contactBean.getAvatarPath())){
            Glide.with(context).load(contactBean.getAvatarPath()).into(holder.head);
        }else {
            Glide.with(context).load(R.drawable.default_head).into(holder.head);
        }
        //E: [PT-78][Intranet Chat] [APP][UI]  联系人列表和消息列表头像串行,Allen Luo,2019/11/12
        holder.contactItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (QuickClickListener.isFastClick()) {
                    ChatRoomActivity.go(context, contactBean.getName(), contactBean.getAvatarPath(), contactBean.getHost(), contactBean.getIdentifier(), false);
                }
            }
        });

        holder.head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserInfoShowActivity.go(context,contactBean.getName(),contactBean.getAvatarPath(), contactBean.getIdentifier());
//                ,String name,String avatar,String identifier
            }
        });
        return convertView;
    }

//    public void updateData(List<MessageBean> sessionList) {
//        sessionList.clear();
//        sessionList.addAll(sessionList);
//    }

    private class ViewHolder {
        TextView name;
        CircleImageView head;
        View userState;
//        SwipeLayout swipeLayout;
        ConstraintLayout contactItem;
    }
}
