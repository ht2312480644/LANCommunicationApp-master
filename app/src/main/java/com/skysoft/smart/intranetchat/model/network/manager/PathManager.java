/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Create a file service for file recipients to download files.
 */
package com.skysoft.smart.intranetchat.model.network.manager;

import com.skysoft.smart.intranetchat.model.network.Config;

public class PathManager {
    public static String fromType(int type){
        String path = null;
        switch (type){
            case Config.FILE_AVATAR:
                path = Config.PATH_AVATAR;
                break;
            case Config.FILE_COMMON:
                path = Config.PATH_RECEIVE_FILE;
                break;
            case Config.FILE_PICTURE:
                path = Config.PATH_IMAGE;
                break;
            case Config.FILE_VIDEO:
                path = Config.PATH_VIDEO;
                break;
            case Config.FILE_VOICE:
                path = Config.PATH_VOICE;
                break;
        }
        return path;
    }
}
