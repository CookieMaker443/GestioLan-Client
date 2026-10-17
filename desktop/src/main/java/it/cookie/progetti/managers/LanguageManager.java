package it.cookie.progetti.managers;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {
    private static LanguageManager instance;
    private ResourceBundle bundle;
    private SupportedLanguage current;

    public enum SupportedLanguage {
        /*GERMAN("de", "de-DE"),
        SPANISH("es", "es-ES"),
        FRENCH("fr", "fr-FR"),
        POLISH("pl", "pl-PL"),*/
        ITALIAN("it", "it-IT"),
        ENGLISH("en", "en-US");

        private final String code;
        private final String tag;

        SupportedLanguage(String code, String tag) {
            this.code = code;
            this.tag = tag;
        }

        public String getCode() { return code; }
        public String getTag() { return tag; }

        public static SupportedLanguage fromCode(String code) {
            for (SupportedLanguage l : values()) {
                if (l.code.equals(code)) return l;
            }
            return ENGLISH; // fallback
        }
    }

    public static LanguageManager getInstance() {
        if (instance == null) instance = new LanguageManager();
        return instance;
    }

    private LanguageManager() {
        // carica la lingua salvata nei settings
        load(SettingsManager.getInstance().getLanguage());
    }

    public final void load(String langCode) {
        current = SupportedLanguage.fromCode(langCode);
        Locale locale = Locale.forLanguageTag(current.getTag());
        bundle = ResourceBundle.getBundle("i18n.language", locale);

        System.out.println("Richiesto: " + langCode + " -> " + current
        + " | bundle locale: '" + bundle.getLocale() + "'");
    }

    public ResourceBundle getBundle() { return bundle; }

    public SupportedLanguage getCurrentLanguage() {
        return current;
    }
}