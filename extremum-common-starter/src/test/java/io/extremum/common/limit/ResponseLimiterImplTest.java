package io.extremum.common.limit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.mapper.MockedMapperDependencies;
import io.extremum.common.mapper.SystemJsonObjectMapper;
import io.extremum.sharedmodels.fundamental.CollectionReference;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

class ResponseLimiterImplTest {
    private ResponseLimiterImpl limiter;

    @BeforeEach
    void createLimiter() {
        ObjectMapper objectMapper = new SystemJsonObjectMapper(new MockedMapperDependencies());
        limiter = new ResponseLimiterImpl(1500, objectMapper);
    }

    @Test
    void givenCollectionHas3ElementsButOnlyOneFits_whenLimiting_then1ElementShouldBeLeft() {
        List<String> strings = Stream.of("a", "b", "c")
                .map(str -> StringUtils.repeat(str, 1000))
                .collect(Collectors.toList());
        CollectionReference<String> reference = new CollectionReference<>(strings);
        TestResponseDto dto = new TestResponseDto(reference);

        limiter.limit(dto);

        assertThat(dto.collection.getTop(), hasSize(1));
    }

    @Test
    void givenCollectionIsNotEmptyAndFirstElementExceedsTheLimit_whenLimiting_then1ElementStillShouldBeLeft() {
        List<String> strings = Stream.of("a", "b", "c")
                .map(str -> StringUtils.repeat(str, 10000))
                .collect(Collectors.toList());
        CollectionReference<String> reference = new CollectionReference<>(strings);
        TestResponseDto dto = new TestResponseDto(reference);

        limiter.limit(dto);

        assertThat(dto.collection.getTop(), hasSize(1));
    }

    @Test
    void givenCollectionIsEmpty_whenLimiting_thenCollectionShouldRemainEmpty() {
        CollectionReference<String> reference = new CollectionReference<>(emptyList());
        TestResponseDto dto = new TestResponseDto(reference);

        limiter.limit(dto);

        assertThat(dto.collection.getTop(), hasSize(0));
    }

    @Test
    void givenEveryCollectionElementExceedsTheLimit_whenLimiting_thenOneElementShouldBeStillRetained() {
        List<TestResponseDto> dtos = Stream.of(1, 2)
                .map(n -> nestedDtoWithTopOfSize3k())
                .collect(Collectors.toList());
        CollectionReference<TestResponseDto> outerReference = new CollectionReference<>(dtos);
        NestingResponseDto outerDto = new NestingResponseDto(outerReference);

        limiter.limit(outerDto);

        assertThat(outerDto.dtos.getTop(), hasSize(1));
    }

    @Test
    void givenCollectionTopIsNull_whenLimiting_thenNoExceptionShouldBeThrown() {
        CollectionReference<String> reference = CollectionReference.uninitialized();
        TestResponseDto dto = new TestResponseDto(reference);

        limiter.limit(dto);
    }

    @NotNull
    private TestResponseDto nestedDtoWithTopOfSize3k() {
        List<String> strings = Stream.of("a", "b", "c")
                .map(str -> StringUtils.repeat(str, 1000))
                .collect(Collectors.toList());
        CollectionReference<String> reference = new CollectionReference<>(strings);
        return new TestResponseDto(reference);
    }

    public static class TestResponseDto extends CommonResponseDto {
        public CollectionReference<String> collection;

        TestResponseDto(CollectionReference<String> collection) {
            this.collection = collection;
        }

        @Override
        public String getModel() {
            return "Test";
        }
    }

    public static class NestingResponseDto extends CommonResponseDto {
        public CollectionReference<TestResponseDto> dtos;

        NestingResponseDto(CollectionReference<TestResponseDto> dtos) {
            this.dtos = dtos;
        }

        @Override
        public String getModel() {
            return "Nesting";
        }
    }
}