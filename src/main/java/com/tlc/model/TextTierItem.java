package com.tlc.model;

public class TextTierItem extends TierItem {
    private String text;

    public TextTierItem(String id, String text) {
        super(id);
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String getDisplayContent() {
        return text;
    }
}
