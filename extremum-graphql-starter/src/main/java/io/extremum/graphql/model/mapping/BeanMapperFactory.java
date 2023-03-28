package io.extremum.graphql.model.mapping;

import com.github.dozermapper.core.DozerBeanMapperBuilder;
import com.github.dozermapper.core.Mapper;
import com.github.dozermapper.core.loader.api.BeanMappingBuilder;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.basic.StringOrObject;
import lombok.Getter;

import static com.github.dozermapper.core.loader.api.FieldsMappingOptions.customConverter;
import static com.github.dozermapper.core.loader.api.TypeMappingOptions.mapNull;


public class BeanMapperFactory {

    @Getter
    private final Mapper mapper;

    public BeanMapperFactory() {
        mapper = initMapper();
    }


    private Mapper initMapper() {
        Mapper mapper = DozerBeanMapperBuilder.create()
                .withCustomFieldMapper((source, destination, sourceFieldValue, classMap, fieldMapping) ->
                        sourceFieldValue == null
                )
                .withMappingBuilder(stringOrObjectBeanMapperBuilder())
                .withMappingBuilder(stringOrMultilingualBeanMapperBuilder())
                .build();

        return mapper;
    }

    private BeanMappingBuilder stringOrObjectBeanMapperBuilder() {
        return new BeanMappingBuilder() {
            protected void configure() {
                mapping(
                        StringOrObject.class,
                        StringOrObject.class,
                        mapNull(false)
                )
                        .fields("object", "object",
                                customConverter(StringOrObjectMapper.class.getName())
                        );
            }
        };
    }

    private BeanMappingBuilder stringOrMultilingualBeanMapperBuilder() {
        return new BeanMappingBuilder() {
            protected void configure() {
                mapping(
                        StringOrMultilingual.class,
                        StringOrMultilingual.class,
                        mapNull(false)
                )
                        .fields("multilingualContent", "multilingualContent",
                                customConverter(MultilingualMapper.class.getName())
                        );
            }
        };
    }
}
