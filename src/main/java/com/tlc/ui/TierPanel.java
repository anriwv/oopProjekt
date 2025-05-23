package com.tlc.ui;

import com.tlc.model.Tier;
import com.tlc.model.TierItem;
import com.tlc.model.TextTierItem;
import com.tlc.model.ImageTierItem;
import com.tlc.repository.ImageStorage;
import com.tlc.service.TierListService;
import com.tlc.util.Localization;

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
        this.deckPanel = deckPanel;
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        headerPanel = new TierHeaderPanel(tier, tierListService, parentPanel);
        add(headerPanel, BorderLayout.WEST);

        itemsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JScrollPane itemsScrollPane = new JScrollPane(itemsPanel);
        itemsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        itemsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        itemsScrollPane.setBorder(null);
        add(itemsScrollPane, BorderLayout.CENTER);

        refreshItems();
    }

    public void refreshItems() {
        itemsPanel.removeAll();

        for (int i = 0; i < tier.getItems().size(); i++) {
            TierItem item = tier.getItems().get(i);
            JButton itemButton;

            if (item instanceof ImageTierItem) {
                ImageTierItem imageItem = (ImageTierItem) item;
                ImageIcon icon = imageItem.getImageIcon();
                if (icon != null) {
                    try {
                        Image scaledImg = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                        itemButton = new JButton(new ImageIcon(scaledImg));
                        itemButton.setText("Image: " + item.getId());
                        itemButton.setVerticalTextPosition(SwingConstants.BOTTOM);
                        itemButton.setHorizontalTextPosition(SwingConstants.CENTER);
                    } catch (Exception e) {
                        itemButton = new JButton(Localization.get("image.error") + item.getId());
                        System.err.println("Failed to load image for item " + item.getId() + ": " + e.getMessage());
                    }
                } else {
                    itemButton = new JButton(Localization.get("image.error") + item.getId());
                }
            } else {
                itemButton = new JButton(item.getDisplayContent());
            }

            itemButton.setToolTipText(Localization.get("actions.for") + item.getDisplayContent());
            itemButton.setMargin(new Insets(2, 5, 2, 5));
            final int currentIndex = i;
            final TierItem currentItem = item;

            itemButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String[] options = {
                            Localization.get("move.l"),
                            Localization.get("move.r"),
                            Localization.get("move.to.tier"),
                            Localization.get("move.back.to.deck"),
                            Localization.get("remove"),
                            Localization.get("cancel")
                    };
                    int choice = JOptionPane.showOptionDialog(
                            TierPanel.this,
                            Localization.get("choose.action.for.item") + currentItem.getDisplayContent() + "'",
                            Localization.get("item.options"),
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
                                    JOptionPane.showMessageDialog(TierPanel.this, Localization.get("item.left"), Localization.get("move.l"), JOptionPane.INFORMATION_MESSAGE);
                                }
                                break;
                            case 1: // Move Right
                                if (currentIndex < tier.getItems().size() - 1) {
                                    System.out.println("UI: Moving item right in tier '" + tier.getName() + "' from index " + currentIndex);
                                    tierListService.moveItemWithinTier(tier.getName(), currentIndex, currentIndex + 1);
                                    parentPanel.refresh();
                                } else {
                                    JOptionPane.showMessageDialog(TierPanel.this, Localization.get("item.right"), Localization.get("move.r"), JOptionPane.INFORMATION_MESSAGE);
                                }
                                break;
                            case 2: // Move to Tier...
                                List<String> otherTierNames = tierListService.getTierList().getTiers().stream()
                                        .map(Tier::getName)
                                        .filter(name -> !name.equalsIgnoreCase(tier.getName()))
                                        .collect(Collectors.toList());

                                if (otherTierNames.isEmpty()) {
                                    JOptionPane.showMessageDialog(TierPanel.this, Localization.get("no.others.tiers.available.to.move.item"), Localization.get("move.to.tier"), JOptionPane.INFORMATION_MESSAGE);
                                    break;
                                }

                                String targetTier = (String) JOptionPane.showInputDialog(
                                        TierPanel.this,
                                        Localization.get("select.target.tier") + currentItem.getDisplayContent() + "':",
                                        Localization.get("move.item.to.tier"),
                                        JOptionPane.QUESTION_MESSAGE,
                                        null,
                                        otherTierNames.toArray(new String[0]),
                                        otherTierNames.get(0)
                                );

                                if (targetTier != null) {
                                    tierListService.moveItemToTier(tier.getName(), currentIndex, targetTier);
                                    parentPanel.refresh();
                                }
                                break;
                            case 3: // Move back to Deck
                                TierItem removedItem = tier.getItems().remove(currentIndex);
                                tierListService.getTierList().updateModificationTime();
                                deckPanel.addItemToDeck(removedItem);
                                parentPanel.refresh();
                                break;
                            case 4: // Remove Item
                                tier.removeItem(currentItem);
                                if (currentItem instanceof ImageTierItem) {
                                    ImageStorage.removeImage(currentItem.getId()); // Remove image data
                                }
                                tierListService.getTierList().updateModificationTime();
                                parentPanel.refresh();
                                break;
                            case 5: // Cancel
                            default:
                                break;
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(TierPanel.this, Localization.get("error.performing.action") + ex.getMessage(), Localization.get("error"), JOptionPane.ERROR_MESSAGE);
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