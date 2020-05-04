/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/15
 * Description: [PT-40][Intranet Chat] [APP][UI] Home page ui
 */
package com.skysoft.smart.intranetchat.ui.fragment.main.mine;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.model.net_model.Login;
import com.skysoft.smart.intranetchat.tools.QuickClickListener;
import com.skysoft.smart.intranetchat.tools.toastutil.ToastUtil;
import com.skysoft.smart.intranetchat.ui.activity.camera.CameraActivity;
import com.skysoft.smart.intranetchat.ui.activity.camera.ClipImageActivity;
import com.skysoft.smart.intranetchat.model.camera.entity.EventMessage;
import com.skysoft.smart.intranetchat.database.MyDataBase;
import com.skysoft.smart.intranetchat.database.table.MineInfoEntity;
import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.model.network.bean.UserInfoBean;
import com.skysoft.smart.intranetchat.tools.Identifier;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class MineFragment extends Fragment implements View.OnClickListener{
    //B:[Intranet Chat] [APP][UI] Chat Room,Oliver Ou,2019/11/6
    private String TAG = MineFragment.class.getSimpleName();
    private static BottomSheetDialog bottomSheetDialog;
    private TextView mineName;
    private Spinner stateSpinner;
    private ConstraintLayout alterName;
    private CircleImageView mineAvatar;
    private String[] status;
    private ArrayAdapter<String> stateSpinnerAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main_mine, container, false);
        EventBus.getDefault().register(this);
        status = getActivity().getResources().getStringArray(R.array.spingarr);
        mineName = root.findViewById(R.id.mine_info_name);
        alterName = root.findViewById(R.id.mine_info_name_alter);
        stateSpinner = root.findViewById(R.id.mine_info_state_spinner);
        mineAvatar = root.findViewById(R.id.mine_info_head);

        if (IntranetChatApplication.getsMineUserInfo().getName()!=null){
            mineName.setText(IntranetChatApplication.getsMineUserInfo().getName());
            TLog.d(TAG, "onCreateView: IntranetChatApplication.getsMineUserInfo().getName() =null");
        }
        TLog.d(TAG, "onCreateView: IntranetChatApplication.getsMineAvatarPath() = " + IntranetChatApplication.getsMineAvatarPath());
        if (!TextUtils.isEmpty(IntranetChatApplication.getsMineAvatarPath())){
            Glide.with(root).load(IntranetChatApplication.getsMineAvatarPath()).into(mineAvatar);
        }else {
            Glide.with(root).load(R.drawable.default_head).into(mineAvatar);
        }

        switch (IntranetChatApplication.getsMineUserInfo().getStatus()){
            case Config.STATUS_ONLINE:
                stateSpinner.setSelection(0);
                break;
            case Config.STATUS_BUSY:
                stateSpinner.setSelection(1);
                break;
        }
        stateSpinnerAdapter = new ArrayAdapter<String>(getContext(),R.layout.custom_spinner_item,status);

        stateSpinnerAdapter.setDropDownViewResource(R.layout.custom_spinner_style);

        //将adapter 添加到spinner中
        stateSpinner.setAdapter(stateSpinnerAdapter);
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int status = 0;
                switch (position){
                    case 0:
                        status = Config.STATUS_ONLINE;
                        break;
                    case 1:
                        status = Config.STATUS_BUSY;
                        break;
                }
                if (status != IntranetChatApplication.getsMineUserInfo().getStatus()){
                    TLog.d(TAG, "onItemSelected: 改变了状态。status = " + status);
                    IntranetChatApplication.getsMineUserInfo().setStatus(status);
                    Login.broadcastUserInfo();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        alterName.setOnClickListener(this::onClick);
        mineAvatar.setOnClickListener(this::onClick);
        return root;
    }

    @Override
    public void onClick(View v) {
        if (QuickClickListener.isFastClick()) {
            switch (v.getId()) {
                case R.id.mine_info_name_alter:
                    changeUserName();
                    break;
                case R.id.mine_info_head:
                    dialogView();
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveEventMessage(EventMessage eventMessage){
        TLog.d(TAG, "onReceiveEventMessage: eventMessage.getType() = " + eventMessage.getType());
        if (eventMessage.getType() == 4){
            if (!TextUtils.isEmpty(eventMessage.getMessage())){
                Glide.with(getContext()).load(eventMessage.getMessage()).into(mineAvatar);
                Identifier identifier = new Identifier();
                String avatarIdentifier = identifier.getFileIdentifier(eventMessage.getMessage());
                IntranetChatApplication.getsMineUserInfo().setAvatarIdentifier(avatarIdentifier);
                IntranetChatApplication.setsMineAvatarPath(eventMessage.getMessage());
                Login.broadcastChangeAvatar();
//                Login.broadcastUserInfo();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MyDataBase.getInstance().getMineInfoDao().update(generatorMineInfo());
                    }
                }).start();
            }
        }
    }

    private void changeUserName() {
        View inflate = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_input_name, null);
        TextView inputNewName = inflate.findViewById(R.id.dialog_input);
        inputNewName.setText(IntranetChatApplication.getsMineUserInfo().getName());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        AlertDialog alertDialog = builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = inputNewName.getText().toString();
                if (TextUtils.isEmpty(newName) || newName.equals(IntranetChatApplication.getsMineUserInfo().getName())){
                    return;
                }
                if (newName.length() <= 1 || newName.length() > 12){
                    ToastUtil.toast(getActivity(),getString(R.string.limit_name_length));
                    return;
                }
                mineName.setText(newName);
                IntranetChatApplication.getsMineUserInfo().setName(newName);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MineInfoEntity mineInfoEntity = new MineInfoEntity();
                        MyDataBase.getInstance().getMineInfoDao().update(generatorMineInfo());
                    }
                }).start();
                Login.broadcastUserInfo();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).create();
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setView(inflate);
        alertDialog.show();
    }

    public MineInfoEntity generatorMineInfo(){
        MineInfoEntity mineInfoEntity = new MineInfoEntity();
        UserInfoBean mineUserInfo = IntranetChatApplication.getsMineUserInfo();
        mineInfoEntity.setMineHeadIdentifier(mineUserInfo.getAvatarIdentifier());
        mineInfoEntity.setMineIdentifier(mineUserInfo.getIdentifier());
        mineInfoEntity.setMineName(mineUserInfo.getName());
        mineInfoEntity.setMineHeadPath(IntranetChatApplication.getsMineAvatarPath());
        mineInfoEntity.setId(0);
        return mineInfoEntity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    //E:[Intranet Chat] [APP][UI] Chat Room, Oliver Ou,2019/11/6
    public void dialogView(){
        bottomSheetDialog = new BottomSheetDialog(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_photo_choose, null);
        dialogView.findViewById(R.id.dialog_button_photograph).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraActivity.goActivity(getContext(),true);
                bottomSheetDialog.dismiss();
            }
        });
        dialogView.findViewById(R.id.dialog_button_map_depot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goPhotoAlbum();
                bottomSheetDialog.dismiss();
            }
        });
        dialogView.findViewById(R.id.dialog_button_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();
    }

    private void goPhotoAlbum() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 2);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 2 && resultCode == RESULT_OK) {
            //图片
            ClipImageActivity.goActivity(getContext(), data.getData(),true);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
