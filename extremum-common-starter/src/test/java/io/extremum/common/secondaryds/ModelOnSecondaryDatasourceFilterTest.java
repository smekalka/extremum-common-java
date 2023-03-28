package io.extremum.common.secondaryds;

import org.junit.jupiter.api.Test;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelOnSecondaryDatasourceFilterTest {
    private final ModelOnSecondaryDatasourceFilter filter = new ModelOnSecondaryDatasourceFilter();
    private final MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();

    @Test
    void shouldMatchNonReactiveRepositoryWithModelAnnotatedAsSecondaryDatasource() throws Exception {
        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(
                SecondaryDSModelRepository.class.getName());

        assertTrue(filter.match(metadataReader, metadataReaderFactory));
    }

    @Test
    void shouldMatchReactiveRepositoryWithModelAnnotatedAsSecondaryDatasource() throws Exception {
        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(
                ReactiveSecondaryDSModelRepository.class.getName());

        assertTrue(filter.match(metadataReader, metadataReaderFactory));
    }

    @Test
    void shouldNotMatchNonRepositoryClass() throws Exception {
        MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(String.class.getName());

        assertFalse(filter.match(metadataReader, metadataReaderFactory));
    }
}