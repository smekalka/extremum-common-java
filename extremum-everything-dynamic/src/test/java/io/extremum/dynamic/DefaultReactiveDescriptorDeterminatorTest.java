package io.extremum.dynamic;

import io.extremum.dynamic.models.DynamicModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DefaultReactiveDescriptorDeterminatorTest {
    static SchemaMetaService schemaMetaService = new DefaultSchemaMetaService();
    static DefaultReactiveDescriptorDeterminator determinator = new DefaultReactiveDescriptorDeterminator(schemaMetaService);

    @BeforeAll
    static void beforeAll() {
        schemaMetaService.registerMapping(DynamicModel.MODEL_TYPE, "empty.schema.json", 1);
    }

    @Test
    void determineAsADynamicModelDescriptor_if_DescriptorIsForADynamicModel() {
        Descriptor descriptorForADynamicModel = Descriptor.builder()
                .internalId("i-id")
                .externalId("e-id")
                .modelType(DynamicModel.MODEL_TYPE)
                .build();

        boolean result = determinator.isDynamic(descriptorForADynamicModel).block();

        Assertions.assertTrue(result);
    }

    @Test
    void determineAsNotADynamicModelDescriptor_if_DescriptorIsNotForADynamicModel() {
        Descriptor descriptorForANonDynamicModel = Descriptor.builder()
                .internalId("i-id")
                .externalId("e-id")
                .modelType("NotDynamicModel")
                .build();

        boolean result = determinator.isDynamic(descriptorForANonDynamicModel).block();

        Assertions.assertFalse(result);
    }
}