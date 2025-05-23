package com.tlc.model;

import com.tlc.repository.ImageStorage;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageTierItem extends TierItem {
    public ImageTierItem(String id) {
        super(id);
    }

    @Override
    public String getDisplayContent() {
        return "[Image]";
    }

    public ImageIcon getImageIcon() {
        byte[] imageData = ImageStorage.getImageData(getId());
        if (imageData == null) {
            System.err.println("No image data found for ID: " + getId());
            return null;
        }
        try {
            ByteArrayInputStream baits = new ByteArrayInputStream(imageData);
            BufferedImage img = ImageIO.read(baits);
            return new ImageIcon(img);
        } catch (IOException e) {
            System.err.println("Failed to create ImageIcon for ID " + getId() + ": " + e.getMessage());
            return null;
        }
    }
}