package com.tlc.model;

import java.io.ObjectStreamException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TierList {
    private String name;
    private LocalDateTime creationTime;
    private LocalDateTime modificationTime;
    private List<Tier> tiers;

    public TierList(String name) {
        this.name = name;
        this.creationTime = LocalDateTime.now();
        this.modificationTime = LocalDateTime.now();
        this.tiers = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public LocalDateTime getModificationTime() {
        return modificationTime;
    }

    public List<Tier> getTiers() {
        return tiers;
    }

    public void addTier(Tier tier) {
        tiers.add(tier);
        updateModificationTime();
    }

    public void removeTier(Tier tier) {
        tiers.remove(tier);
        updateModificationTime();
    }

    public void updateModificationTime() {
        modificationTime = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return name;
    }

    private Object readResolve() throws ObjectStreamException {
        if (tiers == null) tiers = new ArrayList<>();
        return this;
    }
}
