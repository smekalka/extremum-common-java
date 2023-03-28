package io.extremum.everything.services.fetch;

import io.extremum.sharedmodels.basic.Model;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.common.annotation.ModelName;
import io.extremum.everything.collection.CollectionElementType;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.services.collection.StreamByOwnedCoordinates;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class StreamByOwnedCoordinatesTest {
    @InjectMocks
    private StreamByOwnedCoordinates streamer;

    @Mock
    private UniversalDao universalDao;

    private static final ObjectId OBJECT_ID1 = new ObjectId();
    private static final ObjectId OBJECT_ID2 = new ObjectId();

    @Test
    void whenEverythingIsOk_thenCollectionShouldBeReturned() {
        whenRetrieveByIdsThenReturn2Houses();

        Flux<Model> houses = streamer.stream(new Street(), "houses", Projection.empty());
        assertThat(houses.toStream().collect(Collectors.toList()), hasSize(2));
    }

    private void whenRetrieveByIdsThenReturn2Houses() {
        when(universalDao.streamByIds(any(), any(), any()))
                .thenReturn(Flux.just(new House(), new House()));
    }

    @Test
    void whenCollectionElementIsAnnotatedOnGetter_thenCollectionShouldBeReturned() {
        whenRetrieveByIdsThenReturn2Houses();

        Flux<Model> houses = streamer.stream(new Street(),
                "collectionElementOnGetter", Projection.empty());
        assertThat(houses.toStream().collect(Collectors.toList()), hasSize(2));
    }

    @Test
    void whenFieldContentsIsNull_thenAnEmptyListShouldBeReturned() {
        Street host = new Street();
        host.houses = null;

        Flux<Model> houses = streamer.stream(host, "houses", Projection.empty());

        assertThat(houses.toStream().collect(Collectors.toList()), hasSize(0));
    }

    @Test
    void whenCollectionFieldContainsModels_thenTheyShouldBeReturned() {
        Flux<Model> houses = streamer.stream(new Street(), "bareHouses", Projection.empty());
        assertThat(houses.toStream().collect(Collectors.toList()), hasSize(2));
    }

    @ModelName("House")
    private static class House extends MongoCommonModel {
    }

    @ModelName("Street")
    @Getter
    public static class Street extends MongoCommonModel {
        private String name = "the name";
        @CollectionElementType(House.class)
        private List<String> houses = Arrays.asList(OBJECT_ID1.toString(), OBJECT_ID2.toString());
        private List<House> bareHouses = Arrays.asList(new House(), new House());
        @Getter(onMethod_ = {@CollectionElementType(House.class)})
        private List<String> collectionElementOnGetter = Arrays.asList(OBJECT_ID1.toString(), OBJECT_ID2.toString());
    }
}