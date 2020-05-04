package com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom;

import com.skysoft.smart.intranetchat.database.table.ChatRecordEntity;

public interface OnClickReplayOrNotify {
    /**
     * 在PopupWindow选中'回复'后，ChatRoom弹出回复框，输入框中添加@***
     * @param recordEntity  被选中的记录
     * @param name 记录对应用户的用户名*/
    void onClickReplay(ChatRecordEntity recordEntity, String name);

    /**
     * 长按用户头像，输入框中添加@***
     * @param recordEntity  被选中的记录
     * @param name 记录对应用户的用户名*/
    void onClickNotify(ChatRecordEntity recordEntity, String name);
}
