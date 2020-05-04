/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/30
 * Description: [Intranet Chat] [APP][UI] Chat Room
 */
package com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom;

public class ChatRoomConfig {
    public static final int RECEIVE_MESSAGE = 0;
    public static final int SEND_MESSAGE = 1;
    public static final int RECEIVE_VOICE = 2;
    public static final int SEND_VOICE = 3;
    public static final int RECEIVE_IMAGE = 4;
    public static final int SEND_IMAGE = 5;
    public static final int RECEIVE_VIDEO = 6;
    public static final int SEND_VIDEO = 7;
    public static final int RECEIVE_FILE = 8;
    public static final int SEND_FILE = 9;

    public static final int RECORD_TEXT = 10;
    public static final int RECORD_IMAGE = 11;
    public static final int RECORD_VOICE = 12;
    public static final int RECORD_VIDEO = 13;
    public static final int RECORD_FILE = 14;
    public static final int RECORD_TIME = 15;
    public static final int RECORD_CALL = 16;
    public static final int RECEIVE_VIDEO_CALL = 17;
    public static final int SEND_VIDEO_CALL = 18;
    public static final int RECEIVE_VOICE_CALL = 19;
    public static final int SEND_VOICE_CALL = 20;

    public static final int RECORD_NOTIFY_MESSAGE = 21;     //@别人
    public static final int RECORD_REPLAY_MESSAGE = 22;     //回复消息
    public static final int SEND_AT_MESSAGE = 23;           //发送@消息
    public static final int RECEIVE_AT_MESSAGE = 24;        //收到@消息
    public static final int SEND_REPLAY_MESSAGE = 25;       //发送回复
    public static final int RECEIVE_REPLAY_MESSAGE = 26;    //收到回复

    public static final String NAME = "name";
    public static final String AVATAR = "avatar";
    public static final String HOST = "host";
    public static final String IDENTIFIER = "identifier";
    public static final String UID = "uid";
    public static final String GROUP = "group";

    public static final double MAX_VOICE_LENGTH = 100;
    public static final int BASE_VOICE_LENGTH = 30;
    public static final int MAX_VOICE_TEXT_VIEW_LENGTH = 600;

    public static final String PATH_VIDEO_FIRST_FRAME = "/Android/data/com.skysoft.smart.intranetchat/files/videoFirstFrame/";

    public static final int CALL_END_ANSWER = 20;
    public static final int CALL_OUT_TIME_LAUNCH = 21;
    public static final int CALL_OUT_TIME_ANSWER = 22;
    public static final int CALL_IN_CALL = 23;
    public static final int CALL_REFUSE_LAUNCH = 24;
    public static final int CALL_REFUSE_ANSWER = 25;
    public static final int CALL_DIE_LAUNCH = 26;
    public static final int CALL_DIE_ANSWER = 27;
    public static final int CALL_END_LAUNCH = 28;
    public static final int CALL_REFUSE_ANSWER_MINE = 29;
    public static final int CALL_REFUSE_LAUNCH_MINE = 30;
}
