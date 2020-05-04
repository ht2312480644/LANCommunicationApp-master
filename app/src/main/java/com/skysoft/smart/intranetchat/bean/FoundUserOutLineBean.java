package com.skysoft.smart.intranetchat.bean;

public class FoundUserOutLineBean {
    private String identifier;
    private int mark;

    public FoundUserOutLineBean(String identifier, int mark) {
        this.identifier = identifier;
        this.mark = mark;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }
}
