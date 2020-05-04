/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Voice or video communication function
 */
package com.skysoft.smart.intranetchat.ui.activity.voicecall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.model.net_model.VoiceCall;
import com.skysoft.smart.intranetchat.app.impl.OnEstablishCallConnect;
import com.skysoft.smart.intranetchat.app.impl.OnReceiveCallHungUp;
import com.skysoft.smart.intranetchat.model.camera.audioTrack.VioceCallPlay;
import com.skysoft.smart.intranetchat.model.camera.manager.MyAudioManager;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;
import com.skysoft.smart.intranetchat.ui.activity.videocall.VideoCallActivity;

public class VoiceCallActivity extends AppCompatActivity {

    private String TAG = VoiceCallActivity.class.getSimpleName();
    private ImageView hungUpCall;
    private ImageView headImg;
    private TextView mName;
    private TextView mTime;
    private String host;
    private String name;
    private String imgPath;
    private MyAudioManager myAudioManager;
    private VioceCallPlay mVoicePlay;
    private String mIdentifier;
    private boolean isAnswer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_voice_call);
        CustomStatusBarBackground.customStatusBarTransparent(this);
        CustomStatusBarBackground.colorViewStatusBar(this,R.color.color_black,findViewById(R.id.custom_status_bar_background));
        IntranetChatApplication.setStartCallTime(System.currentTimeMillis());
        IntranetChatApplication.getsCallback().setHungUpInAnswer(hungUp);
        IntranetChatApplication.getsCallback().setOnEstablishCallConnect(onEstablishCallConnect);
        hungUpCall = findViewById(R.id.activity_on_voice_call_cancel);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        host = bundle.getString("host");
        name = bundle.getString("name");
        imgPath = bundle.getString("imgPath");
        mIdentifier = bundle.getString("identifier");
        isAnswer = bundle.getBoolean("answer");
        mTime = findViewById(R.id.activity_on_voice_call_time);
        mName = findViewById(R.id.activity_on_voice_call_name);
        headImg = findViewById(R.id.activity_on_voice_call_img);
        mName.setText(name);
        if (!TextUtils.isEmpty(imgPath)){
            Glide.with(this).load(imgPath).into(headImg);
        }else {
            Glide.with(this).load(R.drawable.default_head).into(headImg);
        }
        mVoicePlay = new VioceCallPlay();
        myAudioManager = new MyAudioManager();
        myAudioManager.vioceCall();
        mVoicePlay.start();
        hungUpCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VoiceCall.hungUpVoiceCall(host);
                IntranetChatApplication.setInCall(false);
                IntranetChatApplication.setEndCallTime(System.currentTimeMillis());
                VideoCallActivity.endCall(mIdentifier,host,isAnswer,true);
                finish();
            }
        });

    }

    private OnReceiveCallHungUp hungUp = new OnReceiveCallHungUp() {
        @Override
        public void onReceiveHungUpVoiceCall(String host) {
            if (!VoiceCallActivity.this.host.equals(host)){
                return;
            }
            IntranetChatApplication.setInCall(false);
            IntranetChatApplication.setEndCallTime(System.currentTimeMillis());
            VideoCallActivity.endCall(mIdentifier,host,isAnswer,true);
            finish();
        }
    };


    public static void go(Activity activity, String host) {
        Intent intent = new Intent(activity, VoiceCallActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("host", host);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    public static void go(Activity activity, String host, String name, String imgPath, String identifier,boolean answer) {
        Intent intent = new Intent(activity, VoiceCallActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("imgPath", imgPath);
        bundle.putString("host", host);
        bundle.putString("identifier",identifier);
        bundle.putBoolean("answer",answer);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myAudioManager.stop();
        mVoicePlay.stopPlay();

    }

    public OnEstablishCallConnect onEstablishCallConnect = new OnEstablishCallConnect() {
        @Override
        public void onEstablishCallConnect() {
            TLog.d(TAG, "onEstablishCallConnect: ");
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        VoiceCall.hungUpVoiceCall(host);
        IntranetChatApplication.setInCall(false);
        IntranetChatApplication.setEndCallTime(System.currentTimeMillis());
        VideoCallActivity.endCall(mIdentifier,host,isAnswer,true);
        finish();
    }
}
