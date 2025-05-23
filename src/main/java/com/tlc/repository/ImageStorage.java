package com.tlc.repository;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ImageStorage {
    private static final File STORAGE_DIR = new File(".");
    private static final File STORAGE_FILE = new File(STORAGE_DIR, "images.dat");

    public static Map<String, byte[]> loadImages() {
        if (!STORAGE_FILE.exists()) {
            return new HashMap<>();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(STORAGE_FILE))) {
            return (Map<String, byte[]>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load images from " + STORAGE_FILE.getAbsolutePath() + ": " + e.getMessage());
            return new HashMap<>();
        }
    }

    public static void saveImage(String itemId, byte[] imageData) {
        STORAGE_DIR.mkdirs();
        Map<String, byte[]> images = loadImages();
        images.put(itemId, imageData);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STORAGE_FILE))) {
            oos.writeObject(images);
            System.out.println("Saved image data for ID '" + itemId + "' to " + STORAGE_FILE.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to save image for ID '" + itemId + "' to " + STORAGE_FILE.getAbsolutePath() + ": " + e.getMessage());
        }
    }

    public static byte[] getImageData(String itemId) {
        return loadImages().get(itemId);
    }

    public static void removeImage(String itemId) {
        Map<String, byte[]> images = loadImages();

        if (images.remove(itemId) != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STORAGE_FILE))) {
                oos.writeObject(images);
                System.out.println("Removed image data for ID '" + itemId + "' from " + STORAGE_FILE.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Failed to remove image for ID '" + itemId + "' from " + STORAGE_FILE.getAbsolutePath() + ": " + e.getMessage());
            }
        }
    }

}