package com.tlc.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class Localization {
    private static Locale currentLocale = Locale.ENGLISH;
    private static ResourceBundle bundle = ResourceBundle.getBundle("com.tlc.ui.messages", currentLocale);

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle("com.tlc.ui.messages", currentLocale);
    }

    public static String get(String key) {
        return bundle.getString(key);
    }

    public static Locale getLocale() {
        return currentLocale;
    }
}
