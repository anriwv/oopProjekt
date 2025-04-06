package com.tlc.ui;

import com.tlc.model.Tier;
import com.tlc.model.TierItem;
import com.tlc.service.TierListService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;


public class TierPanel extends JPanel {
    private Tier tier;
    private TierListService tierListService;
    private TierListPanel parentPanel;
    private DeckPanel deckPanel;
    private JPanel itemsPanel;
    private TierHeaderPanel headerPanel;

    public TierPanel(Tier tier, TierListService tierListService, TierListPanel parentPanel, DeckPanel deckPanel) {
        this.tier = tier;
        this.tierListService = tierListService;
        this.parentPanel = parentPanel;
        this.deckPanel = deckPanel; // Store the reference
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        headerPanel = new TierHeaderPanel(tier, tierListService, parentPanel);
        add(headerPanel, BorderLayout.WEST);

        itemsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        // itemsPanel.setBackground(Color.WHITE);
        JScrollPane itemsScrollPane = new JScrollPane(itemsPanel);
        itemsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        itemsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        itemsScrollPane.setBorder(null);
        // itemsScrollPane.getViewport().setBackground(Color.WHITE);

        add(itemsScrollPane, BorderLayout.CENTER);

        refreshItems();
    }

    public void refreshItems() {
        itemsPanel.removeAll();

        for (int i = 0; i < tier.getItems().size(); i++) {
            TierItem item = tier.getItems().get(i);
            JButton itemButton = new JButton(item.getDisplayContent());
            itemButton.setToolTipText("Actions for: " + item.getDisplayContent());
            itemButton.setMargin(new Insets(2, 5, 2, 5));
            final int currentIndex = i;
            final TierItem currentItem = item;

            itemButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {

                    String[] options = {
                            "Move Left",
                            "Move Right",
                            "Move to Tier...",
                            "Move back to Deck",
                            "Remove",
                            "Cancel"
                    };
                    int choice = JOptionPane.showOptionDialog(
                            TierPanel.this,
                            "Choose action for item: '" + currentItem.getDisplayContent() + "'",
                            "Item Options",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            options,
                            options[options.length - 1]
                    );

                    try {
                        switch (choice) {
                            case 0: // Move Left
                                if (currentIndex > 0) {
                                    System.out.println("UI: Moving item left in tier '" + tier.getName() + "' from index " + currentIndex);
                                    tierListService.moveItemWithinTier(tier.getName(), currentIndex, currentIndex - 1);
                                    parentPanel.refresh();
                                } else {
                                    JOptionPane.showMessageDialog(TierPanel.this, "Item is already at the leftmost position.", "Move Left", JOptionPane.INFORMATION_MESSAGE);
                                }
                                break;
                            case 1: // Move Right
                                if (currentIndex < tier.getItems().size() - 1) {
                                    System.out.println("UI: Moving item right in tier '" + tier.getName() + "' from index " + currentIndex);
                                    tierListService.moveItemWithinTier(tier.getName(), currentIndex, currentIndex + 1);
                                    parentPanel.refresh();
                                } else {
                                    JOptionPane.showMessageDialog(TierPanel.this, "Item is already at the rightmost position.", "Move Right", JOptionPane.INFORMATION_MESSAGE);
                                }
                                break;
                            case 2: // Move to Tier...
                                List<String> otherTierNames = tierListService.getTierList().getTiers().stream()
                                        .map(Tier::getName)
                                        .filter(name -> !name.equalsIgnoreCase(tier.getName())) //  - hetketier
                                        .collect(Collectors.toList());

                                if (otherTierNames.isEmpty()) {
                                    JOptionPane.showMessageDialog(TierPanel.this, "No other tiers available to move the item to.", "Move to Tier", JOptionPane.INFORMATION_MESSAGE);
                                    System.out.println("UI: 'Move to Tier' selected for item '" + currentItem.getDisplayContent() + "' but no other tiers exist.");
                                    break;
                                }

                                String targetTier = (String) JOptionPane.showInputDialog(
                                        TierPanel.this,
                                        "Select target tier for '" + currentItem.getDisplayContent() + "':",
                                        "Move Item to Tier",
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,
                                        otherTierNames.toArray(new String[0]),
                                        otherTierNames.get(0) // default
                                );

                                if (targetTier != null) {
                                    System.out.println("UI: Attempting move item '" + currentItem.getDisplayContent() + "' from '" + tier.getName() + "' to '" + targetTier + "'");
                                    tierListService.moveItemToTier(tier.getName(), currentIndex, targetTier);
                                    parentPanel.refresh();
                                } else {
                                    System.out.println("UI: 'Move to Tier' cancelled for item '" + currentItem.getDisplayContent() + "'");
                                }
                                break;
                            case 3: // Move back to Deck
                                System.out.println("UI: Attempting move item '" + currentItem.getDisplayContent() + "' from tier '" + tier.getName() + "' back to deck.");

                                TierItem removedItem = tier.getItems().remove(currentIndex);

                                tierListService.getTierList().updateModificationTime();

                                deckPanel.addItemToDeck(removedItem);

                                parentPanel.refresh();
                                System.out.println("UI: Item '" + removedItem.getDisplayContent() + "' moved back to deck.");
                                break;
                            case 4: // Remove Item
                                System.out.println("UI: Removing item '" + currentItem.getDisplayContent() + "' permanently from tier '" + tier.getName() + "'");
                                tier.removeItem(currentItem);
                                tierListService.getTierList().updateModificationTime();
                                parentPanel.refresh();
                                break;
                            case 5: // Cancel or closed dialog
                            default:
                                System.out.println("UI: Item action cancelled for '" + currentItem.getDisplayContent() + "'");
                                break;
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(TierPanel.this, "Error performing action: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        System.err.println("Error performing item action on '" + currentItem.getDisplayContent() + "': " + ex.getMessage());
                        ex.printStackTrace();

                        parentPanel.refresh();
                    }
                }
            });

            itemsPanel.add(itemButton);
        }

        itemsPanel.revalidate();
        itemsPanel.repaint();
        revalidate();
        repaint();
    }

    public void refresh() {
        headerPanel.updateDisplay();
        refreshItems();
    }
}