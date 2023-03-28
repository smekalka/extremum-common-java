
package io.extremum.security.rules.parser.jackson;

import org.projectnessie.cel.common.types.ref.Val;

import static org.projectnessie.cel.common.types.IntT.intOf;

final class JacksonEnumValue {

    private final Val ordinalValue;
    private final Enum<?> enumValue;

    JacksonEnumValue(Enum<?> enumValue) {
        this.ordinalValue = intOf(enumValue.ordinal());
        this.enumValue = enumValue;
    }

    static String fullyQualifiedName(Enum<?> value) {
        return value.getClass().getName().replace('$', '.') + '.' + value.name();
    }

    String fullyQualifiedName() {
        return fullyQualifiedName(enumValue);
    }

    Val ordinalValue() {
        return ordinalValue;
    }
}
