package io.extremum.security.rules.parser.jackson;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ser.std.EnumSerializer;
import org.projectnessie.cel.common.types.pb.Checked;

import java.util.List;
import java.util.stream.Stream;

final class JacksonEnumDescription {

    private final String name;
    private final com.google.api.expr.v1alpha1.Type pbType;
    private final List<Enum<?>> enumValues;

    JacksonEnumDescription(JavaType javaType, EnumSerializer ser) {
        this.name = javaType.getRawClass().getName().replace('$', '.');
        this.enumValues = ser.getEnumValues().enums();
        this.pbType = Checked.checkedInt;
    }

    com.google.api.expr.v1alpha1.Type pbType() {
        return pbType;
    }

    Stream<JacksonEnumValue> buildValues() {
        return enumValues.stream().map(JacksonEnumValue::new);
    }
}
