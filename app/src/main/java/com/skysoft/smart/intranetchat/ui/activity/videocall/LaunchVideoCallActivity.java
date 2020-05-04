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
import com.skysoft.smart.intranetchat.app.impl.HandleVoiceCallResponse;
import com.skysoft.smart.intranetchat.app.impl.OnReceiveCallHungUp;
import com.skysoft.smart.intranetchat.bean.InCallBean;
import com.skysoft.smart.intranetchat.bean.RecordCallBean;
import com.skysoft.smart.intranetchat.model.camera.manager.MyShowCaptureManager;
import com.skysoft.smart.intranetchat.model.camera.videocall.Sender;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomConfig;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Timer;
import java.util.TimerTask;

public class LaunchVideoCallActivity extends AppCompatActivity {

    private String TAG = LaunchVideoCallActivity.class.getSimpleName();
    private ImageView hungVideoCall;
    private ImageView mHeadImg;
    private TextView mName;
    private TextureView mTexture;
    private MyShowCaptureManager mShowCaptureManager;
    private String host;
    private long lastWaitingConsentCall;
    private long intervalTime = 550;
    private Timer affirmWaitingTimer;
    private Timer requestConsentTimer;
    private String mIdentifier;
    private String mImagePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_video_call);
        CustomStatusBarBackground.customStatusBarTransparent(this);
        CustomStatusBarBackground.colorViewStatusBar(this,R.color.color_black,findViewById(R.id.custom_status_bar_background));


        IntranetChatApplication.getsCallback().setHandleVoiceCallResponse(handleVoiceCallResponse);
        IntranetChatApplication.getsCallback().setHungUpInAnswer(onReceiveCallHungUp);
        hungVideoCall = findViewById(R.id.start_video_call_hang_up);
        mName = findViewById(R.id.activity_launch_video_name);
        mHeadImg = findViewById(R.id.activity_launch_video_head_img);
        mTexture = findViewById(R.id.activity_launch_video_show);
        EventBus.getDefault().register(this);
//        mTexture.setSurfaceTextureListener(mSurfaceTextureListener);
//        mShowCaptureManager = new MyShowCaptureManager(this, mTexture);
//        mShowCaptureManager.startBackgroundThread();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        host = bundle.getString("host");
        mIdentifier = bundle.getString("identifier");
        mName.setText(bundle.getString("name"));
        mImagePath = bundle.getString("imgPath");
        VoiceCall.startVideoCall(IntranetChatApplication.getsMineUserInfo(), host);
        if (!TextUtils.isEmpty(mImagePath)){
            Glide.with(this).load(mImagePath).into(mHeadImg);
        }else {
            Glide.with(this).load(R.drawable.default_head).into(mHeadImg);
        }

        hungVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VoiceCall.hungUpVoiceCall(host);
                IntranetChatApplication.setInCall(false);
                EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_REFUSE_LAUNCH_MINE,host,false));
                finish();
            }
        });

        monitor();
        Sender.mInputDatasQueue.clear();
        TLog.d(TAG, "onClick: send queen "+ Sender.mInputDatasQueue.size()+" receiver : "+IntranetChatApplication.getmDatasQueue().size());

        TLog.d(TAG, "onCreate: onReceiveInCall " + this);
    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
//            mShowCaptureManager.openCamera(width, height);
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

    public void monitor() {
        lastWaitingConsentCall = System.currentTimeMillis();
        affirmWaitingTimer = new Timer();
        TimerTask affirmWaitingTask = new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - lastWaitingConsentCall > intervalTime) {
                    VoiceCall.hungUpVoiceCall(host);
                    IntranetChatApplication.setInCall(false);
                    EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_DIE_LAUNCH,host,false));
                    finish();
                }
            }
        };
        affirmWaitingTimer.schedule(affirmWaitingTask, 100, 1000);

        //请求接听电话
        requestConsentTimer = new Timer();
        TimerTask requestConsentTask = new TimerTask() {
            @Override
            public void run() {
                VoiceCall.requestConsentVoiceCall(host);
            }
        };
        requestConsentTimer.schedule(requestConsentTask, 100, 300);
    }

    private HandleVoiceCallResponse handleVoiceCallResponse = new HandleVoiceCallResponse() {
        @Override
        public void onReceiveConsentVoiceCall(String host) {
            TLog.d(TAG, "onReceiveConsentVoiceCall: " + host);
            VideoCallActivity.go(LaunchVideoCallActivity.this, host,mIdentifier,false);
            finish();
        }

        @Override
        public void onReceiveRefuseVoiceCall(String host) {
            TLog.d(TAG, "onReceiveRefuseVoiceCall: " + host);
            if (!LaunchVideoCallActivity.this.host.equals(host)){
                return;
            }
            EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_REFUSE_LAUNCH,host,false));
            IntranetChatApplication.setInCall(false);
            finish();
        }

        @Override
        public void onReceiveWaitingConsentCall() {
            lastWaitingConsentCall = System.currentTimeMillis();
            TLog.d(TAG, "onReceiveWaitingConsentCall: " + lastWaitingConsentCall);
        }

        @Override
        public void onReceiveConsentOutTime() {
            TLog.d(TAG, "onReceiveConsentOutTime: ");
            IntranetChatApplication.setInCall(false);
            EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_OUT_TIME_LAUNCH,host,false));
            finish();
        }

        @Override
        public void onReceiveInCall(String host) {
            TLog.d(TAG, "onReceiveInCall: " + host);
            if (!LaunchVideoCallActivity.this.host.equals(host)){
                return;
            }
//            Toast.makeText(LaunchVideoCallActivity.this,"对方正在通话中",Toast.LENGTH_SHORT).show();
            EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_IN_CALL,host,false));
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveInCall(InCallBean inCallBean){
        TLog.d(TAG, "onReceiveInCall: Main Thread");
        hungVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        findViewById(R.id.start_video_in_call).setVisibility(View.VISIBLE);
        Timer inCall = new Timer();
        TimerTask inCallTask = new TimerTask() {
            @Override
            public void run() {
                TLog.d(TAG, "run: finish");
                IntranetChatApplication.setInCall(false);
                finish();
            }
        };
        inCall.schedule(inCallTask,1000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        VoiceCall.hungUpVoiceCall(host);
        IntranetChatApplication.setInCall(false);
        EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_REFUSE_LAUNCH_MINE,host,false));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (affirmWaitingTimer != null) {
            affirmWaitingTimer.cancel();
        }
        if (requestConsentTimer != null) {
            requestConsentTimer.cancel();
        }
    }

    public static void go(Activity activity) {
        Intent intent = new Intent(activity, LaunchVideoCallActivity.class);
        activity.startActivity(intent);
    }

    public static void go(Activity activity, String host, String name, String imgPath,String identifier) {
        Intent intent = new Intent(activity, LaunchVideoCallActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("imgPath", imgPath);
        bundle.putString("host", host);
        bundle.putString("identifier",identifier);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    private OnReceiveCallHungUp onReceiveCallHungUp = new OnReceiveCallHungUp() {
        @Override
        public void onReceiveHungUpVoiceCall(String host) {
            TLog.d(TAG, "onReceiveHungUpVoiceCall: " + host);
            if (!LaunchVideoCallActivity.this.host.equals(host)){
                return;
            }
            IntranetChatApplication.setInCall(false);
            EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_REFUSE_LAUNCH,host,false));
            finish();
        }
    };
}
