package com.skysoft.smart.intranetchat.ui.activity.chatroom.PopupWindow;

import androidx.constraintlayout.widget.ConstraintLayout;
import de.hdodenhof.circleimageview.CircleImageView;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.app.BaseActivity;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.bean.SendMessageBean;
import com.skysoft.smart.intranetchat.database.MyDataBase;
import com.skysoft.smart.intranetchat.database.dao.ChatRecordDao;
import com.skysoft.smart.intranetchat.model.net_model.SendMessage;
import com.skysoft.smart.intranetchat.bean.TransmitBean;
import com.skysoft.smart.intranetchat.database.table.ChatRecordEntity;
import com.skysoft.smart.intranetchat.database.table.LatestChatHistoryEntity;
import com.skysoft.smart.intranetchat.model.camera.entity.EventMessage;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomConfig;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomMessageAdapter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TransmitActivity extends BaseActivity implements View.OnClickListener{
    private static final String TAG = "TransmitActivity";

    public static void startActivity(Context context, String message, int recordType, String transmitRoomIdentifier){
        Intent intent = new Intent(context,TransmitActivity.class);
        intent.putExtra("message",message);
        intent.putExtra("recordType",recordType);
        intent.putExtra("transmitRoomIdentifier",transmitRoomIdentifier);
        context.startActivity(intent);
    }

    private TextView mClose;
    private ConstraintLayout mSearchBox;
    private LinearLayout mRecentlyChat;
    private List<TransmitBean> mTransmitUsers;      //转发界面的最近聊天列表
    private String mMessage;
    private int mRecordType;        //转发消息的内型
    private String mTransmitRoomIdentifier;     //转发消息的聊天室Identifier
    private ScrollView mScroll;     //title以下的内容
    private ConstraintLayout mTitle;
    private LinearLayout mSearchInputBox;
    private LinearLayout mSearchResultBox;
    private ListView mSearchResultList;     //搜索结果的list
    private TextView mNoMoreResult;     //没有更多搜索结果
    private EditText mInputSearchKey;
    private ImageView mClearInputSearchKey;   //删除搜索框输入的内容
    private TextView mCancelSearch;     //取消搜索

    private SearchResultAdapter mAdapter;

    private TextWatcher mInputSearchKeyListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            TLog.d(TAG, "afterTextChanged: s = " + s.toString());
            if (!TextUtils.isEmpty(s.toString())){
                mSearchResultBox.setBackgroundColor(getResources().getColor(R.color.color_white));
                mNoMoreResult.setVisibility(View.VISIBLE);
                if(mSearchResultList.getVisibility() == View.GONE){
                    mSearchResultList.setVisibility(View.VISIBLE);
                }
            }else {
                mSearchResultBox.setBackgroundColor(getResources().getColor(R.color.color_light_black));
                mNoMoreResult.setVisibility(View.GONE);
                mSearchResultList.setVisibility(View.GONE);
            }

            mAdapter.onInputSearchKeyChange(s.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transmit);
        setStatusView(R.id.custom_status_bar_background);
        getIntentContent();
        initView();
        initData();
    }

    private void getIntentContent() {
        Intent intent = getIntent();
        mMessage = intent.getStringExtra("message");
        mRecordType = intent.getIntExtra("recordType",-1);
        mTransmitRoomIdentifier = intent.getStringExtra("transmitRoomIdentifier");
    }

    private void initView() {
        mClose = findViewById(R.id.close_transmit);
        mSearchBox = findViewById(R.id.transmit_search_box);
        mRecentlyChat = findViewById(R.id.transmit_recently_chat);
        mScroll = findViewById(R.id.transmit_scroll);
        mTitle = findViewById(R.id.transmit_title);
        mSearchInputBox = findViewById(R.id.transmit_search_input_box);
        mSearchResultBox = findViewById(R.id.transmit_search_result_box);
        mSearchResultList = findViewById(R.id.transmit_search_result_list);
        mNoMoreResult = findViewById(R.id.transmit_no_more_result);
        mInputSearchKey = findViewById(R.id.transmit_search);
        mClearInputSearchKey = findViewById(R.id.transmit_clear_search_content);
        mCancelSearch = findViewById(R.id.transmit_cancel_search);

        mClose.setOnClickListener(this);
        mSearchBox.setOnClickListener(this::onClick);
        mClearInputSearchKey.setOnClickListener(this::onClick);
        mCancelSearch.setOnClickListener(this::onClick);
        mInputSearchKey.addTextChangedListener(mInputSearchKeyListener);
        mAdapter = new SearchResultAdapter(this, new OnSelectSearchResultListener() {
            @Override
            public void onSelectSearchResultListener(TransmitBean bean) {
                closeSearchBox();
                mTransmitUsers.add(bean);
                showDialog(mTransmitUsers.size()-1);
            }
        },mTransmitRoomIdentifier);
        mSearchResultList.setAdapter(mAdapter);
    }

    private void initData() {
        mTransmitUsers = new ArrayList<>();
        //获得显示内容
        Iterator<String> iterator = IntranetChatApplication.getMessageList().iterator();
        while (iterator.hasNext()){
            LatestChatHistoryEntity next = IntranetChatApplication.sLatestChatHistoryMap.get(iterator.next());
            //不转发给自己
            if (next.getUserIdentifier().equals(mTransmitRoomIdentifier)){
                continue;
            }
            TransmitBean transmitBean = new TransmitBean(next.getUserHeadPath(), next.getUserName(), next.getUserIdentifier(), next.getGroup() == 1 ? true : false, next.getHost());
            //消息列表的host是空值
            transmitBean.setmHost(next.getHost());
            mTransmitUsers.add(transmitBean);
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        Iterator<TransmitBean> beanIterator = mTransmitUsers.iterator();
        int index = 0;
        while (beanIterator.hasNext()){
            //加载联系人布局
            View view = inflater.inflate(R.layout.listview_main_contact, null);
            view.findViewById(R.id.contact_state).setVisibility(View.GONE);
            CircleImageView avatar = view.findViewById(R.id.contact_head);
            TextView name = view.findViewById(R.id.contact_name);

            //取出加载内容
            TransmitBean next = beanIterator.next();
            if (!TextUtils.isEmpty(next.getmAvatarPath())){
                Glide.with(this).load(next.getmAvatarPath()).into(avatar);
            }else {
                avatar.setImageResource(R.drawable.default_head);
            }

            if (!TextUtils.isEmpty(next.getmUseName())){
                name.setText(next.getmUseName());
            }else {
                continue;
            }
            //设置背景为透明色
            view.setBackgroundColor(getResources().getColor(R.color.color_transparent));
            view.setOnClickListener(this::onClick);

            mRecentlyChat.addView(view,index++,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        View view = LayoutInflater.from(TransmitActivity.this).inflate(R.layout.dialog_confirm_transmit, null);

        AlertDialog alertDialog = new AlertDialog.Builder(TransmitActivity.this)
                .setView(view)
                .create();

        Window window = alertDialog.getWindow();
        window.setBackgroundDrawable(new BitmapDrawable());     //显示Dialog圆角
        buildDialogView(alertDialog,view,id);       //填充dialog的控件，添加点击事件等
        return alertDialog;
    }

    /**
     * 给Dialog绑定事件
     * @param alertDialog 弹出的alertDialog，点击取消和发送时关闭alertDialog
     * @param view 自定义的view，给view绑定事件
     * @param id alertDialog对应mTransmitUsers的位置*/
    private void buildDialogView(AlertDialog alertDialog, View view, int id) {
        CircleImageView avatar = view.findViewById(R.id.transmit_avatar);   //转发对象的头像
        TextView name = view.findViewById(R.id.transmit_name);
        TextView message = view.findViewById(R.id.transmit_message);    //显示文字消息
        ImageView image = view.findViewById(R.id.transmit_image);       //显示图片
        EditText leaveWord = view.findViewById(R.id.transmit_input_leave);      //留言
        Button cancel = view.findViewById(R.id.transmit_cancel);        //取消按钮
        Button send = view.findViewById(R.id.transmit_send);        //确认按钮

        TransmitBean transmitBean = mTransmitUsers.get(id);     //转发对象
        if (!TextUtils.isEmpty(transmitBean.getmAvatarPath())){
            Glide.with(TransmitActivity.this).load(transmitBean.getmAvatarPath()).into(avatar);
        }
        name.setText(transmitBean.getmUseName());

        if (mRecordType == ChatRoomConfig.RECORD_IMAGE){
            //显示转发的图片，不显示图片路径
            message.setVisibility(View.GONE);
            image.setVisibility(View.VISIBLE);
            Glide.with(TransmitActivity.this).load(mMessage).into(image);
        }else {
            //显示转发的文字
            message.setText(mMessage);
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.transmit_cancel){
                    alertDialog.dismiss();
                }else {
                    String leave = leaveWord.getText().toString();      //获得输入的留言
                    TLog.d(TAG, "onClick: leave = " + leave);

                    ChatRecordEntity messageRecord = null;
                    ChatRecordEntity leaveRecord = null;

                    if (mRecordType == ChatRoomConfig.RECORD_TEXT){
                        //转发文字
                        messageRecord = transmitMessage(id);
                    }else if (mRecordType == ChatRoomConfig.RECORD_IMAGE){
                        //转发图片
                        messageRecord = transmitFile(id);
                    }
                    if (!TextUtils.isEmpty(leave)){     //如果输入留言，转发留言
                        mMessage = leave;
                        leaveRecord = transmitMessage(id);
                        //同时向数据库写入转发记录和留言记录
                        ChatRecordEntity[] entities = new ChatRecordEntity[2];
                        entities[0] = messageRecord;
                        entities[1] = leaveRecord;
                        record(entities);
                    }else {
                        record(messageRecord);
                    }

                    alertDialog.dismiss();
                    TransmitActivity.this.finish();
                }
            }
        };
        cancel.setOnClickListener(onClickListener);
        send.setOnClickListener(onClickListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.close_transmit:
                finish();
                break;
            case R.id.contact_list_item:
                int i = ((ViewGroup) v.getParent()).indexOfChild(v);
                TLog.d(TAG, "onClick: i = " + i);
                showDialog(i);
                break;
            case R.id.transmit_search_box:
                openSearchBox();
                break;
            case R.id.transmit_clear_search_content:
                mInputSearchKey.setText("");
                break;
            case R.id.transmit_cancel_search:
                closeSearchBox();
                break;
        }
    }

    /**
     * 点击搜索框，弹出搜索界面*/
    private void openSearchBox() {
        mScroll.setVisibility(View.GONE);
        mTitle.setVisibility(View.GONE);

        mSearchInputBox.setVisibility(View.VISIBLE);
        mSearchResultBox.setVisibility(View.VISIBLE);
        mSearchResultBox.setBackgroundColor(getResources().getColor(R.color.color_light_black));
    }

    private void closeSearchBox() {
        mInputSearchKey.setText("");
        mSearchResultBox.setVisibility(View.GONE);
        mNoMoreResult.setVisibility(View.GONE);
        mSearchResultList.setVisibility(View.GONE);
        mSearchInputBox.setVisibility(View.GONE);

        mTitle.setVisibility(View.VISIBLE);
        mScroll.setVisibility(View.VISIBLE);
    }

    /**
     * 转发文字
     * @param i 转发对象在mTransmitUsers中的位置*/
    private ChatRecordEntity transmitMessage(int i) {
        return transmitMessage(mMessage,mTransmitUsers.get(i));
    }

    /**
     * 转发文字
     * @param message 转发的文字
     * @param transmitBean 转发对象*/
    public static ChatRecordEntity transmitMessage(String message, TransmitBean transmitBean){
        //转发消息
        ChatRecordEntity recordEntity = SendMessage.sendCommonMessage(new SendMessageBean(message,
                transmitBean.getmUserIdentifier(),
                transmitBean.getmHost(),
                transmitBean.getmAvatarPath(),
                transmitBean.getmUseName(),
                transmitBean.isGroup()));

        return recordEntity;
    }

    /**
     * 转发文件，目前仅支持图片和视频
     * @param i 转发对象在mTransmitUsers中的位置*/
    private ChatRecordEntity transmitFile(int i){
        int type = mMessage.equals(ChatRoomConfig.RECORD_VIDEO) ? 2 : 1;
        ChatRecordEntity chatRecordEntity = SendMessage.sendCommonMessage(new EventMessage(mMessage, type),
                new SendMessageBean(mMessage,
                        mTransmitUsers.get(i).getmUserIdentifier(),
                        mTransmitUsers.get(i).getmHost(),
                        mTransmitUsers.get(i).getmAvatarPath(),
                        mTransmitUsers.get(i).getmUseName(),
                        mTransmitUsers.get(i).isGroup()),
                true);
        return chatRecordEntity;
    }

    /**
     * 转发消息后增加聊天记录
     * @param recordEntity 消息记录*/
    public static void record(ChatRecordEntity... recordEntity){
        //记录消息
        new Thread(new Runnable() {
            @Override
            public void run() {
                ChatRecordDao chatRecordDao = MyDataBase.getInstance().getChatRecordDao();
                //如果上次聊天时间超过现在2分钟，插入上次聊天时间
                long latestRecordTime = chatRecordDao.getLatestRecordTime(recordEntity[0].getReceiver());
                if (latestRecordTime != 0 && recordEntity[0].getTime() - latestRecordTime > 2*60*1000){
                    ChatRecordEntity recordTime = ChatRoomMessageAdapter.generatorTimeRecord(recordEntity[0].getReceiver(),latestRecordTime);
                    chatRecordDao.insert(recordTime);
                }
                //记录转发
                chatRecordDao.insert(recordEntity);
            }
        }).start();
    }

    @Override
    public void onBackPressed() {

        if (mTitle.getVisibility() == View.GONE){
            closeSearchBox();
            return;
        }
        super.onBackPressed();
    }
}
