package com.tlc.ui;

import com.tlc.model.Tier;
import com.tlc.service.TierListService;

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

        // Edit Button
        JButton editButton = new JButton("✎");
        editButton.setToolTipText("Edit Tier Name");
        editButton.setMargin(new Insets(2, 2, 2, 2));
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String currentName = tier.getName();
                String newName = (String) JOptionPane.showInputDialog(
                        TierHeaderPanel.this,
                        "Enter new name for tier:",
                        "Edit Tier Name",
                        JOptionPane.PLAIN_MESSAGE,
                        null, // Icon
                        null, // Selection values (none)
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
                        JOptionPane.showMessageDialog(TierHeaderPanel.this, "Error renaming tier: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        System.err.println("Error renaming tier: " + ex.getMessage());
                    }
                } else if (newName != null && newName.trim().equals(currentName)) {
                    // No change
                } else {
                    System.out.println("Tier rename cancelled for '" + currentName + "'");
                }
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