/***
 * Copyright (c) 2019 ASKEY Computer Corp. and/or its affiliates. All rights reserved.
 * Created by Oliver Ou on 2019/10/14
 * Description:[Intranet Chat] [APP] [Communication]File message sending and receiving (excluding file data transfer).
 */
package com.skysoft.smart.intranetchat.model.network.bean;

public class AskResourceBean {
    private int resourceType;
    private String resourceUniqueIdentifier;

    public int getResourceType() {
        return resourceType;
    }

    public void setResourceType(int resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceUniqueIdentifier() {
        return resourceUniqueIdentifier;
    }

    public void setResourceUniqueIdentifier(String resourceUniqueIdentifier) {
        this.resourceUniqueIdentifier = resourceUniqueIdentifier;
    }
}
