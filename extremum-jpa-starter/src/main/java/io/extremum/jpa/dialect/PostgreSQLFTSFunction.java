package io.extremum.jpa.dialect;

import java.util.List;
import org.hibernate.QueryException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.BooleanType;
import org.hibernate.type.Type;

public class PostgreSQLFTSFunction implements SQLFunction {

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public boolean hasParenthesesIfNoArguments() {
        return false;
    }

    @Override
    public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException {
        return new BooleanType();
    }

    public String render(Type firstArgumentType, List args, SessionFactoryImplementor factory) throws QueryException {
        if (args == null || args.size() < 2) {
            throw new IllegalArgumentException("The function must be passed at least 2 arguments");
        }

        String fragment;
        String ftsConfiguration;
        String ftsConfiguration2;
        String field;
        String value;

        if (args.size() == 4) {
            ftsConfiguration = (String) args.get(0);
            field = (String) args.get(1);
            ftsConfiguration2 = (String) args.get(2);
            value = (String) args.get(3);
            fragment = "to_tsvector(" + ftsConfiguration + "::regconfig, " + field + ") @@ " + "to_tsquery(" + ftsConfiguration2 + "::regconfig, " + value + ")";

        } else {
            field = (String) args.get(0);
            value = (String) args.get(1);
            fragment = "to_tsvector(" + field + ") @@ " + "to_tsquery(" + value + ")";
        }

        return fragment;
    }
}