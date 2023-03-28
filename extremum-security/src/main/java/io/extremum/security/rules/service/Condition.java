package io.extremum.security.rules.service;

import io.extremum.sharedmodels.basic.MultilingualLanguage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Condition {
    private String nested;
    private String field;
    private Object value;
    private MultilingualLanguage locale;
    private boolean anyLocale;

    public Condition(List<Object> arguments, List<String> attributeNameChain, MultilingualLanguage locale) {
        this.locale = locale;
        switch (arguments.size()) {
            case 2:
                value = arguments.get(0);
                locale = MultilingualLanguage.fromString((String) arguments.get(1));

                if (arguments.get(1).equals("*")) {
                    anyLocale = true;
                }

                if (locale == null && !anyLocale) {
                    value = arguments;
                } else {
                    this.locale = locale;
                }
                break;
            case 1:
                value = arguments.get(0);
                break;
            default:
                value = arguments;
                break;
        }
        if (attributeNameChain.size() == 2) {
            field = attributeNameChain.get(1);
            nested = attributeNameChain.get(0);
        } else if (attributeNameChain.size() == 1) {
            field = attributeNameChain.get(0);
        } else if (attributeNameChain.size() == 0) {
            field = null;
        } else {
            throw new IllegalStateException("Condition must have 0, 1 or 2 arguments");
        }
    }

    public boolean forNested() {
        return nested != null;
    }

    public boolean localized() {
        return locale != null || anyLocale;
    }
}
