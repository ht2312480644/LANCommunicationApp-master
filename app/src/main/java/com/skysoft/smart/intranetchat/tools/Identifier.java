/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Create a file service for file recipients to download files.
 */
package com.skysoft.smart.intranetchat.tools;

import android.text.TextUtils;
import com.skysoft.smart.intranetchat.tools.toastutil.TLog;

import java.security.MessageDigest;

public class Identifier {

    public String getUserIdentifier(String userName){
        String ref = userName;
        return getIdentifier(ref);
    }

    public String getFileIdentifier(String path){
        String ref = path + String.valueOf(System.currentTimeMillis());
        return getIdentifier(ref);
    }

    private String getIdentifier(String ref) {
        // 16进制数组
        char hexDigits[] = { 'g', '0', 'p', '6', 'x', '9', '3', '2', '5', 'q', 'b', 'j', 'e', 'k', 'm', 'y' };
        try {
            char str[];
            byte strTemp[] = ref.getBytes();
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(strTemp);
            // 获取加密后的数组
            byte md[] = messageDigest.digest();
            str = new char[md.length];
            int k = 0;
            // 将数组做位移
            for (int i = 0; i < md.length; i++) {
                byte byte0 = md[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }

    private String TAG = Identifier.class.getSimpleName();
    public String getGroupIdentifier(String... identifiers){
        if (identifiers == null){
            return null;
        }
        int[] newIdentifier = new int[identifiers[0].length()];
        for (int i = 0; i < newIdentifier.length; i++){
            for(int j = 0; j < identifiers.length; j++){
                newIdentifier[i] += identifiers[j].charAt(i);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < newIdentifier.length; i++){
            sb.append(newIdentifier[i]);
        }
        String temp = sb.toString();
        return getIdentifier(temp);
    }

    public String getGroupIdentifier(StringBuilder stringBuilder){
        return getIdentifier(stringBuilder.toString());
    }

    public String getDefaultAvatarIdentifier(){
        return getIdentifier("avatar");
    }
}
