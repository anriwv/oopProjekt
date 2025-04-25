package com.tlc.ui;

import com.tlc.model.Tier;
import com.tlc.model.TierList;
import com.tlc.repository.TierListRepository;
import com.tlc.util.Localization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;

public class TierListManagerPanel extends JPanel {
    private TierListRepository repository;
    private DefaultListModel<TierList> listModel;
    private JList<TierList> tierListDisplay;
    private JButton openButton;
    private JButton newButton;
    private JButton deleteButton;
    private MainFrame mainFrame;
    private LanguageSelector languageSelector;

    public TierListManagerPanel(TierListRepository repository, MainFrame mainFrame) {
        this.repository = repository;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        languageSelector = new LanguageSelector();
        languageSelector.setSelectedLanguage(Localization.getLocale().getLanguage().equals("et") ? "Estonian" : "English");

        languageSelector.addLanguageChangeListener(e -> {
            updateLanguage(languageSelector.getSelectedLanguage());
            updateUIText();

        });
        add(languageSelector, BorderLayout.NORTH);

        // Tier list
        listModel = new DefaultListModel<>();
        tierListDisplay = new JList<>(listModel);
        tierListDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tierListDisplay);
        add(scrollPane, BorderLayout.CENTER);

        // Nuppude paneel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        openButton = new JButton(Localization.get("open.selected"));
        newButton = new JButton(Localization.get("new.list"));
        deleteButton = new JButton(Localization.get(("delete.selected")));
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
                    JOptionPane.showMessageDialog(null, Localization.get("no.tier.list.selected"));
                }
            }
        });

        // uus tl
        newButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog(Localization.get("new.tier.list.name"));
                if (name != null && !name.trim().isEmpty()) {

                    String numStr = JOptionPane.showInputDialog(Localization.get("enter.number.of.tiers"));
                    int numTiers = 0;
                    try {
                        numTiers = Integer.parseInt(numStr);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(null, Localization.get("invalid.number.using.3"));
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
                    System.out.println(Localization.get("created.new.tier.list") + name + Localization.get("with") + numTiers + Localization.get("tiers"));
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
                            Localization.get("delete.entire.TierList"),
                            Localization.get("confirm.deletion"),
                            JOptionPane.YES_NO_OPTION
                    );


                    // kustuta repository's
                    if (confirm == JOptionPane.YES_OPTION) {
                        try {
                            repository.deleteTierList(selected);
                            refreshList();
                            JOptionPane.showMessageDialog(TierListManagerPanel.this, "TierList " + selected + Localization.get("deleted.successfully"), Localization.get("success"), JOptionPane.INFORMATION_MESSAGE);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(TierListManagerPanel.this, Localization.get("error.deleting.TierList") + ex.getMessage(), Localization.get("error"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, Localization.get("no.tier.list.selected"));
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

    public void updateUIText() {

        mainFrame.setTitle(Localization.get("app.title"));
        openButton.setText(Localization.get("open.selected"));
        newButton.setText(Localization.get("new.list"));
        deleteButton.setText(Localization.get("delete.selected"));
        languageSelector.setLabelText(Localization.get("language"));
        revalidate();
        repaint();
    }

    private void updateLanguage(String selectedLanguage) {
        switch (selectedLanguage) {
            case "Estonian":
                Localization.setLocale(new Locale("et", "EE"));
                break;
            case "English":
            default:
                Localization.setLocale(Locale.ENGLISH);
                break;
        }
    }
}