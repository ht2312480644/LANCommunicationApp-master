/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/30
 * Description: [Intranet Chat] [APP][UI] Chat Room
 */
package com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.skysoft.smart.intranetchat.R;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomMessageViewHolder extends RecyclerView.ViewHolder {
    private TextView time ;
    private CircleImageView senderAvatar;
    private TextView senderMessage;
    private CircleImageView mineAvatar;
    private TextView mineMessage;
    private TextView senderVoiceLength;
    private TextView mineVoiceLength;
    private LinearLayout senderVoice;
    private LinearLayout mineVoice;
    private ImageView senderImage;
    private ImageView mineImage;
    private ConstraintLayout senderFile;
    private ConstraintLayout mineFile;
    private TextView senderFileName;
    private TextView mineFileName;
    private TextView senderFileSize;
    private TextView mineFileSize;
    private ImageView senderVideoThumbnail;
    private ImageView mineVideoThumbnail;
    private TextView senderName;
    private LinearLayout senderCallBox;
    private LinearLayout mineCallBox;
    private ImageView senderCallImage;
    private ImageView mineCallImage;
    private TextView senderCallText;
    private TextView mineCallText;
    private View box;

    public ChatRoomMessageViewHolder(@NonNull View itemView) {
        super(itemView);
        time = itemView.findViewById(R.id.list_view_chat_room_time);
        senderAvatar = itemView.findViewById(R.id.list_view_chat_room_sender_avatar);
        senderMessage = itemView.findViewById(R.id.list_view_chat_room_sender_message);
        mineAvatar = itemView.findViewById(R.id.list_view_chat_room_mine_avatar);
        mineMessage = itemView.findViewById(R.id.list_view_chat_room_mine_message);
        senderVoice = itemView.findViewById(R.id.list_view_chat_room_sender_voice);
        mineVoice = itemView.findViewById(R.id.list_view_chat_room_mine_voice);
        mineVoiceLength = itemView.findViewById(R.id.list_view_chat_room_mine_voice_length);
        senderVoiceLength = itemView.findViewById(R.id.list_view_chat_room_sender_voice_length);
        senderImage = itemView.findViewById(R.id.list_view_chat_room_sender_image);
        mineImage = itemView.findViewById(R.id.list_view_chat_room_mine_image);
        senderFile = itemView.findViewById(R.id.list_view_chat_room_sender_file);
        mineFile = itemView.findViewById(R.id.list_view_chat_room_mine_file);
        senderFileName = itemView.findViewById(R.id.list_view_chat_room_sender_file_name);
        mineFileName = itemView.findViewById(R.id.list_view_chat_room_mine_file_name);
        senderFileSize = itemView.findViewById(R.id.list_view_chat_room_sender_file_size);
        mineFileSize = itemView.findViewById(R.id.list_view_chat_room_mine_file_size);
        senderVideoThumbnail = itemView.findViewById(R.id.list_view_chat_room_sender_video_thumbnail);
        mineVideoThumbnail = itemView.findViewById(R.id.list_view_chat_room_mine_video_thumbnail);
        senderName = itemView.findViewById(R.id.list_view_chat_room_sender_name);
        senderCallBox = itemView.findViewById(R.id.list_view_chat_room_sender_call_box);
        mineCallBox = itemView.findViewById(R.id.list_view_chat_room_mine_call_box);
        senderCallImage = itemView.findViewById(R.id.list_view_chat_room_sender_call_image);
        mineCallImage = itemView.findViewById(R.id.list_view_chat_room_mine_call_image);
        senderCallText = itemView.findViewById(R.id.list_view_chat_room_sender_call_text);
        mineCallText = itemView.findViewById(R.id.list_view_chat_room_mine_call_text);
        box = itemView;
    }

    public void hiddenAll(){
        time.setVisibility(View.GONE);
        senderAvatar.setVisibility(View.GONE);
        senderMessage.setVisibility(View.GONE);
        mineAvatar.setVisibility(View.GONE);
        mineMessage.setVisibility(View.GONE);
        senderVoice.setVisibility(View.GONE);
        mineVoice.setVisibility(View.GONE);
        senderImage.setVisibility(View.GONE);
        mineImage.setVisibility(View.GONE);
        senderFile.setVisibility(View.GONE);
        mineFile.setVisibility(View.GONE);
        senderVideoThumbnail.setVisibility(View.GONE);
        mineVideoThumbnail.setVisibility(View.GONE);
        senderName.setVisibility(View.GONE);
        senderCallBox.setVisibility(View.GONE);
        mineCallBox.setVisibility(View.GONE);
    }

    public View getBox() {
        return box;
    }

    public TextView getTime() {
        return time;
    }

    public ImageView getSenderAvatar() {
        return senderAvatar;
    }

    public TextView getSenderMessage() {
        return senderMessage;
    }

    public ImageView getMineAvatar() {
        return mineAvatar;
    }

    public TextView getMineMessage() {
        return mineMessage;
    }

    public TextView getSenderVoiceLength() {
        return senderVoiceLength;
    }

    public TextView getMineVoiceLength() {
        return mineVoiceLength;
    }

    public LinearLayout getSenderVoice() {
        return senderVoice;
    }

    public LinearLayout getMineVoice() {
        return mineVoice;
    }

    public ImageView getSenderImage() {
        return senderImage;
    }

    public ImageView getMineImage() {
        return mineImage;
    }

    public ConstraintLayout getSenderFile() {
        return senderFile;
    }

    public ConstraintLayout getMineFile() {
        return mineFile;
    }

    public TextView getSenderFileName() {
        return senderFileName;
    }

    public TextView getMineFileName() {
        return mineFileName;
    }

    public TextView getSenderFileSize() {
        return senderFileSize;
    }

    public TextView getMineFileSize() {
        return mineFileSize;
    }

    public ImageView getSenderVideoThumbnail() {
        return senderVideoThumbnail;
    }

    public ImageView getMineVideoThumbnail() {
        return mineVideoThumbnail;
    }

    public TextView getSenderName() {
        return senderName;
    }

    public LinearLayout getSenderCallBox() {
        return senderCallBox;
    }

    public LinearLayout getMineCallBox() {
        return mineCallBox;
    }

    public ImageView getSenderCallImage() {
        return senderCallImage;
    }

    public ImageView getMineCallImage() {
        return mineCallImage;
    }

    public TextView getSenderCallText() {
        return senderCallText;
    }

    public TextView getMineCallText() {
        return mineCallText;
    }
}
