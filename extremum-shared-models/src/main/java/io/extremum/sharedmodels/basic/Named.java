package io.extremum.sharedmodels.basic;

import io.extremum.sharedmodels.annotation.DocumentationName;

/**
 * @author rpuch
 */
@DocumentationName("Named")
public interface Named extends Described {
    /**
     * The user-friendly and URL-valid name of the data element.
     */
    String getSlug();

    void setSlug(String slug);

    /**
     * The display name of the object.
     */
    StringOrMultilingual getName();

    void setName(StringOrMultilingual name);
}
