package com.tlc.ui;

import com.tlc.model.TierList;
import com.tlc.repository.TierListRepository;
import com.tlc.service.TierListService;
import com.tlc.util.PersistenceUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainFrame extends JFrame {
    private TierListRepository repository;
    private TierListManagerPanel managerPanel;

    public MainFrame() {
        super("Tier List Creator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(900, 700);
        setLocationRelativeTo(null);

        System.out.println("Loading repository...");
        repository = PersistenceUtil.loadRepository();
        System.out.println("Repository loaded. Tier lists found: " + repository.getAllTierLists().size());

        managerPanel = new TierListManagerPanel(repository, this);
        add(managerPanel, BorderLayout.CENTER);
    }

    public void openTierList(TierList tierList) {
        System.out.println("Opening TierList: " + tierList.getName());

        getContentPane().removeAll();

        TierListService service = new TierListService(tierList);

        DeckPanel deckPanel = new DeckPanel(service, null);

        TierListPanel tierListPanel = new TierListPanel(service, deckPanel);


        deckPanel.setTierListPanel(tierListPanel);


        JPanel detailPanel = new JPanel(new BorderLayout(0, 10));
        detailPanel.add(tierListPanel, BorderLayout.CENTER);
        detailPanel.add(deckPanel, BorderLayout.SOUTH);
        deckPanel.setPreferredSize(new Dimension(0, 150));

        JButton backButton = new JButton("← Back & Save");
        backButton.setToolTipText("Return to the list manager and save changes to " + tierList.getName());
        backButton.addActionListener((ActionEvent e) -> {
            try {
                System.out.println("Saving TierList: " + tierList.getName() + " before going back.");
                tierList.updateModificationTime(); // Ensure modification time is current
                PersistenceUtil.saveRepository(repository);
                System.out.println("Repository saved successfully.");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error saving tier list repository: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
                System.err.println("Error saving repository: " + ex.getMessage());
            }
            getContentPane().removeAll();
            add(managerPanel, BorderLayout.CENTER);
            managerPanel.refreshList();
            System.out.println("Returned to Tier List Manager view.");
            revalidate();
            repaint();
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backButton);
        topPanel.add(new JLabel("Editing: " + tierList.getName()));

        add(topPanel, BorderLayout.NORTH);
        add(detailPanel, BorderLayout.CENTER);

        System.out.println("Displayed tier list view for: " + tierList.getName());
        revalidate();
        repaint();
    }
}