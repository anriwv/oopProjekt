package com.tlc.ui;

import com.tlc.model.Tier;
import com.tlc.service.TierListService;
import com.tlc.model.TierList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class TierListPanel extends JPanel {
    private TierListService tierListService;
    private DeckPanel deckPanel; // Reference to the DeckPanel
    private JPanel tiersContainerPanel;
    private JButton addTierButton;
    private static final int MAX_TIERS = 6;

    private static final String[] DEFAULT_NAMES = {"S", "A", "B", "C", "D", "F"};
    private static final String[] DEFAULT_COLORS = {"#FF0000", "#FFA500", "#FFD700", "#FFF44F", "#90EE90", "#808080"};

    public TierListPanel(TierListService tierListService, DeckPanel deckPanel) {
        this.tierListService = tierListService;
        this.deckPanel = deckPanel;
        setLayout(new BorderLayout());

        tiersContainerPanel = new JPanel();
        tiersContainerPanel.setLayout(new BoxLayout(tiersContainerPanel, BoxLayout.Y_AXIS));
        // tiersContainerPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(tiersContainerPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        // scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addTierButton = new JButton("+ Add Tier");
        addTierButton.setToolTipText("Add a new tier row (up to " + MAX_TIERS + " total)");
        controlPanel.add(addTierButton);
        add(controlPanel, BorderLayout.SOUTH);

        addTierButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TierList currentList = tierListService.getTierList();
                int currentTierCount = currentList.getTiers().size();

                if (currentTierCount >= MAX_TIERS) {
                    JOptionPane.showMessageDialog(TierListPanel.this,
                            "Maximum number of tiers (" + MAX_TIERS + ") reached.",
                            "Cannot Add Tier", JOptionPane.WARNING_MESSAGE);
                    System.out.println("Add Tier button clicked, but max tiers reached.");
                    return;
                }

                String nextName = DEFAULT_NAMES[currentTierCount];
                String nextColor = DEFAULT_COLORS[currentTierCount];
                String uniqueName = nextName;
                int suffix = 1;
                while (tierListService.getTierByName(uniqueName) != null) {
                    uniqueName = nextName + "_" + suffix++;
                }
                if (!uniqueName.equals(nextName)) {
                    System.out.println("Default name '" + nextName + "' already exists, using '" + uniqueName + "'");
                }

                Tier newTier = new Tier(uniqueName, nextColor);
                try {
                    System.out.println("Adding new tier: Name=" + newTier.getName() + ", Color=" + newTier.getColor());
                    tierListService.addTier(newTier);
                    refresh();
                    System.out.println("Tier '" + newTier.getName() + "' added successfully.");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(TierListPanel.this, "Error adding tier: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    System.err.println("Error adding tier: " + ex.getMessage());
                }
            }
        });

        refresh();
    }


    public void refresh() {
        System.out.println("Refreshing TierListPanel...");
        tiersContainerPanel.removeAll();

        List<Tier> tiers = tierListService.getTierList().getTiers();
        for (Tier tier : tiers) {

            TierPanel tierPanel = new TierPanel(tier, tierListService, this, deckPanel);
            tiersContainerPanel.add(tierPanel);
            tiersContainerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        addTierButton.setEnabled(tiers.size() < MAX_TIERS);

        tiersContainerPanel.revalidate();
        tiersContainerPanel.repaint();
        revalidate();
        repaint();
        System.out.println("TierListPanel refresh complete. Tiers displayed: " + tiers.size());
    }
}