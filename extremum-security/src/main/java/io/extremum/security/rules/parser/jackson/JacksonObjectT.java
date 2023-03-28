package io.extremum.security.rules.parser.jackson;

import org.projectnessie.cel.common.types.ObjectT;
import org.projectnessie.cel.common.types.StringT;
import org.projectnessie.cel.common.types.ref.Val;

import static org.projectnessie.cel.common.types.Err.noSuchField;
import static org.projectnessie.cel.common.types.Err.noSuchOverload;
import static org.projectnessie.cel.common.types.Types.boolOf;

final class JacksonObjectT extends ObjectT {

    private JacksonObjectT(JacksonRegistry registry, Object value, JacksonTypeDescription typeDesc) {
        super(registry, value, typeDesc, typeDesc.type());
    }

    static JacksonObjectT newObject(
            JacksonRegistry registry, Object value, JacksonTypeDescription typeDesc) {
        return new JacksonObjectT(registry, value, typeDesc);
    }

    JacksonTypeDescription typeDesc() {
        return (JacksonTypeDescription) typeDesc;
    }

    JacksonRegistry registry() {
        return (JacksonRegistry) adapter;
    }

    @Override
    public Val isSet(Val field) {
        if (!(field instanceof StringT)) {
            return noSuchOverload(this, "isSet", field);
        }
        String fieldName = (String) field.value();

        if (!typeDesc().hasProperty(fieldName)) {
            return noSuchField(fieldName);
        }

        Object value = typeDesc().fromObject(value(), fieldName);

        return boolOf(value != null);
    }

    @Override
    public Val get(Val index) {
        if (!(index instanceof StringT)) {
            return noSuchOverload(this, "get", index);
        }
        String fieldName = (String) index.value();

        if (!typeDesc().hasProperty(fieldName)) {
            return noSuchField(fieldName);
        }

        Object v = typeDesc().fromObject(value(), fieldName);

        return registry().nativeToValue(v);
    }

    @Override
    public <T> T convertToNative(Class<T> typeDesc) {
        throw new UnsupportedOperationException();
    }
}
