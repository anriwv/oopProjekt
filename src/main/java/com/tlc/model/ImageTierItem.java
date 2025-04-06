package com.tlc.model;

public class ImageTierItem extends TierItem {
    private String imagePath;

    public ImageTierItem(String id, String imagePath) {
        super(id);
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Override
    public String getDisplayContent() {
        return "[Image: " + imagePath + "]";
    }
}
