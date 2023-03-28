package io.extremum.rdf.triple.service.converter;

import io.extremum.common.dto.converters.ConversionConfig;
import io.extremum.rdf.triple.controller.dto.TripleRequestDto;
import io.extremum.rdf.triple.controller.dto.TripleResponseDto;
import io.extremum.rdf.triple.dao.mongo.model.Triple;
import io.extremum.rdf.triple.service.IriResolver;
import io.extremum.sharedmodels.basic.StringOrObject;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripleConverterTest {

    @Mock
    private IriResolver iriResolver;

    private TripleConverter converter;

    @BeforeEach
    void setUp() {
        converter = new TripleConverter(iriResolver);
    }

    @Test
    void should_convert_from_request_properly() {
        Triple actual = converter.convertFromRequest(
                new TripleRequestDto(
                        "testSubject",
                        "testPredicate",
                        Arrays.asList("obj1", "obj2")
                )
        );

        Triple expected = new Triple("testSubject",
                "testPredicate",
                Arrays.asList("obj1", "obj2")
        );

        Assertions.assertTrue(new ReflectionEquals(expected).matches(actual));
    }

    @Test
    void should_convert_to_request_reactively() {
        StepVerifier
                .create(converter.convertToRequestReactively(
                        new Triple(
                                "testSubject",
                                "testPredicate",
                                Arrays.asList("obj1", "obj2")
                        ),
                        ConversionConfig.builder().expand(false).build())
                )
                .expectNext(
                        new TripleRequestDto(
                                "testSubject",
                                "testPredicate",
                                Arrays.asList("obj1", "obj2")
                        )
                )
                .verifyComplete();
    }

    @Test
    void should_get_request_dto_type() {
        Assertions.assertEquals(converter.getRequestDtoType(), TripleRequestDto.class);
    }

    @Test
    void should_convert_to_response_reactively() {
        StepVerifier
                .create(
                        converter.convertToResponseReactively(
                                new Triple(
                                        "testSubject",
                                        "testPredicate",
                                        Arrays.asList("obj1", "obj2")
                                ),
                                ConversionConfig.builder().expand(false).build())
                )
                .expectNextMatches(expected ->
                        new ReflectionEquals(expected).matches(
                                new TripleResponseDto(
                                        "testSubject",
                                        "testPredicate",
                                        Arrays.asList(new StringOrObject<>("obj1"), new StringOrObject<>("obj2"))
                                )
                        )
                )
                .verifyComplete();

        TestResponseDto resolvedByIri1 = new TestResponseDto("stringValue1", 123);
        TestResponseDto resolvedByIri2 = new TestResponseDto("stringValue2", 1234);

        when(iriResolver.resolve("obj1")).thenReturn(Mono.just(resolvedByIri1));
        when(iriResolver.resolve("obj2")).thenReturn(Mono.just(resolvedByIri2));

        StepVerifier
                .create(
                        converter.convertToResponseReactively(
                                new Triple(
                                        "testSubject",
                                        "testPredicate",
                                        Arrays.asList("obj1", "obj2")
                                ),
                                ConversionConfig.builder().expand(true).build())
                )
                .expectNextMatches(expected ->
                        new ReflectionEquals(expected).matches(
                                new TripleResponseDto(
                                        "testSubject",
                                        "testPredicate",
                                        Arrays.asList(new StringOrObject<>(resolvedByIri1),new StringOrObject<>( resolvedByIri2))
                                )
                        )
                )
                .verifyComplete();
    }


    @Getter
    @Setter
    @AllArgsConstructor
    private static class TestResponseDto extends CommonResponseDto {
        private String string;
        private Integer integer;

        @Override
        public String getModel() {
            return "TestModel";
        }
    }

    @Test
    void should_get_response_dto_type() {
        Assertions.assertEquals(converter.getResponseDtoType(), TripleResponseDto.class);
    }

    @Test
    void should_get_supported_model() {
        Assertions.assertEquals(converter.getSupportedModel(), "triple");

    }
}