/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Allen Luo on 2019/10/30
 * Description: [PT-60][Intranet Chat] [APP][UI] contact list page
 */
package com.skysoft.smart.intranetchat.tools.listsort;

import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import com.skysoft.smart.intranetchat.app.IntranetChatApplication;
import com.skysoft.smart.intranetchat.database.table.LatestChatHistoryEntity;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MessageListSort {

    public static boolean Today(List<String> myBeanList){
        int i = 0;
        for (; i < myBeanList.size(); i++){
            LatestChatHistoryEntity historyEntity = IntranetChatApplication.sLatestChatHistoryMap.get(myBeanList.get(i));
            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
            String dateStr = dateformat.format(historyEntity.getContentTimeMill());
            TLog.d("dateformat", "onCreateView:  " + dateStr);
            historyEntity.setContentTime(dateStr);
            TLog.d("ContentTimeMill", "onCreateView:  " + historyEntity.getContentTimeMill());
        }
        return true;
    }
    public static void CollectionsList(List<String> myBeanList) {
        Today(myBeanList);
        Collections.sort(myBeanList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                LatestChatHistoryEntity historyEntity1 = IntranetChatApplication.sLatestChatHistoryMap.get(o1);
                LatestChatHistoryEntity historyEntity2 = IntranetChatApplication.sLatestChatHistoryMap.get(o2);
                int i = historyEntity1.getTop()
                        - historyEntity2.getTop();
                if(i == 0){
                    return historyEntity1.getContentTimeMill() - historyEntity2.getContentTimeMill() > 0 ? -1 : 1;
                }
                return -i;
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }
        });
    }
}
