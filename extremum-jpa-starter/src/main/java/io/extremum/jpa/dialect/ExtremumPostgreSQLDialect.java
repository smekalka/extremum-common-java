package io.extremum.jpa.dialect;

import org.hibernate.dialect.PostgreSQL9Dialect;

public class ExtremumPostgreSQLDialect extends PostgreSQL9Dialect {

    public ExtremumPostgreSQLDialect() {
        registerFunction("fts", new PostgreSQLFTSFunction());
    }
}