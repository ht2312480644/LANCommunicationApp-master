/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication] Create a new process service with message sending and receiving.
 */
package com.skysoft.smart.intranetchat.model.network;

public class Config {
    //notification
    public static final String Notification_Channel_Id = "SkySoftChat_Channel_id";
    public static final String Notification_Channel_Name = "联系人消息通知";

    //port
    public static final int PORT_UDP_RECEIVE = 12302;
    public static final int PORT_TCP_FILE = 12303;
    public static final int PORT_TCP_CALL = 12304;

    //length
    public static final int UDP_RCV_DATA_PACKET_LENGTH = 1024*32;

    //status
    public static final int STATUS_LOGIN = 0;
    public static final int STATUS_ONLINE = 1;
    public static final int STATUS_BUSY = 2;
    public static final int STATUS_OUT_LINE = 3;
    public static final int STATUS_GROUP = 4;
    public static final int TOP = 1;
    public static final int NO_TOP = 0;

    //code
    public static final int CODE_MESSAGE = 10;
    public static final int CODE_MESSAGE_NOTIFICATION = 101;
    public static final int CODE_MESSAGE_REPLAY = 102;
    public static final int CODE_USERINFO = 11;
    public static final int CODE_REQUEST = 12;
    public static final int CODE_FILE = 13;
    public static final int CODE_ASK_RESOURCE = 14;
    public static final int CODE_RESPONSE = 15;
    public static final int CODE_VOICE_CALL = 16;
    public static final int CODE_VIDEO_CALL = 17;
    public static final int CODE_ESTABLISH_GROUP = 18;
    public static final int CODE_USER_OUT_LINE = 19;

    //request
    public static final int REQUEST_USERINFO = 20;
    public static final int REQUEST_CONSENT_CALL = 21;
    public static final int REQUEST_MONITOR = 22;
    public static final int REQUEST_BE_MONITOR = 23;
    public static final int REQUEST_HEARTBEAT = 24;
    public static final int HEARTBEAT = 25;

    //resource type
    public static final int RESOURCE_FILE = 30;
    public static final int RESOURCE_AVATAR = 31;
    public static final int RESOURCE_GROUP = 32;

    //radix
    public static final int RADIX_16 = 16;
    public static final int RADIX_32 = 32;
    public static final int RADIX_64 = 64;

    //file
    public static final int FILE_VIDEO = 40;
    public static final int FILE_VOICE = 41;
    public static final int FILE_PICTURE = 42;
    public static final int FILE_COMMON = 43;
    public static final int FILE_AVATAR = 44;

    //response
    public static final int RESPONSE_NOT_REQUEST_THIS_RESOURCE = 50;
    public static final int RESPONSE_RECEIVE_FILE_SUCCESS = 51;
    public static final int RESPONSE_RECEIVE_FILE_FAILURE = 52;
    public static final int RESPONSE_RECEIVE_FILE_FAILURE_AND_RESEND = 53;
    public static final int RESPONSE_BUSY = 54;
    public static final int RESPONSE_NOT_FOUND_FILE = 55;
    public static final int RESPONSE_RESOURCE_OK = 56;
    public static final int RESPONSE_NOTHING_ASK = 57;
    public static final int RESPONSE_BUSY_TRANSPOND = 58;
    public static final int RESPONSE_REFUSE_VOICE_CALL = 59;
    public static final int RESPONSE_CONSENT_VOICE_CALL = 60;
    public static final int RESPONSE_REFUSE_VIDEO_CALL = 61;
    public static final int RESPONSE_CONSENT_VIDEO_CALL = 62;
    public static final int RESPONSE_HUNG_UP_CALL = 63;
    public static final int RESPONSE_WAITING_CONSENT = 64;
    public static final int RESPONSE_CONSENT_OUT_TIME = 65;
    public static final int RESPONSE_IN_CALL = 66;
    public static final int RESPONSE_REFUSE_MONITOR = 67;
    public static final int RESPONSE_REFUSE_BE_MONITOR = 68;

    public static final int ON_ESTABLISH_CALL_CONNECT = 70;
    public static final int NOT_FOUND_RESOURCE = 71;
    public static final int SEND_FILE_BIG = 72;
    public static final int SEND_FILE_NOT_FOUND = 73;
    public static final int SEND_SUCCESS = 74;
    public static final int SEND_FILE_ZEOR = 75;
    public static final int NOTIFY_CLEAR_QUEUE = 76;

    //down file step
    public static final int STEP_RECEIVE_NOTIFY = 80;
    public static final int STEP_ASK_FILE = 83;
    public static final int STEP_DOWN_LOAD_FAILURE = 84;
    public static final int STEP_SUCCESS = 85;

    //path
    public static final String PATH_IMAGE = "/Android/data/com.skysoft.smart.intranetchat/files/images/";
    public static final String PATH_AVATAR = "/Android/data/com.skysoft.smart.intranetchat/files/avatar/";
    public static final String PATH_VOICE = "/Android/data/com.skysoft.smart.intranetchat/files/voice/";
    public static final String PATH_VIDEO = "/Android/data/com.skysoft.smart.intranetchat/files/video/";
    public static final String PATH_RECEIVE_FILE = "/Android/data/com.skysoft.smart.intranetchat/files/receive";
}
