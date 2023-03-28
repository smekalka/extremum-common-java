package io.extremum.common.secondaryds;

import io.extremum.common.annotation.SecondaryDatasource;

public final class ModelOnSecondaryDatasourceFilter extends ModelAnnotatedFilter {
    public ModelOnSecondaryDatasourceFilter() {
        super(SecondaryDatasource.class);
    }
}
