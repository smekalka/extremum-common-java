package io.extremum.common.dto.converters.services;

import io.extremum.common.dto.converters.*;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.common.annotation.ModelName;
import io.extremum.sharedmodels.dto.RequestDto;
import io.extremum.sharedmodels.dto.ResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class DtoConvertersCollectionTest {
    private static final String MODEL_NAME = "AModel";

    @Mock
    private FromRequestDtoConverter<Model, RequestDto> fromRequestConverter;
    @Mock
    private ToRequestDtoConverter<Model, RequestDto> toRequestConverter;
    @Mock
    private ToResponseDtoConverter<Model, ResponseDto> toResponseConverter;
    @Mock
    private ReactiveToResponseDtoConverter<Model, ResponseDto> reactiveToResponseDtoConverter;
    @Mock
    private ReactiveToRequestDtoConverter<Model, RequestDto> reactiveToRequestDtoConverter;

    private final DtoConverters emptyConverters = new DtoConvertersCollection(emptyList(), emptyList(), emptyList(),
            emptyList(), emptyList());

    @Test
    void givenThereIsFromRequestDtoConverter_whenFindingIt_thenItShouldBeFound() {
        when(fromRequestConverter.getSupportedModel()).thenReturn(MODEL_NAME);

        DtoConverters converters = new DtoConvertersCollection(
                singletonList(fromRequestConverter), emptyList(), emptyList(), emptyList(), emptyList()
        );

        assertThat(converters.findFromRequestDtoConverter(AModel.class).orElse(null),
                is(sameInstance(fromRequestConverter)));
    }

    @Test
    void givenThereIsNoFromRequestDtoConverter_whenFindingIt_thenEmptyShouldBeReturned() {
        assertThat(emptyConverters.findFromRequestDtoConverter(AModel.class).orElse(null), is(nullValue()));
    }

    @Test
    void givenThereIsToRequestDtoConverter_whenFindingIt_thenItShouldBeFound() {
        when(toRequestConverter.getSupportedModel()).thenReturn(MODEL_NAME);

        DtoConverters converters = new DtoConvertersCollection(
                emptyList(), singletonList(toRequestConverter), emptyList(), emptyList(), emptyList()
        );

        assertThat(converters.findToRequestDtoConverter(AModel.class).orElse(null),
                is(sameInstance(toRequestConverter)));
    }

    @Test
    void givenThereIsNoToRequestDtoConverter_whenFindingIt_thenEmptyShouldBeReturned() {
        assertThat(emptyConverters.findToRequestDtoConverter(AModel.class).orElse(null), is(nullValue()));
    }

    @Test
    void givenThereIsToResponseDtoConverter_whenFindingIt_thenItShouldBeFound() {
        when(toResponseConverter.getSupportedModel()).thenReturn(MODEL_NAME);

        DtoConverters converters = new DtoConvertersCollection(
                emptyList(), emptyList(), singletonList(toResponseConverter), emptyList(), emptyList()
        );

        assertThat(converters.findToResponseDtoConverter(AModel.class).orElse(null),
                is(sameInstance(toResponseConverter)));
    }

    @Test
    void givenThereIsNoToResponseDtoConverter_whenFindingIt_thenEmptyShouldBeReturned() {
        assertThat(emptyConverters.findToResponseDtoConverter(AModel.class).orElse(null), is(nullValue()));
    }

    @Test
    void givenThereIsReactiveToResponseDtoConverter_whenFindingIt_thenItShouldBeFound() {
        when(reactiveToResponseDtoConverter.getSupportedModel()).thenReturn(MODEL_NAME);

        DtoConverters converters = new DtoConvertersCollection(
                emptyList(), emptyList(), emptyList(), singletonList(reactiveToResponseDtoConverter), emptyList()
        );

        assertThat(converters.findReactiveToResponseDtoConverter(AModel.class).orElse(null),
                is(sameInstance(reactiveToResponseDtoConverter)));
    }

    @Test
    void givenThereIsNoReactiveToResponseDtoConverter_whenFindingIt_thenEmptyShouldBeReturned() {
        assertThat(emptyConverters.findReactiveToResponseDtoConverter(AModel.class).orElse(null), is(nullValue()));
    }

    @Test
    void givenThereIsReactiveToRequestDtoConverter_whenFindingIt_thenItShouldBeFound() {
        when(reactiveToRequestDtoConverter.getSupportedModel()).thenReturn(MODEL_NAME);

        DtoConverters converters = new DtoConvertersCollection(
                emptyList(), emptyList(), emptyList(), emptyList(), singletonList(reactiveToRequestDtoConverter)
        );

        assertThat(converters.findReactiveToRequestDtoConverter(AModel.class).orElse(null),
                is(sameInstance(reactiveToRequestDtoConverter)));
    }

    @Test
    void givenThereIsNoReactiveToRequestDtoConverter_whenFindingIt_thenEmptyShouldBeReturned() {
        assertThat(emptyConverters.findReactiveToRequestDtoConverter(AModel.class).orElse(null), is(nullValue()));
    }

    @ModelName(MODEL_NAME)
    private static class AModel extends MongoCommonModel {
    }
}