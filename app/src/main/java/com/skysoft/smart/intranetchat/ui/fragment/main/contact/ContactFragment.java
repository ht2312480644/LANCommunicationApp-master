/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/15
 * Description: [PT-40][Intranet Chat] [APP][UI] Home page ui
 */
package com.skysoft.smart.intranetchat.ui.fragment.main.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.skysoft.smart.intranetchat.R;
import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.tools.customstatusbar.CustomStatusBarBackground;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ContactFragment extends Fragment {

    private ListView contactListView;
    private View statusBar;
//B: [PT-60][Intranet Chat] [APP][UI] contact list page,Allen Luo,2019/10/30
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


//        contactBeanList = IntranetChatApplication.getContactList();
        View root = inflater.inflate(R.layout.fragment_main_contact, container, false);
        statusBar = root.findViewById(R.id.custom_status_bar_background);
        CustomStatusBarBackground.drawableViewStatusBar(getContext(),R.drawable.custom_gradient_main_title,statusBar);
        contactListView = root.findViewById(R.id.list_main_contact);
        TextView title = root.findViewById(R.id.page_title);
        title.setText("联系人");
        IntranetChatApplication.setsContactListAdapter(new ContactListAdapter(IntranetChatApplication.getsContactList(),getContext()));
        contactListView.setAdapter(IntranetChatApplication.getsContactListAdapter());
        return root;
    }
//E: [PT-60][Intranet Chat] [APP][UI] contact list page,Allen Luo,2019/10/30
}