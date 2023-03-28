package io.extremum.everything.dao;

import io.extremum.everything.TestWithServices;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.starter.CommonConfiguration;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author rpuch
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = CommonConfiguration.class)
class SpringDataUniversalDaoTest extends TestWithServices {
    private static final ZonedDateTime YEAR_2000 = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
    private static final ZonedDateTime YEAR_3000 = YEAR_2000.plusYears(1000);

    @Autowired
    private MongoOperations mongoOperations;
    @Autowired
    private ReactiveMongoOperations reactiveMongoOperations;

    private MongoUniversalDao universalDao;

    private List<ObjectId> houseIds;
    private House house1;

    @BeforeEach
    void createHouses() {
        universalDao = new MongoUniversalDao(mongoOperations, reactiveMongoOperations);

        house1 = new House("1");
        House house2 = new House("2a");
        mongoOperations.save(house1);
        mongoOperations.save(house2);

        houseIds = Arrays.asList(house1.getId(), house2.getId());
    }

    @Test
    void givenTwoHousesExist_whenRetrievingByTheirIdsWithEmptyProjection_then2HousesShouldBeReturned() {
        CollectionFragment<House> retrievedHouses = universalDao.retrieveByIds(houseIds,
                House.class, Projection.empty());

        assertThat(retrievedHouses.elements(), hasSize(2));
        assertThat(retrievedHouses.total().orElse(1000), is(2L));
    }

    @Test
    void givenTwoHousesExist_whenRetrievingByTheirIdsWithOffset1_then1HouseShouldBeReturnedButTotalShouldBe2() {
        CollectionFragment<House> retrievedHouses = universalDao.retrieveByIds(houseIds,
                House.class, Projection.offsetLimit(1, 10));
        
        assertThat(retrievedHouses.elements(), hasSize(1));
        assertThat(retrievedHouses.total().orElse(1000), is(2L));
    }

    @Test
    void givenTwoHousesExist_whenStreamingByTheirIdsWithEmptyProjection_then2HousesShouldBeReturned() {
        Flux<House> retrievedHouses = universalDao.streamByIds(houseIds,
                House.class, Projection.empty());

        assertThat(retrievedHouses.toStream().collect(Collectors.toList()), hasSize(2));
    }

    @Test
    void givenTwoHousesExist_whenStreamingByTheirIdsWithOffset1_then1HouseShouldBeReturnedButTotalShouldBe2() {
        Flux<House> retrievedHouses = universalDao.streamByIds(houseIds,
                House.class, Projection.offsetLimit(1, 10));

        assertThat(retrievedHouses.toStream().collect(Collectors.toList()), hasSize(1));
    }

    @Test
    void modelCreatedBeforeSinceShouldNotBeFoundWhenRetrieving() {
        Projection projection = Projection.sinceUntil(YEAR_3000, null);

        CollectionFragment<House> houses = universalDao.retrieveByIds(singletonList(house1.getId()),
                House.class, projection);

        assertThat(houses.elements(), is(empty()));
    }

    @Test
    void modelCreatedExactlyAtSinceShouldBeFoundWhenRetrieving() {
        Projection projection = Projection.sinceUntil(house1.getCreated(), YEAR_3000);

        CollectionFragment<House> houses = universalDao.retrieveByIds(singletonList(house1.getId()),
                House.class, projection);

        assertThatFirstHouseIsFound(houses);
    }

    private void assertThatFirstHouseIsFound(CollectionFragment<House> houses) {
        assertThatFirstHouseIsFound(houses.elements());
    }

    private void assertThatFirstHouseIsFound(Collection<House> houses) {
        assertThat(houses, hasSize(1));
        assertThat(houses.iterator().next().getId(), equalTo(house1.getId()));
    }

    @Test
    void modelCreatedBetweenSinceAndUntilShouldBeFoundWhenRetrieving() {
        Projection projection = Projection.sinceUntil(YEAR_2000, YEAR_3000);

        CollectionFragment<House> houses = universalDao.retrieveByIds(singletonList(house1.getId()),
                House.class, projection);

        assertThatFirstHouseIsFound(houses);
    }

    @Test
    void modelCreatedExactlyAtUntilShouldNotBeFoundWhenRetrieving() {
        Projection projection = Projection.sinceUntil(YEAR_2000, house1.getCreated());

        CollectionFragment<House> houses = universalDao.retrieveByIds(singletonList(house1.getId()),
                House.class, projection);

        assertThat(houses.elements(), is(empty()));
    }

    @Test
    void modelCreatedAfterUntilShouldNotBeFoundWhenRetrieving() {
        Projection projection = Projection.sinceUntil(null, YEAR_2000);

        CollectionFragment<House> houses = universalDao.retrieveByIds(singletonList(house1.getId()),
                House.class, projection);

        assertThat(houses.elements(), is(empty()));
    }

    @Test
    void modelCreatedBeforeSinceShouldNotBeFoundWhenStreaming() {
        Projection projection = Projection.sinceUntil(YEAR_3000, null);

        List<House> houses = streamByFirstHouseIdWith(projection);

        assertThat(houses, is(empty()));
    }

    @Test
    void modelCreatedExactlyAtSinceShouldBeFoundWhenStreaming() {
        Projection projection = Projection.sinceUntil(house1.getCreated(), YEAR_3000);

        List<House> houses = streamByFirstHouseIdWith(projection);

        assertThatFirstHouseIsFound(houses);
    }

    @Test
    void modelCreatedBetweenSinceAndUntilShouldBeFoundWhenStreaming() {
        Projection projection = Projection.sinceUntil(YEAR_2000, YEAR_3000);

        List<House> houses = streamByFirstHouseIdWith(projection);

        assertThatFirstHouseIsFound(houses);
    }

    @Test
    void modelCreatedExactlyAtUntilShouldNotBeFoundWhenStreaming() {
        Projection projection = Projection.sinceUntil(YEAR_2000, house1.getCreated());

        List<House> houses = streamByFirstHouseIdWith(projection);

        assertThat(houses, is(empty()));
    }

    @Test
    void modelCreatedAfterUntilShouldNotBeFoundWhenStreaming() {
        Projection projection = Projection.sinceUntil(null, YEAR_2000);

        List<House> houses = streamByFirstHouseIdWith(projection);

        assertThat(houses, is(empty()));
    }

    @NotNull
    private List<House> streamByFirstHouseIdWith(Projection projection) {
        return universalDao.streamByIds(singletonList(house1.getId()), House.class, projection)
                .toStream().collect(Collectors.toList());
    }
}