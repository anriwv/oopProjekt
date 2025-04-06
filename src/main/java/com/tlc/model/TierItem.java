package com.tlc.model;

public abstract class TierItem {
    private String id;

    public TierItem(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract String getDisplayContent();
}
