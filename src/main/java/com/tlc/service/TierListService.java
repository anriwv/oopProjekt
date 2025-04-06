package com.tlc.service;

import com.tlc.model.Tier;
import com.tlc.model.TierItem;
import com.tlc.model.TierList;

public class TierListService {
    private TierList tierList;

    public TierListService(TierList tierList) {
        this.tierList = tierList;
    }

    public TierList getTierList() {
        return tierList;
    }

    public void addItemToTier(String tierName, TierItem item) throws Exception {
        Tier tier = getTierByName(tierName);
        if (tier == null) {
            throw new Exception("Tier '" + tierName + "' not found.");
        }
        tier.addItem(item);
        tierList.updateModificationTime();
        System.out.println("Service: Item '" + item.getDisplayContent() + "' added to tier '" + tierName + "'");
    }

    public Tier getTierByName(String tierName) {
        if (tierName == null) return null;
        for (Tier t : tierList.getTiers()) {
            if (t.getName().equalsIgnoreCase(tierName.trim())) {
                return t;
            }
        }
        return null;
    }

    public void moveItemWithinTier(String tierName, int fromIndex, int toIndex) throws Exception {
        Tier tier = getTierByName(tierName);
        if (tier == null) {
            throw new Exception("Tier '" + tierName + "' not found for moving item.");
        }
        tier.moveItem(fromIndex, toIndex);
        tierList.updateModificationTime();
        System.out.println("Service: Item moved within tier '" + tierName + "' from index " + fromIndex + " to " + toIndex);
    }

    public void renameTier(String oldName, String newName) throws Exception {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("New tier name cannot be empty.");
        }
        String trimmedNewName = newName.trim();
        Tier existingWithNewName = getTierByName(trimmedNewName);
        if (existingWithNewName != null && !existingWithNewName.getName().equalsIgnoreCase(oldName)) {
            throw new IllegalArgumentException("Another tier already exists with the name '" + trimmedNewName + "'.");
        }
        Tier tierToRename = getTierByName(oldName);
        if (tierToRename == null) {
            throw new Exception("Tier with name '" + oldName + "' not found for renaming.");
        }
        tierToRename.setName(trimmedNewName);
        tierList.updateModificationTime();
        System.out.println("Service: Renamed tier '" + oldName + "' to '" + trimmedNewName + "'");
    }

    public void addTier(Tier tier) throws Exception {
        if (tier == null) {
            throw new IllegalArgumentException("Cannot add a null tier.");
        }
        if (tierList.getTiers().size() >= 6) {
            throw new IllegalStateException("Maximum number of tiers (6) already reached.");
        }
        if (getTierByName(tier.getName()) != null) {
            throw new IllegalArgumentException("A tier with the name '" + tier.getName() + "' already exists.");
        }
        tierList.addTier(tier);
        System.out.println("Service: Added new tier '" + tier.getName() + "' with color " + tier.getColor());
    }

    public void deleteTier(Tier tier) {
        if (tier == null) return;
        tierList.removeTier(tier);
        System.out.println("Service: Deleted tier '" + tier.getName() + "' with color " + tier.getColor());
    }


    public void moveItemToTier(String sourceTierName, int itemIndex, String targetTierName) throws Exception {
        if (sourceTierName == null || targetTierName == null || sourceTierName.trim().equalsIgnoreCase(targetTierName.trim())) {
            throw new IllegalArgumentException("Source and target tier names must be different and not null.");
        }

        Tier sourceTier = getTierByName(sourceTierName);
        Tier targetTier = getTierByName(targetTierName);

        if (sourceTier == null) {
            throw new Exception("Source tier '" + sourceTierName + "' not found.");
        }
        if (targetTier == null) {
            throw new Exception("Target tier '" + targetTierName + "' not found.");
        }

        if (itemIndex < 0 || itemIndex >= sourceTier.getItems().size()) {
            throw new IndexOutOfBoundsException("Invalid item index (" + itemIndex + ") for source tier '" + sourceTierName + "'.");
        }

        TierItem itemToMove = sourceTier.getItems().remove(itemIndex);
        targetTier.addItem(itemToMove);

        tierList.updateModificationTime();
        System.out.println("Service: Moved item '" + itemToMove.getDisplayContent() + "' from tier '" + sourceTierName + "' to tier '" + targetTierName + "'.");
    }
}