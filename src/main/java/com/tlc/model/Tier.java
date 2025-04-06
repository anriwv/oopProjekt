package com.tlc.model;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.List;

public class Tier {
    private String name;
    private String color;
    private List<TierItem> items;

    public Tier(String name, String color) {
        this.name = name;
        this.color = color;
        this.items = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<TierItem> getItems() {
        return items;
    }

    public void addItem(TierItem item) {
        items.add(item);
    }

    public void removeItem(TierItem item) {
        items.remove(item);
    }

    public void moveItem(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= items.size() || toIndex < 0 || toIndex > items.size()) {
            throw new IndexOutOfBoundsException("Invalid index for moving item");
        }
        TierItem item = items.remove(fromIndex);
        items.add(toIndex, item);
    }

    private Object readResolve() throws ObjectStreamException {
        if (items == null) items = new ArrayList<>();
        return this;
    }
}
