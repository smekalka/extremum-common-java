package io.extremum.common.model;

import io.extremum.sharedmodels.basic.MultilingualLanguage;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CollectionFilter {
    private final String celExpr;
    private final MultilingualLanguage locale;
}
