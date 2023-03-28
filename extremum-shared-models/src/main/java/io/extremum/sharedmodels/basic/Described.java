package io.extremum.sharedmodels.basic;

import io.extremum.sharedmodels.annotation.DocumentationName;

@DocumentationName("Described")
public interface Described {
    /**
     * The value of the attribute given in current locale determined by data operation environment.
     */
    StringOrMultilingual getDescription();

    void setDescription(StringOrMultilingual description);
}
