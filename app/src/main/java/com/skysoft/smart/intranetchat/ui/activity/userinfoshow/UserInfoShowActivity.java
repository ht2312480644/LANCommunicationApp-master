package com.skysoft.smart.intranetchat.ui.activity.userinfoshow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.model.net_model.Login;
import com.skysoft.smart.intranetchat.database.table.ContactEntity;
import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.ChatRoom.ChatRoomConfig;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserInfoShowActivity extends AppCompatActivity {
    private static String TAG = UserInfoShowActivity.class.getSimpleName();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_user_info_show);
        CustomStatusBarBackground.customStatusBarTransparent(this);
        CustomStatusBarBackground.drawableViewStatusBar(this,R.drawable.custom_gradient_main_title,findViewById(R.id.custom_status_bar_background));

        ViewHolder holder = new ViewHolder();
        holder.avatar = findViewById(R.id.user_info_head);
        holder.name = findViewById(R.id.user_info_name);
        holder.state = findViewById(R.id.user_info_state);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String name = bundle.getString(ChatRoomConfig.NAME);
        String identifier = bundle.getString(ChatRoomConfig.IDENTIFIER);
        String avatarPath = bundle.getString(ChatRoomConfig.AVATAR);

        IntranetChatApplication.setShowUserInfoAvatar(holder.avatar);
        IntranetChatApplication.setShowUserInfoName(holder.name);
        IntranetChatApplication.setShowUserState(holder.state);
        IntranetChatApplication.setFilterIdentifier(identifier);
        IntranetChatApplication.setInShowUserInfoActivity(true);

        String host = null;
        TLog.d(TAG, "onCreate: identifier = " + identifier + ", avatarPath = " + avatarPath);
        ContactEntity next = IntranetChatApplication.sContactMap.get(identifier);
        if (null != next){
            switch (next.getStatus()){
                case Config.STATUS_ONLINE:
                    holder.state.setText(getString(R.string.user_state_online));
                    holder.state.setTextColor(getResources().getColor(R.color.color_green));
                    break;
                case Config.STATUS_BUSY:
                    holder.state.setText(getString(R.string.user_state_busy));
                    holder.state.setTextColor(getResources().getColor(R.color.color_red));
                    break;
                case Config.STATUS_OUT_LINE:
                    holder.state.setText(getString(R.string.user_state_out_line));
                    holder.state.setTextColor(getResources().getColor(R.color.color_gray));
                    break;
                default:
                    TLog.d(TAG, "onCreate: next.status() = " + next.getStatus());
                    break;
            }
            host = next.getHost();
        }
        TLog.d(TAG, "onCreate: host = " + host);
        if (!TextUtils.isEmpty(host)){
            TLog.d(TAG, "onCreate: host = " + host);
            Login.requestUserInfo(host);
        }
        holder.name.setText(name);
        if (!TextUtils.isEmpty(avatarPath)){
            Glide.with(this).load(avatarPath).into(holder.avatar);
        }
    }

    public class ViewHolder{
        CircleImageView avatar;
        TextView name;
        TextView state;
    }

    public static void go(Context context,String name,String avatar,String identifier){
        Intent intent = new Intent(context, UserInfoShowActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(ChatRoomConfig.NAME,name);
        bundle.putString(ChatRoomConfig.AVATAR,avatar);
        bundle.putString(ChatRoomConfig.IDENTIFIER,identifier);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!IntranetChatApplication.isRequestAvatar()){
            IntranetChatApplication.setFilterIdentifier(null);
        }
        IntranetChatApplication.setShowUserInfoName(null);
        IntranetChatApplication.setShowUserInfoAvatar(null);
        IntranetChatApplication.setShowUserState(null);
        IntranetChatApplication.setInShowUserInfoActivity(false);
        finish();
    }
}
