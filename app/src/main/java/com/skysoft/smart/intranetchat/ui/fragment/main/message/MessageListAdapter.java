/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/21
 * Description: [PT-40][Intranet Chat] [APP][UI] Home page ui
 */
package com.skysoft.smart.intranetchat.ui.fragment.main.message;

import android.content.Context;
import android.text.TextUtils;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.database.dao.LatestChatHistoryDao;
import com.skysoft.smart.intranetchat.tools.QuickClickListener;
import com.skysoft.smart.intranetchat.ui.SwipeLayout;
import com.skysoft.smart.intranetchat.database.MyDataBase;
import com.skysoft.smart.intranetchat.database.table.LatestChatHistoryEntity;
import com.skysoft.smart.intranetchat.tools.listsort.MessageListSort;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomActivity;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageListAdapter extends BaseAdapter {
    private final String TAG = "MessageListAdapter";

    private List<SwipeLayout> openList=new ArrayList<SwipeLayout>();
    private LayoutInflater inflater;
    //B:[PG1-Smart Team-CT] [PT-59] [Intranet Chat] [APP][UI] Chat Room,Oliver Ou,2019/10/30
    private List<String> myBeanList = null;
    private Context context;
    public MessageListAdapter(List<String> myBeanList,Context context) {
        this.myBeanList = myBeanList;
        inflater = LayoutInflater.from(context);
        this.notifyDataSetChanged();
        this.context = context;
    }
    //E: [PG1-Smart Team-CT] [PT-59] [Intranet Chat] [APP][UI] Chat Room,Oliver Ou,2019/10/30

    @Override
    public int getCount() {
        return myBeanList == null?0:myBeanList.size();
    }

    @Override
    public Object getItem(int position) {
        TLog.d(TAG, "getItem: " + myBeanList.get(position));
        return IntranetChatApplication.sLatestChatHistoryMap.get(myBeanList.get(position));
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
//B: [PT-60][Intranet Chat] [APP][UI] contact list page,Allen Luo,2019/10/30
        LatestChatHistoryEntity item = (LatestChatHistoryEntity) getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listview_main_message, null);

            /*得到各个控件的对象*/
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.message = (TextView) convertView.findViewById(R.id.message);
            holder.messageTime = (TextView) convertView.findViewById(R.id.message_time); // to ItemButton
            holder.swipeLayout = (SwipeLayout) convertView.findViewById(R.id.list_message_item);
            holder.delete = (TextView) convertView.findViewById(R.id.delete);
            holder.top = (TextView) convertView.findViewById(R.id.top);
            holder.messageItem = (LinearLayout) convertView.findViewById(R.id.message_list_item) ;
            //B: 去掉消息列表展示联系人状态，Allen Luo，2019/11/13
            //holder.userState = (View) convertView.findViewById(R.id.user_state);
            //E: 去掉消息列表展示联系人状态，Allen Luo，2019/11/13
            holder.unreadNumber = convertView.findViewById(R.id.unread_number);
            holder.head = convertView.findViewById(R.id.head);
            convertView.setTag(holder); //绑定ViewHolder对象
        }
        else {
            holder = (ViewHolder) convertView.getTag(); //取出ViewHolder对象
        }
        //B: 去掉消息列表展示联系人状态，Allen Luo，2019/11/13
        //initState(position,holder);
        //E: 去掉消息列表展示联系人状态，Allen Luo，2019/11/13
        initView(position,holder,convertView);
        setView(holder,item);
        holder.messageItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (QuickClickListener.isFastClick()){
                    boolean group = false;
                    if(item.getGroup() == 1){
                        group = true;
                    }
                    int number = IntranetChatApplication.getmTotalUnReadNumber() - item.getUnReadNumber();
                    item.setUnReadNumber(0);
                    holder.unreadNumber.setText(String.valueOf(0));
                    holder.unreadNumber.setVisibility(View.GONE);
                    if (number == 0){
                        //B: [PT-80][Intranet Chat] [APP][UI] TextBadgeItem 一直为红色,Allen Luo,2019/11/12
                        IntranetChatApplication.getmTextBadgeItem().hide();
                    }else {
                        IntranetChatApplication.getmTextBadgeItem().show().setText(String.valueOf(number));
                        //E: [PT-80][Intranet Chat] [APP][UI] TextBadgeItem 一直为红色,Allen Luo,2019/11/12
                    }
                    IntranetChatApplication.setmTotalUnReadNumber(number);
                    ChatRoomActivity.go(context,item.getUserName(),item.getUserHeadPath(),item.getHost(),item.getUserIdentifier(),group);
                }
            }
        });

        return convertView;
    }
    //E:[Intranet Chat] [APP][UI] Chat Room Oliver Ou 2019/10/31

    private class ViewHolder {
        TextView delete;
        TextView top;
        TextView name;
        CircleImageView head;
        TextView message;
        TextView messageTime;
        //B: 去掉消息列表展示联系人状态，Allen Luo，2019/11/13
        //View userState;
        //E: 去掉消息列表展示联系人状态，Allen Luo，2019/11/13
        TextView unreadNumber;
        SwipeLayout swipeLayout;
        LinearLayout messageItem;
    }
//B: 去掉消息列表展示联系人状态，Allen Luo，2019/11/13
//    public void initState(int position, ViewHolder holder){
//        if (myBeanList.get(position).getStatus() == 3){
//            holder.userState.setBackgroundResource(R.drawable.bg_circle_gray);
//        }else if (myBeanList.get(position).getStatus() == 1){
//            holder.userState.setBackgroundResource(R.drawable.bg_circle_green);
//        }else {
//            holder.userState.setBackgroundResource(R.drawable.bg_circle_red);
//        }
//    }
//E: 去掉消息列表展示联系人状态，Allen Luo，2019/11/13

    public void initView(int position, ViewHolder holder, View convertView){
        if (IntranetChatApplication.sLatestChatHistoryMap.get(myBeanList.get(position)).getTop() == 0) {
            holder.top.setText("置顶");
            holder.messageItem.setBackground(convertView.getResources().getDrawable(R.drawable.fragment_message_list_item_selector_default));
        }else {
            holder.top.setText("取消置顶");
            holder.messageItem.setBackground(convertView.getResources().getDrawable(R.drawable.fragment_message_list_item_selector_top));
        }

        holder.swipeLayout.setSwipeChangeListener(new SwipeLayout.OnSwipeChangeListener() {

            @Override
            public void onStartOpen(SwipeLayout mSwipeLayout) {
                TLog.i(TAG, "onStartOpen: 正在打开？");
                for(SwipeLayout layout:openList){
                    layout.close();
                }
                openList.clear();
            }

            @Override
            public void onStartClose(SwipeLayout mSwipeLayout) {

            }

            @Override
            public void onOpen(SwipeLayout mSwipeLayout) {
                openList.add(mSwipeLayout);
                holder.delete.setOnClickListener(new View.OnClickListener(){

                    public void onClick(View view){
                        LatestChatHistoryEntity historyEntity = IntranetChatApplication.sLatestChatHistoryMap.get(myBeanList.get(position));
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                LatestChatHistoryDao latestChatHistoryDao = MyDataBase.getInstance().getLatestChatHistoryDao();
                                LatestChatHistoryEntity history = latestChatHistoryDao.getHistory(historyEntity.getUserIdentifier());
                                latestChatHistoryDao.delete(history);
                            }
                        }).start();
                        int unReadNumber = historyEntity.getUnReadNumber();
                        int number = IntranetChatApplication.getmTotalUnReadNumber() - unReadNumber;
                        if (number == 0){
                            //B: [PT-80][Intranet Chat] [APP][UI] TextBadgeItem 一直为红色,Allen Luo,2019/11/12
                            IntranetChatApplication.getmTextBadgeItem().hide();
                        }else {
                            IntranetChatApplication.getmTextBadgeItem().show().setText(String.valueOf(number));
                            //E: [PT-80][Intranet Chat] [APP][UI] TextBadgeItem 一直为红色,Allen Luo,2019/11/12
                        }
                        IntranetChatApplication.setmTotalUnReadNumber(number);
                        String remove = myBeanList.remove(position);
                        IntranetChatApplication.sLatestChatHistoryMap.remove(remove);
                        onStartOpen(mSwipeLayout);
                        //删除一条数据记录
                        MessageListAdapter.this.notifyDataSetChanged();
                    }
                });
                holder.top.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (QuickClickListener.isFastClick()) {
                            LatestChatHistoryEntity messageBean = IntranetChatApplication.sLatestChatHistoryMap.get(myBeanList.get(position));
                            if (messageBean.getTop() == 0) {
                                messageBean.setTop(1);
                            } else {
                                messageBean.setTop(0);
                            }
                            MessageListSort.CollectionsList(myBeanList);
                            onStartOpen(mSwipeLayout);
                            MessageListAdapter.this.notifyDataSetChanged();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    MyDataBase.getInstance().getLatestChatHistoryDao().update(messageBean);
                                }
                            }).start();
                        }
                    }
                });
            }

            @Override
            public void onDraging(SwipeLayout mSwipeLayout) {

            }

            @Override
            public void onClose(SwipeLayout mSwipeLayout) {
                openList.remove(mSwipeLayout);
            }
        });
    }

    //B: [PT-60][Intranet Chat] [APP][UI] contact list page,Oliver Ou,2019/10/31
    public void setView(ViewHolder holder,LatestChatHistoryEntity messageBean){
        //if(myBeanList.get(position).getAvatarPath != null)
        holder.name.setText(String.valueOf(messageBean.getUserName()));
        holder.message.setText(messageBean.getContent());
        holder.messageTime.setText(messageBean.getContentTime());
        if (messageBean.getUnReadNumber() == 0){
            holder.unreadNumber.setVisibility(View.GONE);
        }else {
            holder.unreadNumber.setText(String.valueOf(messageBean.getUnReadNumber()));
            holder.unreadNumber.setVisibility(View.VISIBLE);
        }
        //B: [PT-78][Intranet Chat] [APP][UI]  联系人列表和消息列表头像串行,Allen Luo,2019/11/12
        if (!TextUtils.isEmpty(messageBean.getUserHeadPath())){
            Glide.with(context).load(messageBean.getUserHeadPath()).into(holder.head);
        }else {
            Glide.with(context).load(R.drawable.default_head).into(holder.head);
        }
        //E: [PT-78][Intranet Chat] [APP][UI]  联系人列表和消息列表头像串行,Allen Luo,2019/11/12
        holder.delete.setText(R.string.MessageListAdapter_delete);
    }
    //E: [PT-60][Intranet Chat] [APP][UI] contact list page,Oliver Ou,2019/10/31
//E: [PT-60][Intranet Chat] [APP][UI] contact list page,Allen Luo,2019/10/30
}