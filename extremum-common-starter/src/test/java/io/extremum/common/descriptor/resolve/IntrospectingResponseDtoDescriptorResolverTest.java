package io.extremum.common.descriptor.resolve;

import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.fundamental.CommonResponseDto;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntrospectingResponseDtoDescriptorResolverTest {
    @InjectMocks
    private IntrospectingResponseDtoDescriptorResolver responseDtoResolver;

    @Mock
    private ReactiveDescriptorResolver descriptorResolver;

    @Captor
    private ArgumentCaptor<List<Descriptor>> descriptorListCaptor;

    @Test
    void givenResponseDtoContainsDescriptors_whenResolvingTheDto_thenTheDescriptorsExternalIdsShouldBeResolved() {
        // given
        when(descriptorResolver.resolveExternalIds(any())).thenReturn(Mono.empty());

        Descriptor descriptor1 = new Descriptor("descriptor1");
        Descriptor descriptor2 = new Descriptor("descriptor2");
        Descriptor descriptor3 = new Descriptor("descriptor3");
        Descriptor descriptor4 = new Descriptor("descriptor4");

        TestResponseDto responseDto = new TestResponseDto();
        responseDto.setId(descriptor1);

        TestResponseDto childDto = new TestResponseDto();
        childDto.setId(descriptor2);

        responseDto.setChild(childDto);

        responseDto.setDescriptors(Arrays.asList(descriptor3, descriptor4));

        // when
        responseDtoResolver.resolveExternalIdsIn(responseDto).block();

        // then
        verify(descriptorResolver).resolveExternalIds(descriptorListCaptor.capture());
        assertThat(descriptorListCaptor.getValue(),
                containsInAnyOrder(descriptor1, descriptor2, descriptor3, descriptor4));
    }

    @Getter
    @Setter
    private static class TestResponseDto extends CommonResponseDto {
        private TestResponseDto child;
        private List<Descriptor> descriptors;

        @Override
        public String getModel() {
            return "TestModel";
        }
    }
}