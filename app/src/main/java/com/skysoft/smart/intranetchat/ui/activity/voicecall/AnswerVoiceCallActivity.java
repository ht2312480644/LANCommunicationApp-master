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
import com.skysoft.smart.intranetchat.app.impl.OnReceiveCallHungUp;
import com.skysoft.smart.intranetchat.app.impl.OnReceiveRequestConsent;
import com.skysoft.smart.intranetchat.bean.RecordCallBean;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomConfig;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

public class AnswerVoiceCallActivity extends AppCompatActivity {

    private String TAG = AnswerVoiceCallActivity.class.getSimpleName();
    private ImageView refuseCall;
    private ImageView consentCall;
    private TextView mName;
    private ImageView headImg;
    private String host;

    private String name;
    private String imgPath;

    private Timer consentOutTimer;
    private long intervalTime = 550;
    private long lastRequestConsentTime;
    private Timer affirmRequestConsentTimer;
    private String mIdentifier;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);
        CustomStatusBarBackground.customStatusBarTransparent(this);
        CustomStatusBarBackground.colorViewStatusBar(this,R.color.color_black,findViewById(R.id.custom_status_bar_background));
        IntranetChatApplication.getsCallback().setHungUpInAnswer(hungUp);
        IntranetChatApplication.getsCallback().setOnReceiveRequestConsent(onReceiveRequestConsent);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        mIdentifier = bundle.getString("identifier");
        host = bundle.getString("host");
        name = bundle.getString("name");
        imgPath = bundle.getString("imgPath");

        mName = findViewById(R.id.answer_voice_phone_name);
        mName.setText(name);
        headImg = findViewById(R.id.answer_voice_phone_img);
        if (!TextUtils.isEmpty(imgPath)){
            Glide.with(this).load(imgPath).into(headImg);
        }else {
            Glide.with(this).load(R.drawable.default_head).into(headImg);
        }

        refuseCall = findViewById(R.id.answer_voice_phone_cancel);
        consentCall = findViewById(R.id.answer_voice_phone_accept);

        refuseCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VoiceCall.refuseVoiceCall(host);
                IntranetChatApplication.setInCall(false);
                EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_REFUSE_ANSWER_MINE,host,true));
                finish();
            }
        });

        consentCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VoiceCall.consentVoiceCall(host);
                VoiceCallActivity.go(AnswerVoiceCallActivity.this, host,name,imgPath,mIdentifier,true);
                finish();
            }
        });

        lastRequestConsentTime = System.currentTimeMillis();
        consentOutTime();
    }

    private void consentOutTime(){
        consentOutTimer = new Timer();
        TimerTask consentOutTask = new TimerTask() {
            @Override
            public void run() {
                TLog.d(TAG, "run: responseConsentOutTime");
                VoiceCall.responseConsentOutTime(host);
                IntranetChatApplication.setInCall(false);
                EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_OUT_TIME_ANSWER,host,true));
                finish();
            }
        };
        consentOutTimer.schedule(consentOutTask,60*1000);

        affirmRequestConsentTimer = new Timer();
        TimerTask affirmRequestConsentTask = new TimerTask() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - lastRequestConsentTime > intervalTime){
                    TLog.d(TAG, "run: affirmRequestConsentTimer answer voice call");
                    VoiceCall.hungUpVoiceCall(host);
                    IntranetChatApplication.setInCall(false);
                    EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_DIE_ANSWER,host,true));
                    finish();
                }
            }
        };
        affirmRequestConsentTimer.schedule(affirmRequestConsentTask,100,300);
    }

    private OnReceiveCallHungUp hungUp = new OnReceiveCallHungUp() {
        @Override
        public void onReceiveHungUpVoiceCall(String host) {
            if (!AnswerVoiceCallActivity.this.host.equals(host)){
                return;
            }
            IntranetChatApplication.setInCall(false);
            EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_REFUSE_ANSWER,host,true));
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

    public static void go(Activity activity, String host, String name, String imgPath,String identifier) {
        Intent intent = new Intent(activity, AnswerVoiceCallActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("imgPath", imgPath);
        bundle.putString("host", host);
        bundle.putString("identifier",identifier);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        VoiceCall.hungUpVoiceCall(host);
        IntranetChatApplication.setInCall(false);
        EventBus.getDefault().post(new RecordCallBean(mIdentifier,ChatRoomConfig.CALL_REFUSE_ANSWER_MINE,host,true));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (consentOutTimer != null){
            consentOutTimer.cancel();
        }
        if (affirmRequestConsentTimer != null){
            affirmRequestConsentTimer.cancel();
        }
    }
}
