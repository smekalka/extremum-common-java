package io.extremum.mapper.jackson.module;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.extremum.mapper.jackson.deserializer.NumberOrObjectDeserializers;
import io.extremum.mapper.jackson.deserializer.StringOrObjectDeserializers;
import io.extremum.mapper.jackson.modifier.NumberOrObjectTypeModifier;
import io.extremum.mapper.jackson.modifier.StringOrObjectTypeModifier;
import io.extremum.mapper.jackson.serializer.NumberOrObjectSerializers;
import io.extremum.mapper.jackson.serializer.StringOrObjectSerializers;

public class StringOrObjectModule extends SimpleModule {
    @Override
    public void setupModule(SetupContext context) {
        context.addSerializers(new StringOrObjectSerializers());
        context.addDeserializers(new StringOrObjectDeserializers());
        context.addTypeModifier(new StringOrObjectTypeModifier());

        context.addSerializers(new NumberOrObjectSerializers());
        context.addDeserializers(new NumberOrObjectDeserializers());
        context.addTypeModifier(new NumberOrObjectTypeModifier());
    }
}
