package io.extremum.security.rules.parser.jackson;

import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.google.api.expr.v1alpha1.Type;
import org.projectnessie.cel.common.types.ref.FieldGetter;
import org.projectnessie.cel.common.types.ref.FieldTester;
import org.projectnessie.cel.common.types.ref.FieldType;

final class JacksonFieldType extends FieldType {

    private final PropertyWriter propertyWriter;

    JacksonFieldType(
            Type type, FieldTester isSet, FieldGetter getFrom, PropertyWriter propertyWriter) {
        super(type, isSet, getFrom);
        this.propertyWriter = propertyWriter;
    }

    PropertyWriter propertyWriter() {
        return propertyWriter;
    }
}
