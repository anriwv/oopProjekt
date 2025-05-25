package com.tlc.ui;

import com.tlc.model.TierList;
import com.tlc.repository.TierListRepository;
import com.tlc.service.TierListService;
import com.tlc.util.Localization;
import com.tlc.util.PersistenceUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;

public class MainFrame extends JFrame {
    private TierListRepository repository;
    private TierListManagerPanel managerPanel;
    private LanguageSelector languageSelector;
    private JButton backButton;
    private JLabel editingLabel;
    private TierList currentTierList;
    private DeckPanel currentDeckPanel;

    public MainFrame() {
        setTitle(Localization.get("app.title"));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(900, 700);
        setLocationRelativeTo(null);

        System.out.println("Loading repository...");
        repository = PersistenceUtil.loadRepository();
        System.out.println("Repository loaded. Tier lists found: " + repository.getAllTierLists().size());

        managerPanel = new TierListManagerPanel(repository, this);
        add(managerPanel, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveRepository();
            }
        });
    }

    public void saveRepository() {
        try {
            for (TierList tierList : repository.getAllTierLists()) {
                tierList.updateModificationTime();
            }

            PersistenceUtil.saveRepository(repository);
            System.out.println("Repository saved successfully.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    Localization.get("failed.to.save.t.l.repo") + ex.getMessage(),
                    Localization.get("save.error"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void openTierList(TierList tierList) {
        this.currentTierList = tierList;
        System.out.println("Opening TierList: " + tierList.getName());
        getContentPane().removeAll();

        TierListService service = new TierListService(tierList);
        currentDeckPanel = new DeckPanel(service, null);
        TierListPanel tierListPanel = new TierListPanel(service, currentDeckPanel);
        currentDeckPanel.setTierListPanel(tierListPanel);

        managerPanel.setDeckPanel(currentDeckPanel);

        JPanel detailPanel = new JPanel(new BorderLayout(0, 10));
        detailPanel.add(tierListPanel, BorderLayout.CENTER);
        detailPanel.add(currentDeckPanel, BorderLayout.SOUTH);
        currentDeckPanel.setPreferredSize(new Dimension(0, 150));

        backButton = new JButton();
        editingLabel = new JLabel();
        updateUIText();

        backButton.addActionListener((ActionEvent e) -> {
            saveRepository();
            getContentPane().removeAll();
            add(managerPanel, BorderLayout.CENTER);
            managerPanel.refreshList();
            System.out.println("Returned to Tier List Manager view.");
            revalidate();
            repaint();
        });

        languageSelector = new LanguageSelector();
        languageSelector.setSelectedLanguage(Localization.getLocale().getLanguage().equals("et") ? "Estonian" : "English");
        languageSelector.addLanguageChangeListener(e -> {
            updateLanguage(languageSelector.getSelectedLanguage());
            updateUIText();
            managerPanel.updateUIText();
            if (currentDeckPanel != null) {
                currentDeckPanel.updateUIText();
            }
            if (tierListPanel != null) {
                tierListPanel.refresh();
            }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backButton);
        topPanel.add(editingLabel);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(languageSelector);

        add(topPanel, BorderLayout.NORTH);
        add(detailPanel, BorderLayout.CENTER);

        System.out.println("Displayed tier list view for: " + tierList.getName());
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

    private void updateUIText() {
        setTitle(Localization.get("app.title"));
        if (backButton != null) {
            backButton.setText(Localization.get("button.back.save"));
            backButton.setToolTipText(String.format(Localization.get("back.save.tooltip"),
                    currentTierList != null ? currentTierList.getName() : ""));
        }
        if (editingLabel != null && currentTierList != null) {
            editingLabel.setText(String.format(Localization.get("label.editing"), currentTierList.getName()));
        }
        revalidate();
        repaint();
    }
}