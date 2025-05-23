package com.tlc.ui;

import com.tlc.model.TextTierItem;
import com.tlc.model.ImageTierItem;
import com.tlc.model.Tier;
import com.tlc.model.TierItem;
import com.tlc.service.TierListService;
import com.tlc.repository.ImageStorage;
import com.tlc.util.Localization;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.UUID;
import javax.swing.filechooser.FileNameExtensionFilter;

public class DeckPanel extends JPanel {
    private TierListService tierListService;
    private TierListPanel tierListPanel;
    private DefaultListModel<TierItem> deckListModel;
    private JList<TierItem> deckList;
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

        addTextButton.addActionListener(e -> {
            String text = newItemField.getText().trim();
            if (!text.isEmpty()) {
                TextTierItem textItem = new TextTierItem(UUID.randomUUID().toString(), text);
                deckListModel.addElement(textItem);
                newItemField.setText("");
                System.out.println("Deck: Added text item '" + text + "' to deck list.");
            }
        });

        addImageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
            int result = fileChooser.showOpenDialog(DeckPanel.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try {
                    byte[] imageData = Files.readAllBytes(selectedFile.toPath());
                    String itemId = UUID.randomUUID().toString();
                    ImageStorage.saveImage(itemId, imageData);
                    ImageTierItem imageItem = new ImageTierItem(itemId);
                    deckListModel.addElement(imageItem);
                    System.out.println("Deck: Added image item to deck list, saved to images.dat.");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(DeckPanel.this,
                            Localization.get("error.reading.image") + ex.getMessage(),
                            Localization.get("error"),
                            JOptionPane.ERROR_MESSAGE);
                    System.err.println("Failed to read image file: " + ex.getMessage());
                }
            }
        });

        deckList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && deckList.getSelectedIndex() != -1) {
                TierItem selectedItem = deckList.getSelectedValue();
                if (selectedItem == null) return;

                String[] availableTiers = tierListService.getTierList().getTiers().stream()
                        .map(Tier::getName).toArray(String[]::new);

                if (availableTiers.length == 0) {
                    JOptionPane.showMessageDialog(this, Localization.get("no.tiers.available"), Localization.get("error"), JOptionPane.ERROR_MESSAGE);
                    deckList.clearSelection();
                    return;
                }

                String chosenTier = (String) JOptionPane.showInputDialog(
                        this,
                        Localization.get("select.tier.to.add") + selectedItem.getDisplayContent() + Localization.get("to"),
                        Localization.get("add.item.to.tier"),
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        availableTiers,
                        availableTiers[0]
                );

                if (chosenTier != null) {
                    try {
                        tierListService.addItemToTier(chosenTier, selectedItem);
                        if (this.tierListPanel != null) {
                            this.tierListPanel.refresh();
                        }
                        deckListModel.removeElement(selectedItem);
                        System.out.println("Deck: Moved item '" + selectedItem.getDisplayContent() + "' to tier '" + chosenTier + "'.");
                        deckList.clearSelection();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, Localization.get("error.adding.item.to.tier") + ex.getMessage(), Localization.get("error"), JOptionPane.ERROR_MESSAGE);
                        System.err.println("Error adding item to tier: " + ex.getMessage());
                    }
                }
                deckList.clearSelection();
            }
        });
    }

    public void setTierListPanel(TierListPanel tierListPanel) {
        this.tierListPanel = tierListPanel;
    }

    public void addItemToDeck(TierItem item) {
        if (item instanceof TextTierItem) {
            TextTierItem textItem = (TextTierItem) item;
            if (!deckListModel.contains(textItem)) {
                deckListModel.addElement(textItem);
            }
        } else if (item instanceof ImageTierItem) {
            ImageTierItem imageItem = (ImageTierItem) item;
            if (!deckListModel.contains(imageItem)) {
                deckListModel.addElement(imageItem);
                System.out.println("Deck: Added image item back to deck.");
            }
        } else {
            System.err.println("Deck: Cannot add unknown TierItem type back to deck. Item type: " + item.getClass().getSimpleName());
            JOptionPane.showMessageDialog(this, Localization.get("cant.move.item"), Localization.get("warning"), JOptionPane.WARNING_MESSAGE);
        }
    }

}