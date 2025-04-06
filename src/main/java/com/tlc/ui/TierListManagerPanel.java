package com.tlc.ui;

import com.tlc.model.Tier;
import com.tlc.model.TierList;
import com.tlc.repository.TierListRepository;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class TierListManagerPanel extends JPanel {
    private TierListRepository repository;
    private DefaultListModel<TierList> listModel;
    private JList<TierList> tierListDisplay;
    private JButton openButton;
    private JButton newButton;
    private JButton deleteButton;
    private MainFrame mainFrame;

    public TierListManagerPanel(TierListRepository repository, MainFrame mainFrame) {
        this.repository = repository;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        listModel = new DefaultListModel<>();
        tierListDisplay = new JList<>(listModel);
        tierListDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tierListDisplay);
        add(scrollPane, BorderLayout.CENTER);

        // Nuppude paneel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        openButton = new JButton("Open Selected");
        newButton = new JButton("New Tier List");
        deleteButton = new JButton("Delete Selected");
        buttonPanel.add(openButton);
        buttonPanel.add(newButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshList();

        // Open selected tl
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TierList selected = tierListDisplay.getSelectedValue();
                if (selected != null) {
                    mainFrame.openTierList(selected);
                } else {
                    JOptionPane.showMessageDialog(null, "No tier list selected.");
                }
            }
        });

        // uus tl
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog("Enter new tier list name:");
                if (name != null && !name.trim().isEmpty()) {

                    String numStr = JOptionPane.showInputDialog("Enter number of tiers (3-6):");
                    int numTiers = 0;
                    try {
                        numTiers = Integer.parseInt(numStr);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, "Invalid number, using 3.");
                        numTiers = 3;
                    }
                    if (numTiers < 3) numTiers = 3;
                    if (numTiers > 6) numTiers = 6;

                    String[] defaultNames = {"S", "A", "B", "C", "D", "F"};
                    String[] defaultColors = {"#FF0000", "#FFA500", "#FFD700", "#FFF44F", "#90EE90", "#808080"};

                    TierList newTierList = new TierList(name.trim());

                    for (int i = 0; i < numTiers; i++) {
                        newTierList.addTier(new Tier(defaultNames[i], defaultColors[i]));
                    }

                    // salvesta repository's
                    repository.saveTierList(newTierList);
                    refreshList();
                    System.out.println("Created new tier list: " + name + " with " + numTiers + " tiers.");
                }
            }
        });

        // kustuta tl
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TierList selected = tierListDisplay.getSelectedValue();
                if (selected != null) {
                    int confirm = JOptionPane.showConfirmDialog(
                            TierListManagerPanel.this,
                            "Are you sure you want to delete the entire TierList?",
                            "Confirm Deletion",
                            JOptionPane.YES_NO_OPTION
                    );


                    // kustuta repository's
                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            repository.deleteTierList(selected);
                            refreshList();
                            JOptionPane.showMessageDialog(TierListManagerPanel.this, "TierList " + selected + " deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(TierListManagerPanel.this, "Error deleting TierList: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "No tier list selected.");
                }
            }
        });


    }

    public void refreshList() {
        listModel.clear();
        List<TierList> lists = repository.getAllTierLists();

        for (TierList tl : lists) {
            listModel.addElement(tl);
        }
    }
}