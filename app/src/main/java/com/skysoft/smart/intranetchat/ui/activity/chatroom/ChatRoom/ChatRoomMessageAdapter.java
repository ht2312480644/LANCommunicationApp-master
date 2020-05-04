/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/30
 * Description: [Intranet Chat] [APP][UI] Chat Room
 */
package com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;

import com.skysoft.smart.intranetchat.model.network.bean.UserInfoBean;
import com.skysoft.smart.intranetchat.tools.CreateNotifyBitmap;
import com.skysoft.smart.intranetchat.tools.GsonTools;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import android.text.style.ImageSpan;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.database.dao.ChatRecordDao;
import com.skysoft.smart.intranetchat.model.net_model.SendFile;
import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.tools.QuickClickListener;
import com.skysoft.smart.intranetchat.ui.activity.camera.PictureShowActivity;
import com.skysoft.smart.intranetchat.ui.activity.camera.VideoPlayActivity;

import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.model.camera.manager.MyMediaPlayerManager;
import com.skysoft.smart.intranetchat.database.MyDataBase;
import com.skysoft.smart.intranetchat.database.table.ChatRecordEntity;
import com.skysoft.smart.intranetchat.database.table.ContactEntity;
import com.skysoft.smart.intranetchat.database.table.FileEntity;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.PopupWindow.PopupWindowAdapter;
import com.skysoft.smart.intranetchat.ui.activity.userinfoshow.UserInfoShowActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomActivity.sIsAudioRecording;

public class ChatRoomMessageAdapter extends RecyclerView.Adapter<ChatRoomMessageViewHolder> implements View.OnTouchListener {
    private static String TAG = ChatRoomMessageAdapter.class.getSimpleName();
    private Context context;
    private List<ChatRecordEntity> messageBeanList = new ArrayList<>();
    private String mineAvatarPath;
    private OnScrollToPosition onScrollToPosition;
    private ChatRoomMessageViewHolder holder;
    private String mReceiverIdentifier;
    private int mTopPosition;
    private boolean mFirstLoadHistory = true;
    private View mInflate;
    private RecyclerView mPopupRecyclerView;
    private ArrayList<String> mHasCode = new ArrayList<>();
    private int mLongClickPosition = 0;     //当前被长按的记录的位置
    private OnClickReplayOrNotify mOnClickReplayOrNotify;
    private ChatRoomMessageViewHolder mLongClickHolder;
    private boolean isUpLongClickAvatar = true;
    private boolean isGroup;

    public boolean isGroup() {
        return isGroup;
    }

    public void setGroup(boolean group) {
        isGroup = group;
    }

    public OnClickReplayOrNotify getOnClickReplayOrNotify() {
        return mOnClickReplayOrNotify;
    }

    public void setOnClickReplayOrNotify(OnClickReplayOrNotify mOnClickReplayOrNotify) {
        this.mOnClickReplayOrNotify = mOnClickReplayOrNotify;
    }

    public int getTopPosition() {
        return mTopPosition;
    }

    public void setTopPosition(int mTopPosition) {
        this.mTopPosition = mTopPosition;
    }

    public OnScrollToPosition getOnScrollToPosition() {
        return onScrollToPosition;
    }

    public void setOnScrollToPosition(OnScrollToPosition onScrollToPosition) {
        this.onScrollToPosition = onScrollToPosition;
    }

    public String getReceiverIdentifier() {
        return mReceiverIdentifier;
    }

    public void setReceiverIdentifier(String mReceiverIdentifier) {
        this.mReceiverIdentifier = mReceiverIdentifier;
    }

    public ChatRoomMessageAdapter(Context context, String mineAvatarPath, String receiverIdentifier, Boolean isGroup) {
        this.context = context;
        this.mineAvatarPath = mineAvatarPath;
        this.mReceiverIdentifier = receiverIdentifier;
        this.isGroup = isGroup;
    }

    @NonNull
    @Override
    public ChatRoomMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.listview_chat_room, parent, false);
        holder =  new ChatRoomMessageViewHolder(inflate);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatRoomMessageViewHolder holder, int position) {
        mTopPosition = position;
        ChatRecordEntity bean = messageBeanList.get(position);
        String hexString = Integer.toHexString(holder.hashCode());
        if (mHasCode.contains(hexString)){
            holder.hiddenAll();
            onBindView(holder,bean);
        }else {
            mHasCode.add(hexString);
            onBindView(holder,bean);
        }
    }

    private void onBindView(ChatRoomMessageViewHolder holder,ChatRecordEntity bean){
        if (bean.getType() == ChatRoomConfig.RECORD_TIME){
            holder.getTime().setText(ChatRoomActivity.millToFullTime(bean.getTime()));
            holder.getTime().setVisibility(View.VISIBLE);
            return;
        }
        //显示头像
        switch (bean.getIsReceive()){
            case ChatRoomConfig.RECEIVE_IMAGE:
            case ChatRoomConfig.RECEIVE_MESSAGE:
            case ChatRoomConfig.RECEIVE_VIDEO:
            case ChatRoomConfig.RECEIVE_VOICE:
            case ChatRoomConfig.RECEIVE_FILE:
            case ChatRoomConfig.RECEIVE_VIDEO_CALL:
            case ChatRoomConfig.RECEIVE_VOICE_CALL:
            case ChatRoomConfig.RECEIVE_AT_MESSAGE:
            case ChatRoomConfig.RECEIVE_REPLAY_MESSAGE:
                bindAvatar(holder,bean,true);
                break;
            case ChatRoomConfig.SEND_IMAGE:
            case ChatRoomConfig.SEND_MESSAGE:
            case ChatRoomConfig.SEND_VIDEO:
            case ChatRoomConfig.SEND_VOICE:
            case ChatRoomConfig.SEND_FILE:
            case ChatRoomConfig.SEND_VIDEO_CALL:
            case ChatRoomConfig.SEND_VOICE_CALL:
            case ChatRoomConfig.SEND_AT_MESSAGE:
            case ChatRoomConfig.SEND_REPLAY_MESSAGE:
                bindAvatar(holder,bean,false);
                break;
        }
        //显示内容
        switch (bean.getType()){
            case ChatRoomConfig.RECORD_TEXT:
                bindText(holder, bean);
                break;
            case ChatRoomConfig.RECORD_NOTIFY_MESSAGE:
                bindAtText(holder,bean);
                break;
            case ChatRoomConfig.RECORD_VOICE:
                bindVoice(holder, bean);
                break;
            case ChatRoomConfig.RECORD_IMAGE:
                bindImage(holder,bean);
                break;
            case ChatRoomConfig.RECORD_VIDEO:
                bindVideo(holder,bean);
                break;
            case ChatRoomConfig.RECORD_FILE:
                bindFile(holder,bean);
                break;
            case ChatRoomConfig.RECORD_CALL:
                bindCall(holder,bean);
                break;
            default:
                TLog.d(TAG, "onBindViewHolder: " + bean.getType());
                break;
        }
    }

    //加载视屏的预览图
    private void bindVideo(ChatRoomMessageViewHolder holder, ChatRecordEntity bean) {
        ImageView thumbnail = null;
        switch (bean.getIsReceive()){
            case ChatRoomConfig.RECEIVE_VIDEO:
                thumbnail = holder.getSenderVideoThumbnail();
                break;
            case ChatRoomConfig.SEND_VIDEO:
                thumbnail = holder.getMineVideoThumbnail();
                break;
        }
        if (TextUtils.isEmpty(bean.getPath())){
            //视频预览图路径为空时判断正在下载或者接收已经失败
            onLoading(thumbnail,bean);
        }else {
            Glide.with(context).load(bean.getPath()).into(new ImageViewTarget<Drawable>(thumbnail) {
                @Override
                protected void setResource(Drawable resource) {
                    this.view.setBackground(resource);
                    this.view.setImageResource(R.drawable.ic_play_video);
                }
            });
        }
        thumbnail.setVisibility(View.VISIBLE);
        thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (QuickClickListener.isFastClick()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (!TextUtils.isEmpty(bean.getFileName())){
                                VideoPlayActivity.goActivity(context, bean.getFileName());
                            }
                        }
                    }).start();
                }
            }
        });

        thumbnail.setOnLongClickListener(new OnLongClickRecord(bean, holder));
    }

    //加载文件
    private void bindFile(ChatRoomMessageViewHolder holder, ChatRecordEntity bean) {
        ConstraintLayout file = null;
        String size = computingFileSize(bean.getLength());
        TLog.d(TAG, "bindFile: size = " + size);
        switch (bean.getIsReceive()){
            case ChatRoomConfig.RECEIVE_FILE:
                holder.getSenderFileName().setText(bean.getFileName());
                holder.getSenderFileSize().setText(size);
                holder.getSenderFile().setVisibility(View.VISIBLE);
                file = holder.getSenderFile();
                break;
            case ChatRoomConfig.SEND_FILE:
                holder.getMineFileName().setText(bean.getFileName());
                holder.getMineFileSize().setText(size);
                holder.getMineFile().setVisibility(View.VISIBLE);
                file = holder.getMineFile();
                break;
        }

        file.setOnLongClickListener(new OnLongClickRecord(bean, holder));
    }

    //加载图片
    private void bindImage(ChatRoomMessageViewHolder holder, ChatRecordEntity bean) {
        ImageView image = null;
        switch (bean.getIsReceive()){
            case ChatRoomConfig.RECEIVE_IMAGE:
                image = holder.getSenderImage();
                break;
            case ChatRoomConfig.SEND_IMAGE:
                image = holder.getMineImage();
                break;
        }
        if (TextUtils.isEmpty(bean.getPath())){
            TLog.d(TAG, "bindImage: path = null");
            //图片路径为空时判断正在下载或者接收已经失败
            onLoading(image,bean);
            return;
        }
        Glide.with(context).load(bean.getPath()).into(image);
        image.setVisibility(View.VISIBLE);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (QuickClickListener.isFastClick()) {
                    String path = bean.getPath();
                    PictureShowActivity.goActivity(context, path);
                }
            }
        });

        image.setOnLongClickListener(new OnLongClickRecord(bean, holder));
    }

    /**
     * 未接收完图片和视频前显示正在加载*/
    public void onLoading(ImageView image,ChatRecordEntity bean){
        if (bean.isReceiveSuccess()){
            FileEntity fileEntity = IntranetChatApplication.getMonitorReceiveFile().get(bean.getContent());
            if (fileEntity != null && fileEntity.getType() != Config.STEP_DOWN_LOAD_FAILURE && System.currentTimeMillis() - fileEntity.getTime() > 10*60*100){
                Glide.with(context).load(R.drawable.ic_file_damage_fill).into(image);
                bean.setReceiveSuccess(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MyDataBase.getInstance().getChatRecordDao().update(bean);
                    }
                }).start();
            }else {
                Glide.with(context).load(R.color.color_gray_dark).into(new ImageViewTarget<Drawable>(image) {
                    @Override
                    protected void setResource(Drawable resource) {
                        this.view.setBackground(resource);
                        this.view.setBackgroundResource(R.drawable.ic_loading);
                    }
                });
            }
        }else {
            Glide.with(context).load(R.drawable.ic_file_damage_fill).into(image);
        }
    }

    //加载语音
    private void bindVoice(ChatRoomMessageViewHolder holder, ChatRecordEntity bean) {
        LinearLayout voice = null;
        TextView voiceLength = null;
        switch (bean.getIsReceive()) {
            case ChatRoomConfig.RECEIVE_VOICE:
                holder.getSenderVoice().setVisibility(View.VISIBLE);
                voiceLength = holder.getSenderVoiceLength();
                voice = holder.getSenderVoice();
                break;
            case ChatRoomConfig.SEND_VOICE:
                voiceLength = holder.getMineVoiceLength();
                voice = holder.getMineVoice();
                break;
            default:
                bean.getIsReceive();
                break;
        }

        voiceLength.setText(String.valueOf(bean.getLength()));
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) voiceLength.getLayoutParams();
        double percent = (double) bean.getLength() / ChatRoomConfig.MAX_VOICE_LENGTH;
        int width = (int) (percent * ChatRoomConfig.MAX_VOICE_TEXT_VIEW_LENGTH) + ChatRoomConfig.BASE_VOICE_LENGTH;
        layoutParams.width = width;
        voiceLength.setLayoutParams(layoutParams);
        voiceLength.setText(String.valueOf(bean.getLength()));
        voice.setVisibility(View.VISIBLE);
        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (QuickClickListener.isFastClick()) {
                    if (bean != null && !TextUtils.isEmpty(bean.getPath())){
                        onClickVoice(bean.getPath());
                    }
                }
            }
        });

        voice.setOnLongClickListener(new OnLongClickRecord(bean, holder));
    }

    //加载文本内容
    private void bindText(ChatRoomMessageViewHolder holder, ChatRecordEntity bean) {
        TextView message = null;
        if (bean.getIsReceive() == ChatRoomConfig.RECEIVE_MESSAGE){
            holder.getSenderMessage().setText(bean.getContent());
            holder.getSenderMessage().setVisibility(View.VISIBLE);
            message = holder.getSenderMessage();
        }else {
            holder.getMineMessage().setText(bean.getContent());
            holder.getMineMessage().setVisibility(View.VISIBLE);
            message = holder.getMineMessage();
        }

        message.setOnLongClickListener(new OnLongClickRecord(bean, holder));
    }

    private void bindAtText(ChatRoomMessageViewHolder holder, ChatRecordEntity bean){
        TextView message = null;
        if (bean.getIsReceive() == ChatRoomConfig.RECEIVE_AT_MESSAGE){
            holder.getSenderMessage().setText(bean.getContent());
            holder.getSenderMessage().setVisibility(View.VISIBLE);
            message = holder.getSenderMessage();

            String at = bean.getFileName();
            buildNotify(at,holder.getSenderMessage());
        }else {
            holder.getMineMessage().setText(bean.getContent());
            holder.getMineMessage().setVisibility(View.VISIBLE);
            message = holder.getMineMessage();
        }

        message.setOnLongClickListener(new OnLongClickRecord(bean, holder));
    }

    /**
     * 根据唯一标识符寻找本地名字替代@后的名字
     * @param at @队列
     * @param message */
    private void buildNotify(String at,TextView message){
        if (!TextUtils.isEmpty(at)){
            Editable editableText = message.getEditableText();

            String[] array = (String[]) GsonTools.formJson(at, String[].class);
            for (int i = array.length-1; i >= 0; i--){
                String[] split = array[i].split("\\|");

                int st = Integer.parseInt(split[1]);        //at开始位置
                int length = Integer.parseInt(split[2]);    //用户名长度

                ContactEntity entity = IntranetChatApplication.sContactMap.get(split[0]);
                if (null != entity){
                    editableText.replace(st+1,st+length+1,entity.getName());
                }else if (split[0].equals(IntranetChatApplication.getsMineUserInfo().getIdentifier())){
                    String name = IntranetChatApplication.getsMineUserInfo().getName();
                    SpannableStringBuilder spannableString = new SpannableStringBuilder("@"+name);    //构建SpannableStringBuilder
                    Bitmap bitmap = CreateNotifyBitmap.notifyBitmap(context,"@"+name);      //构建内容为notify的bitmap
                    ImageSpan imageSpan = new ImageSpan(context,bitmap);      //构建内容为bitmap的ImageSpan
                    spannableString.setSpan(imageSpan,
                            0,name.length()+1,
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);        //SpannableStringBuilder添加ImageSpan

                    editableText.replace(st,st+length+1,spannableString);
                }
            }

            message.setText(editableText);
        }
    }

    private void bindAvatar(ChatRoomMessageViewHolder holder, ChatRecordEntity bean,  boolean sender){
        if (sender){
            ContactEntity next = IntranetChatApplication.sContactMap.get(bean.getSender());
            if (null == next){
                throw new NullPointerException("can not found this contact");
            }
            //填充头像
            if (!TextUtils.isEmpty(next.getAvatarPath())){
                Glide.with(context).load(next.getAvatarPath()).into(holder.getSenderAvatar());
            }else {
                Glide.with(context).load(R.drawable.default_head).into(holder.getSenderAvatar());
            }
            //填充用户名
            if (!TextUtils.isEmpty(next.getName())){
                holder.getSenderName().setText(next.getName());
            }
            holder.getSenderName().setVisibility(View.VISIBLE);
            holder.getSenderAvatar().setVisibility(View.VISIBLE);
            //单击事件
            holder.getSenderAvatar().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isUpLongClickAvatar && QuickClickListener.isFastClick()){       //防止长按后立即触发单击事件
                        UserInfoShowActivity.go(context,next.getName(),next.getAvatarPath(),bean.getSender());
                    }
                }
            });

            //长按头像显示@*****
            holder.getSenderAvatar().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (null != mOnClickReplayOrNotify){
                        //在输入框中显示@***
                        mOnClickReplayOrNotify.onClickNotify(bean,holder.getSenderName().getText().toString());
                    }
                    isUpLongClickAvatar = false;        //正在长按avatar，avatar的单击事件不能触发
                    v.setOnTouchListener(ChatRoomMessageAdapter.this::onTouch);     //监听松开avatar
                    return false;
                }
            });
        }else {
            if (!TextUtils.isEmpty(mineAvatarPath)){
                Glide.with(context).load(mineAvatarPath).into(holder.getMineAvatar());
            }else {
                Glide.with(context).load(R.drawable.default_head).into(holder.getMineAvatar());
            }
            holder.getMineAvatar().setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.list_view_chat_room_sender_avatar){
            if (event.getAction() == MotionEvent.ACTION_UP){
                TLog.d(TAG, "onTouch: up");
                isUpLongClickAvatar = true;     //松开长按
                v.setOnTouchListener(null);     //取消触摸监听
                QuickClickListener.isFastClick();       //避免松开手指立即触发单击事件
            }
        }
        return false;
    }

    private void bindCall(ChatRoomMessageViewHolder holder, ChatRecordEntity bean){
        LinearLayout callBox = null;
        switch (bean.getIsReceive()){
            case ChatRoomConfig.RECEIVE_VOICE_CALL:
                holder.getSenderCallBox().setVisibility(View.VISIBLE);
                Glide.with(context).load(R.drawable.ic_voice_call).into(holder.getSenderCallImage());
                holder.getSenderCallText().setText(bean.getContent());
                break;
            case ChatRoomConfig.RECEIVE_VIDEO_CALL:
                holder.getSenderCallBox().setVisibility(View.VISIBLE);
                Glide.with(context).load(R.drawable.ic_video_call).into(holder.getSenderCallImage());
                holder.getSenderCallText().setText(bean.getContent());
                break;
            case ChatRoomConfig.SEND_VOICE_CALL:
                holder.getMineCallBox().setVisibility(View.VISIBLE);
                Glide.with(context).load(R.drawable.ic_voice_call).into(holder.getMineCallImage());
                holder.getMineCallText().setText(bean.getContent());
                break;
            case ChatRoomConfig.SEND_VIDEO_CALL:
                holder.getMineCallBox().setVisibility(View.VISIBLE);
                Glide.with(context).load(R.drawable.ic_video_call).into(holder.getMineCallImage());
                holder.getMineCallText().setText(bean.getContent());
                break;
        }
        if (bean.getLength() == -1){
            holder.getSenderCallImage().setColorFilter(R.color.color_gray_dark);
            holder.getSenderCallText().setTextColor(context.getResources().getColor(R.color.color_gray_dark));
        }
        if (bean.getIsReceive() == ChatRoomConfig.RECEIVE_VOICE_CALL || bean.getIsReceive() == ChatRoomConfig.RECEIVE_VIDEO_CALL){
            callBox = holder.getSenderCallBox();
        }else {
            callBox = holder.getMineCallBox();
        }

        callBox.setOnLongClickListener(new OnLongClickRecord(bean, holder));
    }

    @Override
    public int getItemCount() {
        return messageBeanList.size();
    }

    public void add(ChatRecordEntity chatRecordEntity,boolean receive){
        if (chatRecordEntity == null) {
            return;
        }
        if (messageBeanList.size() > 0 && System.currentTimeMillis() - messageBeanList.get(messageBeanList.size() - 1).getTime() > 2*60*1000 && messageBeanList.get(messageBeanList.size() - 1).getType() != ChatRoomConfig.RECORD_TIME){
            ChatRecordEntity temp = messageBeanList.get(messageBeanList.size() - 1);
            ChatRecordEntity recordTime = generatorTimeRecord(mReceiverIdentifier,temp.getTime());
            messageBeanList.add(recordTime);
            TLog.d(TAG, "add: recordTime 4 timeString = " + ChatRoomActivity.millToFullTime(recordTime.getTime()) + ", entity = " + chatRecordEntity.toString());
            addRecordToList(chatRecordEntity,recordTime,receive);
            return;
        }

        //填充之前没有加载的图片/视频路径
        if (receive){
            for (int i  = messageBeanList.size() - 1; i > messageBeanList.size() - 11 && i >= 0; i--){
                ChatRecordEntity recordEntity = messageBeanList.get(i);
                if ((recordEntity.getType() == ChatRoomConfig.RECORD_VIDEO || recordEntity.getType() == ChatRoomConfig.RECORD_IMAGE || recordEntity.getType() == ChatRoomConfig.RECORD_VOICE) && recordEntity.getContent().equals(chatRecordEntity.getContent())){
                    if (TextUtils.isEmpty(recordEntity.getPath())){
                        //收到文件接收失败的通知
                        if (!chatRecordEntity.isReceiveSuccess()){
                            recordEntity.setReceiveSuccess(false);
                        }else {
                            //收到文件接收成功的通知
                            recordEntity.setPath(chatRecordEntity.getPath());
                            recordEntity.setFileName(chatRecordEntity.getFileName());
                        }
                        notifyDataSetChanged();
                        return;
                    }
                }
            }
        }

        addRecordToList(chatRecordEntity,null,receive);
    }

    public void add(ChatRecordEntity chatRecordEntity) {
        add(chatRecordEntity,false);
    }

    public static ChatRecordEntity generatorTimeRecord(String identifier,long time){
        ChatRecordEntity recordTime = new ChatRecordEntity();
        recordTime.setTime(time);
        recordTime.setType(ChatRoomConfig.RECORD_TIME);
        recordTime.setReceiver(identifier);
        return recordTime;
    }

    private void addRecordToList(ChatRecordEntity chatRecordEntity,ChatRecordEntity recordTime,boolean receive){
        messageBeanList.add(chatRecordEntity);
        this.notifyItemInserted(messageBeanList.size());
        onScrollToPosition.onLoadViewOver(messageBeanList.size());
        if (!receive){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (recordTime != null){
                        MyDataBase.getInstance().getChatRecordDao().insert(recordTime);
                    }
                    MyDataBase.getInstance().getChatRecordDao().insert(chatRecordEntity);
                    TLog.d(TAG,"chatRecordEntity " + chatRecordEntity);
                }
            }).start();
        }
    }

    public void addAll(List<ChatRecordEntity> chatHistory) {
        int startPosition = 0;
        messageBeanList.addAll(0,chatHistory);
        this.notifyItemRangeInserted(startPosition,chatHistory.size());

        if (mFirstLoadHistory){
            onScrollToPosition.onLoadViewOver(messageBeanList.size());
            mFirstLoadHistory = false;
            return;
        }
    }

    //当点击语音框时，播放语音
    public void onClickVoice(String path) {
        if (sIsAudioRecording){
            return;
        }
        MyMediaPlayerManager.getsInstance().play(path);
//        1572865304716
    }

    public static String createVideoThumbnailFile(String path) {
        TLog.d(TAG, "createVideoThumbnailFile: onReceiveAndSaveFile 1 path = " + path);
        Bitmap bitmap = createVideoThumbnail(path);
        File parentFile = SendFile.getFile(ChatRoomConfig.PATH_VIDEO_FIRST_FRAME);
        if (!parentFile.exists()){
            parentFile.mkdirs();
        }
        TLog.d(TAG, "createVideoThumbnailFile: onReceiveAndSaveFile 2 path = " + path);
        String name = new File(path).getName();
        name = name.substring(0,name.lastIndexOf("."));
        File firstFrameFile = new File(parentFile.getPath(),"first_frame" + name + ".jpg");
        try {
            firstFrameFile.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(firstFrameFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return firstFrameFile.getPath();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private String computingFileSize(long length){
        StringBuilder sb = new StringBuilder();
        String[] unit = {"B","K","M","G","P"};
        int level = 0;
        int value = 1024;
        while(length > value){
            value *= 1024;
            level++;
        }
        int m = (int) (length / (value / 1024));
        if (m > 700){
            sb.append(0);
            sb.append('.');
            sb.append(m/10);
            sb.append(unit[level + 1]);
        }else {
            sb.append(m);
            sb.append(unit[level]);
        }
        return sb.toString();
    }

    //获得视屏文件的第一帧图
    private static Bitmap createVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException e) {
            // Assume this is a corrupt video file
            e.printStackTrace();
        } catch (RuntimeException e) {
            // Assume this is a corrupt video file.
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                // Ignore failures while cleaning up.
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public static int makeDropDownMeasureSpec(int measureSpec){
        int mode = 0;
        if (measureSpec == ViewGroup.LayoutParams.WRAP_CONTENT){
            mode = View.MeasureSpec.UNSPECIFIED;
        }else {
            mode = View.MeasureSpec.EXACTLY;
        }
        return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(measureSpec),mode);
    }

    /**
     * 弹出popupWindow
     * @param view 被长按的控件，popupWindow围绕view显示
     * @param chatRecordEntity 被长按的控件对应的聊天记录
     * @param relativeY 控件相对于聊天室顶部的相对距离
     * @param nameHeight 对方名字的高度*/
    public void showPopupMenu(View view, ChatRecordEntity chatRecordEntity, int relativeY, int nameHeight){
        if (null == mInflate){
            mInflate = LayoutInflater.from(context).inflate(R.layout.chat_message_popup_window, null);
        }

        PopupWindow popupWindow = new PopupWindow(mInflate,ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        if (null == mPopupRecyclerView){
            mPopupRecyclerView = mInflate.findViewById(R.id.chat_message_popup_window);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false);
            mPopupRecyclerView.setLayoutManager(linearLayoutManager);
            mPopupRecyclerView.addItemDecoration(new DividerItemDecoration(context,DividerItemDecoration.HORIZONTAL));
        }

        //popupWindow弹出的内容
        PopupWindowAdapter adapter = new PopupWindowAdapter(context,chatRecordEntity,popupWindow,ChatRoomMessageAdapter.this);
        mPopupRecyclerView.setAdapter(adapter);

        //设置popupWindow点击外部消失
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);

        //测量popupWindow的长宽
        View contentView = popupWindow.getContentView();
        contentView.measure(makeDropDownMeasureSpec(popupWindow.getWidth()),
                makeDropDownMeasureSpec(popupWindow.getHeight()));

        int offsetX = 0;
        int offsetY = -(contentView.getMeasuredHeight() + view.getHeight());

        //如果是接收的消息，popupWindow相对于view靠左，反之靠右
        if (chatRecordEntity.getIsReceive() == ChatRoomConfig.SEND_FILE
                || chatRecordEntity.getIsReceive() == ChatRoomConfig.SEND_IMAGE
                || chatRecordEntity.getIsReceive() == ChatRoomConfig.SEND_MESSAGE
                || chatRecordEntity.getIsReceive() == ChatRoomConfig.SEND_VIDEO
                || chatRecordEntity.getIsReceive() == ChatRoomConfig.SEND_VIDEO_CALL
                || chatRecordEntity.getIsReceive() == ChatRoomConfig.SEND_VOICE
                || chatRecordEntity.getIsReceive() == ChatRoomConfig.SEND_VOICE_CALL
                || chatRecordEntity.getIsReceive() == ChatRoomConfig.SEND_AT_MESSAGE
                || chatRecordEntity.getIsReceive() == ChatRoomConfig.SEND_REPLAY_MESSAGE){
            TLog.d(TAG, "showPopupMenu: right");
            offsetX = -(contentView.getMeasuredWidth() - view.getWidth());
        }

        //判断popupWindow在view上方或者下方
        if (relativeY < contentView.getMeasuredHeight()){
            offsetY = 0;
        }else {
            offsetY -= nameHeight;
        }

        //显示popupWindow
        popupWindow.showAsDropDown(view,offsetX,offsetY,Gravity.LEFT);
    }

    private class OnLongClickRecord implements View.OnLongClickListener{
        private ChatRecordEntity mRecordEntity;     //被长按的记录
        private ChatRoomMessageViewHolder holder;   //被长按的holder

        public OnLongClickRecord(ChatRecordEntity mRecordEntity, ChatRoomMessageViewHolder holder) {
            this.mRecordEntity = mRecordEntity;
            this.holder = holder;
        }

        @Override
        public boolean onLongClick(View v) {
            mLongClickPosition = holder.getAdapterPosition();       //记录长按的位置
            mLongClickHolder = holder;
            showPopupMenu(v,mRecordEntity,holder.getBox().getTop() + holder.getBox().getScrollX(),  //计算view到聊天室顶部的距离
                    holder.getSenderName().getVisibility() == View.VISIBLE ? holder.getSenderName().getHeight() : 0);
            return false;
        }
    }

    /**
     * 删除聊天记录*/
    public void deleteChatRecord(){
        int position = mLongClickPosition;      //记录长按位置

        ChatRecordEntity[] deleteEntities = new ChatRecordEntity[2];    //被删除的两条记录
        boolean common = true;      //普通情况下只删除选中的那条记录
        if (mLongClickPosition != 0 &&      //不是第一条记录
                messageBeanList.get(mLongClickPosition-1).getType() == ChatRoomConfig.RECORD_TIME &&    //上一条记录是时间
                (messageBeanList.size()-1 == mLongClickPosition ||      //最后一条
                        messageBeanList.get(mLongClickPosition+1).getType() == ChatRoomConfig.RECORD_TIME)){        //下一条记录是时间
            common = false;
            mLongClickPosition --;
        }else if (mLongClickPosition == 0 &&       //第一条记录
                messageBeanList.get(1).getType() == ChatRoomConfig.RECORD_TIME){    //第二条记录是时间
            common = false;
        }
        //如果满足上诉条件任意一条，deleteEntities[0]为时间记录，deleteEntities[1]为聊天记录
        //反之，deleteEntities[0]为当前选中的记录
        deleteEntities[0] = messageBeanList.get(mLongClickPosition);

        if (!common){
            //deleteEntities[1]为聊天记录，deleteEntities[0]为时间记录
            deleteEntities[1] = messageBeanList.get(mLongClickPosition+1);
            messageBeanList.remove(mLongClickPosition+1);   //从列表中移除mLongClickPosition+1指向的记录
        }

        messageBeanList.remove(mLongClickPosition);     //从列表中移除mLongClickPosition指向的记录
        notifyDataSetChanged();

        new Thread(new Runnable() {
            @Override
            public void run() {
                ChatRecordDao chatRecordDao = MyDataBase.getInstance().getChatRecordDao();

                if (position == 0){     //长按第一条记录，判断显示的第一条记录在数据库的上一条记录是否是时间记录
                    ChatRecordEntity recordBeforeTime = chatRecordDao.getLatestRecordBeforeTime(deleteEntities[0].getReceiver(), deleteEntities[0].getTime(), deleteEntities[0].getId());
                    if (null != recordBeforeTime && recordBeforeTime.getType() == ChatRoomConfig.RECORD_TIME){
                        chatRecordDao.delete(recordBeforeTime);
                    }
                }

                if (null != deleteEntities[1]){     //删除两条记录
                    chatRecordDao.delete(deleteEntities);
                }else {
                    //只删除选中的长按记录
                    chatRecordDao.delete(deleteEntities[0]);
                }
            }
        }).start();
    }

    /**
     * 回复消息*/
    public void replayChatRecord(){
        if (null != mLongClickHolder && null != mOnClickReplayOrNotify){
            mOnClickReplayOrNotify.onClickReplay(messageBeanList.get(mLongClickPosition),mLongClickHolder.getSenderName().getText().toString());
        }
    }
}
