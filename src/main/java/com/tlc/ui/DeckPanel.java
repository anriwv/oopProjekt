package com.tlc.ui;

import com.tlc.model.TextTierItem;
import com.tlc.model.ImageTierItem;
import com.tlc.model.TierItem;
import com.tlc.service.TierListService;
import com.tlc.util.Localization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.UUID;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DeckPanel extends JPanel {
    private TierListService tierListService;
    private TierListPanel tierListPanel;
    private DefaultListModel<String> deckListModel;
    private JList<String> deckList;
    private JTextField newItemField;

    public DeckPanel(TierListService tierListService, TierListPanel tierListPanel) {
        this.tierListService = tierListService;
        this.tierListPanel = tierListPanel;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(Localization.get("deck")));

        deckListModel = new DefaultListModel<>();
        deckList = new JList<>(deckListModel);
        JScrollPane scrollPane = new JScrollPane(deckList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        newItemField = new JTextField(20);
        JButton addTextButton = new JButton(Localization.get("add.text.item.to.deck"));
        JButton addImageButton = new JButton(Localization.get("add.image.item.to.deck"));
        addPanel.add(new JLabel(Localization.get("new.item")));
        addPanel.add(newItemField);
        addPanel.add(addTextButton);
        addPanel.add(addImageButton);
        add(addPanel, BorderLayout.SOUTH);

        // Add text item
        addTextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = newItemField.getText().trim();
                if (!text.isEmpty()) {
                    deckListModel.addElement(text);
                    newItemField.setText("");
                    System.out.println("Deck: Added text '" + text + "' to deck list.");
                }
            }
        });

        // Add image item (TODO- salvestada resources kausta)
        addImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
                int result = fileChooser.showOpenDialog(DeckPanel.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    String imagePath = selectedFile.getAbsolutePath();
                    deckListModel.addElement("[Image: " + imagePath + "]");
                    System.out.println("Deck: Added image '" + imagePath + "' to deck list.");
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
                    JOptionPane.showMessageDialog(this, Localization.get("no.tiers.available"), Localization.get("error"), JOptionPane.ERROR_MESSAGE);
                    deckList.clearSelection();
                    return;
                }

                String chosenTier = (String) JOptionPane.showInputDialog(
                        this,
                        Localization.get("select.tier.to.add") + selectedText + Localization.get("to"),
                        Localization.get("add.item.to.tier"),
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        availableTiers,
                        availableTiers[0]
                );

                if (chosenTier != null) {
                    try {
                        TierItem item;
                        if (selectedText.startsWith("[Image: ") && selectedText.endsWith("]")) {
                            String imagePath = selectedText.substring(8, selectedText.length() - 1);
                            item = new ImageTierItem(UUID.randomUUID().toString(), imagePath);
                        } else {
                            item = new TextTierItem(UUID.randomUUID().toString(), selectedText);
                        }
                        tierListService.addItemToTier(chosenTier, item);

                        if (this.tierListPanel != null) {
                            this.tierListPanel.refresh();
                        } else {
                            JOptionPane.showMessageDialog(this, Localization.get("item.added.but.ui.ref.faild"), Localization.get("UI.ref.warning"), JOptionPane.WARNING_MESSAGE);
                        }

                        deckListModel.removeElement(selectedText);
                        System.out.println("Deck: Moved '" + selectedText + "' from deck to tier '" + chosenTier + "'.");
                        deckList.clearSelection();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, Localization.get("error.adding.item.to.tier") + ex.getMessage(), Localization.get("error"), JOptionPane.ERROR_MESSAGE);
                        System.err.println("Error adding item from deck to tier: " + ex.getMessage());
                        ex.printStackTrace();
                        deckList.clearSelection();
                    }
                } else {
                    System.out.println("Deck: Item addition cancelled for '" + selectedText + "'.");
                    deckList.clearSelection();
                }
            }
        });
    }

    public void setTierListPanel(TierListPanel tierListPanel) {
        this.tierListPanel = tierListPanel;
    }

    public void addItemToDeck(TierItem item) {
        if (item instanceof TextTierItem) {
            String text = ((TextTierItem) item).getText();
            if (!deckListModel.contains(text)) {
                deckListModel.addElement(text);
            } else {
            }
        } else if (item instanceof ImageTierItem) {
            String imagePath = ((ImageTierItem) item).getImagePath();
            String displayText = "[Image: " + imagePath + "]";
            if (!deckListModel.contains(displayText)) {
                deckListModel.addElement(displayText);
                System.out.println("Deck: Added image item '" + imagePath + "' back to deck from a tier.");
            } else {
                System.out.println("Deck: Image item '" + imagePath + "' is already in the deck, not adding again.");
            }
        } else {
            System.err.println("Deck: Cannot add unknown TierItem type back to deck. Item type: " + item.getClass().getSimpleName());
            JOptionPane.showMessageDialog(this, Localization.get("cant.move.item"), Localization.get("warning"), JOptionPane.WARNING_MESSAGE);
        }
    }
}