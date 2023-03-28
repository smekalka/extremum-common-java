package io.extremum.sharedmodels.basic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * IETF language tags in the format defined by RFC 5646 (language-TERRITORY).
 */
public enum MultilingualLanguage {
    /**
     * Unknown language
     */
    unknown("unknown"),

    /**
     * English
     */
    en("en"),

    /**
     * English (Australia)
     */
    en_AU("en-AU"),

    /**
     * English (Belize)
     */
    en_BZ("en-BZ"),

    /**
     * English (Canada)
     */
    en_CA("en-CA"),

    /**
     * English (Caribbean)
     */
    en_CB("en-CB"),

    /**
     * English (United Kingdom)
     */
    en_GB("en-GB"),

    /**
     * English (Ireland)
     */
    en_IE("en-IE"),

    /**
     * English (Jamaica)
     */
    en_JM("en-JM"),

    /**
     * English (New Zealand)
     */
    en_NZ("en-NZ"),

    /**
     * English (Republic of the Philippines)
     */
    en_PH("en-PH"),

    /**
     * English (Trinidad and Tobago)
     */
    en_TT("en-TT"),

    /**
     * English (United States)
     */
    en_US("en-US"),

    /**
     * English (South Africa)
     */
    en_ZA("en-ZA"),

    /**
     * English (Zimbabwe)
     */
    en_ZW("en-ZW"),

    /**
     * German
     */
    de("de"),

    /**
     * German (Austria)
     */
    de_AT("de-AT"),

    /**
     * German (Switzerland)
     */
    de_CH("de-CH"),

    /**
     * German (Germany)
     */
    de_DE("de-DE"),

    /**
     * German (Liechtenstein)
     */
    de_LI("de-LI"),

    /**
     * German (Luxembourg)
     */
    de_LU("de-LU"),

    /**
     * French
     */
    fr("fr"),

    /**
     * French (Belgium)
     */
    fr_BE("fr-BE"),

    /**
     * French (Canada)
     */
    fr_CA("fr-CA"),

    /**
     * French (Switzerland)
     */
    fr_CH("fr-CH"),

    /**
     * French (France)
     */
    fr_FR("fr-FR"),

    /**
     * French (Luxembourg)
     */
    fr_LU("fr-LU"),

    /**
     * French (Principality of Monaco)
     */
    fr_MC("fr-MC"),

    /**
     * Russian
     */
    ru("ru"),

    /**
     * Russian (Russia)
     */
    ru_RU("ru-RU");

    private final String value;

    MultilingualLanguage(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MultilingualLanguage fromString(String value) {
        if (value != null) {
            for (MultilingualLanguage item : MultilingualLanguage.values()) {
                if (value.equalsIgnoreCase(item.getValue())) {
                    return item;
                }
            }
        }

        return null;
    }
}
