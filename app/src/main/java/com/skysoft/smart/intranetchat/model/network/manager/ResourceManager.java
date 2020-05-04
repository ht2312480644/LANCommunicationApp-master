/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description: [Intranet Chat] [APP] [Communication]Create a file service for file recipients to download files.
 */
package com.skysoft.smart.intranetchat.model.network.manager;

import com.skysoft.smart.intranetchat.model.network.bean.ResourceManagerBean;

import java.util.HashMap;
import java.util.Map;

public class ResourceManager {
    private Map<String, ResourceManagerBean> resource = new HashMap<>();
    private ResourceManager(){}
    private static ResourceManager sInstance = new ResourceManager();

    public static ResourceManager getInstance(){
        return sInstance;
    }

    public ResourceManagerBean getResource(String key) {
        return resource.get(key);
    }

    public void setResource(String key,ResourceManagerBean value) {
        if (resource.containsKey(key)){
            resource.remove(key);
        }
        this.resource.put(key,value);
    }

    public boolean exist(String key){
        return resource.containsKey(key);
    }

    public boolean remove(String key){
        ResourceManagerBean bean = resource.remove(key);
        return bean == null;
    }

    public int size(){
        return resource.size();
    }
}
