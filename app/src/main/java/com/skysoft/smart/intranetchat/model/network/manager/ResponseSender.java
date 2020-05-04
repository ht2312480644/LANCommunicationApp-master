/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Create a file service for file recipients to download files.
 */
package com.skysoft.smart.intranetchat.model.network.manager;

import com.skysoft.smart.intranetchat.model.network.Config;
import com.skysoft.smart.intranetchat.model.network.bean.ResponseBean;
import com.skysoft.smart.intranetchat.tools.GsonTools;

public class ResponseSender {
    public static void response(int response,String identifier,String host){
        ResponseBean responseBean = new ResponseBean(response,identifier);
        Sender.sender(GsonTools.toJson(responseBean),Config.CODE_RESPONSE,host);
    }

    public static void response(int response,String identifier,String host,String remark){
        ResponseBean responseBean = new ResponseBean(response,identifier);
        responseBean.setRemark(remark);
        Sender.sender(GsonTools.toJson(responseBean),Config.CODE_RESPONSE,host);
    }
}
