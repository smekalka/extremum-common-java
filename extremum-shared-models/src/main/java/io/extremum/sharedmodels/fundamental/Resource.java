package io.extremum.sharedmodels.fundamental;

import io.extremum.sharedmodels.annotation.DocumentationName;
import io.extremum.sharedmodels.basic.StringOrMultilingual;

/**
 * @author rpuch
 */
@DocumentationName("Resource")
public interface Resource {
    /**
     * The type of the object as a Resource.
     */
    String getType();
    void setType(String type);

    /**
     * The status of the object as a Resource.
     * Allowed values: draft, active, hidden
     */
    String getStatus();
    void setStatus(String status);

    /**
     * The user-friendly and URL-valid name of the object as a Resource.
     */
    String getSlug();
    void setSlug(String slug);

    /**
     * The URI of the object as a Resource.
     *
     * format: uri
     */
    String getUri();
    void setUri(String uri);

    /**
     * The display name of the object as a Resource.
     */
    StringOrMultilingual getName();
    void setName(StringOrMultilingual name);

    /**
     * A brief description of the object as a Resource. Markdown is very welcome for formatting 🤗
     */
    StringOrMultilingual getDescription();
    void setDescription(StringOrMultilingual description);
}
