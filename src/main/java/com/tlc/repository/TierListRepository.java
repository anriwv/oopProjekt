package com.tlc.repository;

import com.tlc.model.TierList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TierListRepository {
    private List<TierList> tierLists = new ArrayList<>();

    public void saveTierList(TierList tierList) {
        // unikaalsuse kontroll
        for (int i = 0; i < tierLists.size(); i++) {
            if (tierLists.get(i).getName().equalsIgnoreCase(tierList.getName())) {
                tierLists.set(i, tierList);
                return;
            }
        }

        tierLists.add(tierList);
    }

    public List<TierList> getAllTierLists() {
        return tierLists;
    }
}
