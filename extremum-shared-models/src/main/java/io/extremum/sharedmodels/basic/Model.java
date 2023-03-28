package io.extremum.sharedmodels.basic;

import lombok.Getter;

import java.io.Serializable;

/**
 * Base interface for models
 */
public interface Model extends Serializable {
    default void copyServiceFieldsTo(Model to) {
        // nothing to copy here
    }

    @Getter
    enum FIELDS {
        created("@created"),
        modified("@modified"),
        version("@version"),
        model("@type"),
        deleted("deleted"),
        createdBy("@createdBy"),
        modifiedBy("@modifiedBy");

        FIELDS(String stringValue) {
            this.stringValue = stringValue;
        }

        private final String stringValue;
    }
}
