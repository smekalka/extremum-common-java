package io.extremum.everything.services.collection;

import io.extremum.common.collection.conversion.CollectionMakeupRequest;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.sharedmodels.descriptor.CollectionDescriptor;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.fundamental.CollectionReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FillCollectionTopTest {
    @InjectMocks
    private FillCollectionTop filler;

    @Mock
    private EverythingCollectionService everythingCollectionService;

    @Mock
    private ResponseDto responseDto;

    private Descriptor collectionDescriptor = Descriptor.forCollection("external-id",
            CollectionDescriptor.forOwned(new Descriptor("host-id"), "items"));

    @Test
    void givenTopIsNotFilled_whenApplyingToCollection_thenTopAndCountShouldBeFilledFromEverythingCollectionService() {
        // given
        when(everythingCollectionService.fetchCollection(any(), any(), anyBoolean()))
                .thenReturn(CollectionFragment.forFragment(singletonList(responseDto), 10));

        CollectionReference<?> reference = CollectionReference.uninitialized();

        // when
        filler.applyToCollection(new CollectionMakeupRequest(reference, collectionDescriptor));

        // then
        assertThat(reference.getTop(), equalTo(singletonList(responseDto)));
        assertThat(reference.getCount(), is(10L));
    }

    @Test
    void givenTopIsFilled_whenApplyingToCollection_thenTopAndCountShouldNotBeChanged() {
        CollectionReference<?> reference = new CollectionReference<>(emptyList(), 20);

        filler.applyToCollection(new CollectionMakeupRequest(reference, collectionDescriptor));

        assertThat(reference.getTop(), equalTo(emptyList()));
        assertThat(reference.getCount(), is(20L));
    }

    @Test
    void givenTopIsNotFilled_whenApplyingToCollectionReactively_thenTopAndCountShouldBeFilledFromEverythingCollectionService() {
        // given
        when(everythingCollectionService.fetchCollectionReactively(any(), any(), anyBoolean()))
                .thenReturn(Mono.just(CollectionFragment.forFragment(singletonList(responseDto), 10)));

        CollectionReference<?> reference = CollectionReference.uninitialized();

        // when
        filler.applyToCollectionReactively(new CollectionMakeupRequest(reference, collectionDescriptor)).block();

        // then
        assertThat(reference.getTop(), equalTo(singletonList(responseDto)));
        assertThat(reference.getCount(), is(10L));
    }

    @Test
    void givenTopIsFilled_whenApplyingToCollectionReactively_thenTopAndCountShouldNotBeChanged() {
        CollectionReference<?> reference = new CollectionReference<>(emptyList(), 20);

        filler.applyToCollectionReactively(new CollectionMakeupRequest(reference, collectionDescriptor)).block();

        assertThat(reference.getTop(), equalTo(emptyList()));
        assertThat(reference.getCount(), is(20L));
    }
}