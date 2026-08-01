package com.vaadinbaseapp.appvaadin.i18n;

import com.vaadin.flow.i18n.I18NProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Component
public class SimpleI18NProvider implements I18NProvider {

    public static final Locale ENGLISH = Locale.ENGLISH;
    public static final Locale SPANISH = new Locale("es");

    private static final String BUNDLE_BASE_NAME = "vaadin-i18n.translations";

    @Override
    public List<Locale> getProvidedLocales() {
        return List.of(ENGLISH, SPANISH);
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        if (key == null) {
            return "";
        }
        ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE_NAME, locale);
        try {
            String value = bundle.getString(key);
            return params.length == 0 ? value : java.text.MessageFormat.format(value, params);
        } catch (MissingResourceException e) {
            return key;
        }
    }
}
