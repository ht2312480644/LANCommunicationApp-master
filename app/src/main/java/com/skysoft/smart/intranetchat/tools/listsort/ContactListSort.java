package com.skysoft.smart.intranetchat.tools.listsort;

import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.database.table.ContactEntity;
import com.skysoft.smart.intranetchat.database.table.LatestChatHistoryEntity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ContactListSort {
    public static void contactListSort(List<String> list){
        Collections.sort(list, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                ContactEntity contactEntity1 = IntranetChatApplication.sContactMap.get(o1);
                ContactEntity contactEntity2 = IntranetChatApplication.sContactMap.get(o2);
                int i = contactEntity1.getStatus() - contactEntity2.getStatus();
                return i;
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }
        });
    }
}
