package com.tlc.ui;

import com.tlc.util.Localization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Locale;

public class LanguageSelector extends JPanel {
    private JLabel label;
    private JComboBox<String> languageComboBox;

    public LanguageSelector() {
        setLayout(new FlowLayout(FlowLayout.RIGHT));

        label = new JLabel(Localization.get("language"));
        String[] languages = {"English", "Estonian"};
        languageComboBox = new JComboBox<>(languages);

        add(label);
        add(languageComboBox);
    }

    public void addLanguageChangeListener(ActionListener listener) {
        languageComboBox.addActionListener(listener);
    }

    public String getSelectedLanguage() {
        return (String) languageComboBox.getSelectedItem();
    }

    public void setSelectedLanguage(String language) {
        languageComboBox.setSelectedItem(language);
    }

    public void setLabelText(String text) {
        label.setText(text);
    }
}