package com.tlc.ui;

import com.tlc.model.TextTierItem;
import com.tlc.model.TierItem;
import com.tlc.service.TierListService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.UUID;

public class DeckPanel extends JPanel {
    private TierListService tierListService;
    private TierListPanel tierListPanel; // refresh jaoks
    private DefaultListModel<String> deckListModel;
    private JList<String> deckList;
    private JTextField newItemField;

    public DeckPanel(TierListService tierListService, TierListPanel tierListPanel) {
        this.tierListService = tierListService;
        this.tierListPanel = tierListPanel; // null ka
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Deck (Items to Place)"));

        deckListModel = new DefaultListModel<>();
        deckList = new JList<>(deckListModel);
        JScrollPane scrollPane = new JScrollPane(deckList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        newItemField = new JTextField(20);
        JButton addButton = new JButton("Add Text Item to Deck");
        addPanel.add(new JLabel("New Item:"));
        addPanel.add(newItemField);
        addPanel.add(addButton);
        add(addPanel, BorderLayout.SOUTH);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = newItemField.getText().trim();
                if (!text.isEmpty()) {
                    deckListModel.addElement(text);
                    newItemField.setText("");
                    System.out.println("Deck: Added '" + text + "' to deck list.");
                }
            }
        });

        deckList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && deckList.getSelectedIndex() != -1) {
                String selectedText = deckList.getSelectedValue();
                if (selectedText == null) return;

                String[] availableTiers = tierListService.getTierList().getTiers().stream()
                        .map(t -> t.getName()).toArray(String[]::new);

                if (availableTiers.length == 0) {
                    JOptionPane.showMessageDialog(this, "No tiers available to add items to.", "Error", JOptionPane.ERROR_MESSAGE);
                    deckList.clearSelection();
                    return;
                }

                String chosenTier = (String) JOptionPane.showInputDialog(
                        this,
                        "Select tier to add '" + selectedText + "' to:",
                        "Add Item to Tier",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        availableTiers,
                        availableTiers[0]
                );

                if (chosenTier != null) {
                    try {
                        TextTierItem item = new TextTierItem(UUID.randomUUID().toString(), selectedText);
                        tierListService.addItemToTier(chosenTier, item);


                        if (this.tierListPanel != null) {
                            this.tierListPanel.refresh();
                        } else {
                            JOptionPane.showMessageDialog(this,"Item added to model, but UI refresh failed.", "UI Refresh Warning", JOptionPane.WARNING_MESSAGE);
                        }

                        deckListModel.removeElement(selectedText);
                        System.out.println("Deck: Moved '" + selectedText + "' from deck to tier '" + chosenTier + "'.");
                        deckList.clearSelection();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Error adding item to tier: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        System.err.println("Error adding item from deck to tier: " + ex.getMessage());
                        ex.printStackTrace(); // log igaks juhuks

                        deckList.clearSelection();
                    }
                } else {
                    System.out.println("Deck: Item addition cancelled for '" + selectedText + "'.");
                    deckList.clearSelection();
                }
            }
        });
    }

    /**
     * Sets the reference to the TierListPanel.
     * This is necessary because DeckPanel is created before TierListPanel,
     * but needs to call refresh() on it later.
     *
     * @param tierListPanel The TierListPanel instance.
     */
    public void setTierListPanel(TierListPanel tierListPanel) {
        this.tierListPanel = tierListPanel;
        System.out.println("DeckPanel: TierListPanel reference set."); // Log confirmation
    }


    public void addItemToDeck(TierItem item) {
        if (item instanceof TextTierItem) {
            String text = ((TextTierItem) item).getText();
            if (!deckListModel.contains(text)) {
                deckListModel.addElement(text);
                System.out.println("Deck: Added item '" + text + "' back to deck from a tier.");
            } else {
                System.out.println("Deck: Item '" + text + "' is already in the deck, not adding again.");
            }
        } else {
            System.err.println("Deck: Cannot add non-TextTierItem back to deck yet. Item type: " + item.getClass().getSimpleName());
            JOptionPane.showMessageDialog(this, "Cannot move non-text items back to the deck currently.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
}