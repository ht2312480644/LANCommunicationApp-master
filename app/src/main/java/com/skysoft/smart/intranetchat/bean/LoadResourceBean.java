/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/11/6
 * Description: [Intranet Chat] [APP][UI] Group chat
 */
package com.skysoft.smart.intranetchat.bean;

import com.skysoft.smart.intranetchat.database.table.ChatRecordEntity;
import com.skysoft.smart.intranetchat.model.network.bean.ReceiveAndSaveFileBean;

public class LoadResourceBean {
    private ReceiveAndSaveFileBean rasfb;
    private ChatRecordEntity recordEntity;

    public LoadResourceBean(ReceiveAndSaveFileBean rasfb,ChatRecordEntity recordEntity) {
        this.rasfb = rasfb;
        this.recordEntity = recordEntity;
    }

    public ReceiveAndSaveFileBean getRasfb() {
        return rasfb;
    }

    public void setRasfb(ReceiveAndSaveFileBean rasfb) {
        this.rasfb = rasfb;
    }

    public ChatRecordEntity getRecordEntity() {
        return recordEntity;
    }

    public void setRecordEntity(ChatRecordEntity recordEntity) {
        this.recordEntity = recordEntity;
    }
}
