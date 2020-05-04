/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/6
 * Description: [Intranet Chat] [APP] [Communication]Voice or video communication function
 */
package com.skysoft.smart.intranetchat.ui.activity.videocall;

import android.app.Activity;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.text.TextUtils;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.model.net_model.VoiceCall;
import com.skysoft.smart.intranetchat.app.impl.OnReceiveCallHungUp;
import com.skysoft.smart.intranetchat.app.impl.OnReceiveRequestConsent;
import com.skysoft.smart.intranetchat.bean.RecordCallBean;
import com.skysoft.smart.intranetchat.model.camera.manager.MyShowCaptureManager;
import com.skysoft.smart.intranetchat.model.camera.videocall.Sender;
import com.skysoft.smart.intranetchat.model.network.bean.UserInfoBean;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomConfig;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

public class AnswerVideoCallActivity extends AppCompatActivity {

    private String TAG = AnswerVideoCallActivity.class.getSimpleName();
    private ImageView refuseCall;
    private ImageView consentCall;
    private ImageView mHeadImg;
    private TextView mName;
    private TextureView mTexture;
    private MyShowCaptureManager mShowCaptureManager;
    private String host;
    private Timer consentOutTimer;
    private long intervalTime = 550;
    private long lastRequestConsentTime;
    private Timer affirmRequestConsentTimer;
    private String mIdentifier;
    private String mImagePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_video_call);
        CustomStatusBarBackground.customStatusBarTransparent(this);
        CustomStatusBarBackground.colorViewStatusBar(this,R.color.color_black,findViewById(R.id.custom_status_bar_background));
        IntranetChatApplication.getsCallback().setHungUpInAnswer(hungUp);
        IntranetChatApplication.getsCallback().setOnReceiveRequestConsent(onReceiveRequestConsent);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mIdentifier = bundle.getString("identifier");
        host = bundle.getString("host");

        refuseCall = findViewById(R.id.activity_answer_refuse_video_call);
        consentCall = findViewById(R.id.activity_answer_consent_video_call);
        mHeadImg = findViewById(R.id.activity_answer_video_head_img);
        mName = findViewById(R.id.activity_answer_video_name);
        mTexture = findViewById(R.id.activity_answer_video_show);
//        mTexture.setSurfaceTextureListener(mSurfaceTextureListener);
//        mShowCaptureManager = new MyShowCaptureManager(this, mTexture);
//        mShowCaptureManager.startBackgroundThread();

        mName.setText(bundle.getString("name"));
        mImagePath = bundle.getString("imgPath");
        if (!TextUtils.isEmpty(mImagePath)){
            Glide.with(this).load(mImagePath).into(mHeadImg);
        }else {
            Glide.with(this).load(R.drawable.default_head).into(mHeadImg);
        }

        refuseCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VoiceCall.refuseVoiceCall(host);
                IntranetChatApplication.setInCall(false);
                EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_REFUSE_ANSWER_MINE,host,false));
                finish();
            }
        });

        consentCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                affirmRequestConsentTimer.cancel();
                VoiceCall.consentVoiceCall(host);
                VideoCallActivity.go(AnswerVideoCallActivity.this,host,mIdentifier,true);
                finish();
            }
        });

        lastRequestConsentTime = System.currentTimeMillis();
        consentOutTime();
        Sender.mInputDatasQueue.clear();
        TLog.d(TAG, "onClick: send queen "+ Sender.mInputDatasQueue.size()+" receiver : "+IntranetChatApplication.getmDatasQueue().size());

    }

    private void consentOutTime(){
        consentOutTimer = new Timer();
        TimerTask consentOutTask = new TimerTask() {
            @Override
            public void run() {
                TLog.d(TAG, "run: responseConsentOutTime");
                VoiceCall.responseConsentOutTime(host);
                IntranetChatApplication.setInCall(false);
                EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_OUT_TIME_ANSWER,host,false));
                finish();
            }
        };
        consentOutTimer.schedule(consentOutTask,60*1000);

        affirmRequestConsentTimer = new Timer();
        TimerTask affirmRequestConsentTask = new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - lastRequestConsentTime > intervalTime){
                    VoiceCall.hungUpVoiceCall(host);
                    IntranetChatApplication.setInCall(false);
                    EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_DIE_ANSWER,host,false));
                    finish();
                }
            }
        };
        affirmRequestConsentTimer.schedule(affirmRequestConsentTask,100,300);
    }

    private OnReceiveCallHungUp hungUp = new OnReceiveCallHungUp() {
        @Override
        public void onReceiveHungUpVoiceCall(String host) {
            if (!AnswerVideoCallActivity.this.host.equals(host)){
                return;
            }
            IntranetChatApplication.setInCall(false);
            EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_REFUSE_ANSWER,host,false));
            finish();
        }
    };

    private OnReceiveRequestConsent onReceiveRequestConsent = new OnReceiveRequestConsent() {
        @Override
        public void onReceiveRequestConsent() {
            lastRequestConsentTime = System.currentTimeMillis();
            TLog.d(TAG, "onReceiveRequestConsent: " + lastRequestConsentTime);
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        VoiceCall.hungUpVoiceCall(host);
        IntranetChatApplication.setInCall(false);
        EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_REFUSE_ANSWER_MINE,host,false));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mShowCaptureManager.closeCamera();
//        mShowCaptureManager.stopBackgroundThread();
        if (consentOutTimer != null){
            consentOutTimer.cancel();
        }
        if (affirmRequestConsentTimer != null){
            affirmRequestConsentTimer.cancel();
        }
    }

    public static void go(Activity activity, String host, String name, String imgPath,String identifier) {
        Intent intent = new Intent(activity, AnswerVideoCallActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("imgPath", imgPath);
        bundle.putString("host", host);
        bundle.putString("identifier",identifier);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }
    public static void go(Activity activity, UserInfoBean userInfoBean, String host) {
        Intent intent = new Intent(activity, AnswerVideoCallActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("userInfoBean", userInfoBean);
        bundle.putString("host", host);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            mShowCaptureManager.openCamera(width, height);
            TLog.d(TAG, "onSurfaceTextureAvailable: ");
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            TLog.d(TAG, "onSurfaceTextureSizeChanged: ");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

}
