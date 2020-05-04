/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/15
 * Description: [PT-40][Intranet Chat] [APP][UI] Home page ui
 */
package com.skysoft.smart.intranetchat.ui.fragment.main.message;

import android.os.Bundle;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;
import com.skysoft.smart.intranetchat.tools.listsort.MessageListSort;
import com.skysoft.smart.intranetchat.tools.QuickClickListener;
import com.skysoft.smart.intranetchat.ui.activity.chatroom.EstablishGroup.EstablishGroupActivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class MessageFragment extends Fragment {

    private static String TAG = MessageFragment.class.getSimpleName();
    private ListView messageListView;
    private TextView mEstablishGroup;
    private View statusBar;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        MessageListSort.CollectionsList(IntranetChatApplication.getMessageList());
        View root = inflater.inflate(R.layout.fragment_main_message, container, false);
        statusBar = root.findViewById(R.id.custom_status_bar_background);
        CustomStatusBarBackground.drawableViewStatusBar(getContext(),R.drawable.custom_gradient_main_title,statusBar);
        TextView title = root.findViewById(R.id.page_title);
        title.setText("消息");
        messageListView = root.findViewById(R.id.list_main_message);
        IntranetChatApplication.setsMessageListAdapter(new MessageListAdapter(IntranetChatApplication.getMessageList(),getContext()));
        messageListView.setAdapter(IntranetChatApplication.getsMessageListAdapter());
        mEstablishGroup = root.findViewById(R.id.page_establish_group);
        mEstablishGroup.setText(getResources().getText(R.string.MessageFragment_mEstablishGroup));
        mEstablishGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (QuickClickListener.isFastClick(300)) {
                    TLog.d(TAG, "onClick: ");
                    EstablishGroupActivity.go(getActivity(), "group", false);
                }
            }
        });
        return root;
    }
}