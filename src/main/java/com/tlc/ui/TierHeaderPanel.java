package com.tlc.ui;

import com.tlc.model.Tier;
import com.tlc.service.TierListService;
import com.tlc.util.Localization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TierHeaderPanel extends JPanel {
    private Tier tier;
    private TierListService tierListService;
    private TierListPanel parentPanel; // refresh jaoks
    private JLabel nameLabel;

    public TierHeaderPanel(Tier tier, TierListService tierListService, TierListPanel parentPanel) {
        this.tier = tier;
        this.tierListService = tierListService;
        this.parentPanel = parentPanel;

        setLayout(new BorderLayout(5, 0));
        setPreferredSize(new Dimension(100, 40));
        setOpaque(true);
        setBorder(BorderFactory.createEtchedBorder());

        // readde värvid
        try {
            setBackground(Color.decode(tier.getColor()));
        } catch (NumberFormatException e) {
            System.err.println("Invalid color format for tier '" + tier.getName() + "': " + tier.getColor() + ". Using gray.");
            setBackground(Color.GRAY);
        }

        nameLabel = new JLabel(tier.getName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));

        nameLabel.setForeground(isColorDark(getBackground()) ? Color.WHITE : Color.BLACK);
        add(nameLabel, BorderLayout.CENTER);

        // Edit Button - POP-UP menu
        JButton editButton = new JButton("✎");
        editButton.setToolTipText(Localization.get("edit.tier") + tier.getName());
        editButton.setMargin(new Insets(2, 2, 2, 2));

        JPopupMenu editMenu = new JPopupMenu(); // valikud: rename, change color, delete

        // Rename
        JMenuItem renameItem = new JMenuItem(Localization.get("rename.tier"));
        renameItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentName = tier.getName();
                String newName = (String) JOptionPane.showInputDialog(
                        TierHeaderPanel.this,
                        Localization.get("new.tier.name"),
                        Localization.get("edit.tier.name"),
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        currentName
                );

                if (newName != null && !newName.trim().isEmpty() && !newName.trim().equals(currentName)) {
                    try {
                        String updatedName = newName.trim();
                        System.out.println("Attempting to rename tier '" + currentName + "' to '" + updatedName + "'");
                        tierListService.renameTier(currentName, updatedName);
                        tier.setName(updatedName);
                        nameLabel.setText(updatedName);
                        parentPanel.refresh();
                        System.out.println("Tier '" + currentName + "' successfully renamed to '" + updatedName + "'");
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(TierHeaderPanel.this, Localization.get("err.renaming.tier") + ex.getMessage(), Localization.get("error"), JOptionPane.ERROR_MESSAGE);
                        System.err.println("Error renaming tier: " + ex.getMessage());
                    }
                }
            }
        });
        editMenu.add(renameItem);

        // Change color
        JMenuItem changeColorItem = new JMenuItem(Localization.get("change.color"));
        changeColorItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color initialColor;
                try {
                    initialColor = Color.decode(tier.getColor());
                } catch (NumberFormatException ex) {
                    initialColor = Color.GRAY;
                }

                Color newColor = JColorChooser.showDialog(
                        TierHeaderPanel.this,
                        Localization.get("choose.new.color"),
                        initialColor
                );

                if (newColor != null) {
                    String hexColor = String.format("#%02X%02X%02X", newColor.getRed(), newColor.getGreen(), newColor.getBlue());
                    try {
                        tier.setColor(hexColor);
                        tierListService.getTierList().updateModificationTime();
                        updateDisplay();
                        parentPanel.refresh();
                        System.out.println("Tier '" + tier.getName() + "' color changed to " + hexColor);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(TierHeaderPanel.this, Localization.get("err.changing.color") + ex.getMessage(), Localization.get("error"), JOptionPane.ERROR_MESSAGE);
                        System.err.println("Error changing tier color: " + ex.getMessage());
                    }
                }
            }
        });
        editMenu.add(changeColorItem);

        // Delete
        JMenuItem deleteItem = new JMenuItem(Localization.get("del.tier"));
        deleteItem.setForeground(Color.RED);
        deleteItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                        TierHeaderPanel.this,
                        Localization.get("want.del.tier") + tier.getName() + "'?",
                        Localization.get("confirm.deletion"),
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    try {
                        tierListService.deleteTier(tier);
                        parentPanel.refresh();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(TierHeaderPanel.this, Localization.get("err.deleting.tier") + ex.getMessage(), Localization.get("error"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        editMenu.add(deleteItem);

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editMenu.show(editButton, 0, editButton.getHeight());
            }
        });

        add(editButton, BorderLayout.EAST);
    }

    // hehe abimeetod, kas värt on liiga tume
    private boolean isColorDark(Color color) {
        double darkness = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return darkness >= 0.5;
    }

    public void updateDisplay() {
        nameLabel.setText(tier.getName());
        try {
            setBackground(Color.decode(tier.getColor()));
            nameLabel.setForeground(isColorDark(getBackground()) ? Color.WHITE : Color.BLACK);
        } catch (NumberFormatException e) {
            setBackground(Color.GRAY);
            nameLabel.setForeground(Color.WHITE);
        }
        revalidate();
        repaint();
    }
}