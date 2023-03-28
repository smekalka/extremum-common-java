package io.extremum.everything.services.fetch;

import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.common.model.PersistableCommonModel;
import io.extremum.common.annotation.ModelName;
import io.extremum.everything.collection.CollectionElementType;
import io.extremum.everything.collection.CollectionFragment;
import io.extremum.everything.collection.Projection;
import io.extremum.everything.dao.UniversalDao;
import io.extremum.everything.exceptions.EverythingEverythingException;
import io.extremum.everything.services.collection.FetchByOwnedCoordinates;
import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.descriptor.StandardStorageType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author rpuch
 */
@ExtendWith(MockitoExtension.class)
class FetchByOwnedCoordinatesTest {
    @InjectMocks
    private FetchByOwnedCoordinates fetcher;

    @Mock
    private UniversalDao universalDao;

    private static final ObjectId OBJECT_ID1 = new ObjectId();
    private static final ObjectId OBJECT_ID2 = new ObjectId();

    @Test
    void whenEverythingIsOk_thenCollectionShouldBeReturned() {
        whenRetrieveByIdsThenReturn2Houses();

        CollectionFragment<Model> houses = fetcher.fetch(new Street(), "houses", Projection.empty());
        assertThat(houses.elements(), hasSize(2));
    }

    private void whenRetrieveByIdsThenReturn2Houses() {
        when(universalDao.retrieveByIds(any(), any(), any()))
                .thenReturn(CollectionFragment.forCompleteCollection(Arrays.asList(new House(), new House())));
    }

    @Test
    void whenCollectionElementIsAnnotatedOnGetter_thenCollectionShouldBeReturned() {
        whenRetrieveByIdsThenReturn2Houses();

        CollectionFragment<Model> houses = fetcher.fetch(new Street(),
                "collectionElementOnGetter", Projection.empty());
        assertThat(houses.elements(), hasSize(2));
    }

    @Test
    void whenGetterIsNotFound_thenAnExceptionShouldBeThrown() {
        try {
            fetcher.fetch(new Street(), "noSuchField", Projection.empty());
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("No method 'getNoSuchField' was found in class" +
                    " 'class io.extremum.everything.services.fetch.FetchByOwnedCoordinatesTest$Street'"));
        }
    }

    @Test
    void whenFieldContentsIsNull_thenAnEmptyListShouldBeReturned() {
        Street host = new Street();
        host.houses = null;

        CollectionFragment<Model> houses = fetcher.fetch(host, "houses", Projection.empty());

        assertThat(houses.elements(), hasSize(0));
    }

    @Test
    void whenFieldContentsIsNotACollection_thenAnExceptionShouldBeThrown() {
        try {
            fetcher.fetch(new Street(), "name", Projection.empty());
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(),
                    is("'name' attribute on 'Street' contains 'class java.lang.String' and not a Collection"));
        }
    }

    @Test
    void whenFieldContentsIsAnEmptyCollection_thenAnEmptyListShouldBeReturned() {
        CollectionFragment<Model> fragment = fetcher.fetch(new Street(), "emptyList", Projection.empty());
        assertThat(fragment.elements(), hasSize(0));
    }

    @Test
    void whenNoElementTypeIsAnnotated_thenAnExceptionShouldBeThrown() {
        try {
            fetcher.fetch(new Street(), "noElementType", Projection.empty());
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(),
                    is("For host type 'Street' attribute 'noElementType' does not contain " +
                            "@CollectionElementType annotation"));
        }
    }

    @Test
    void whenIdFieldIsFoundMoreThanOnceOnCollectionElementClass_thenAnExceptionShouldBeThrown() {
        try {
            fetcher.fetch(new OwnerWithCollectionWith2Ids(), "items", Projection.empty());
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("'class" +
                    " io.extremum.everything.services.fetch.FetchByOwnedCoordinatesTest$Has2Ids' defines" +
                    " 2 @Id fields, must be exactly 1"));
        }
    }

    @Test
    void whenFieldIsOnASuperclass_thenItShouldWorkAsWell() {
        whenRetrieveByIdsThenReturn2Houses();

        CollectionFragment<Model> houses = fetcher.fetch(new SubStreet(), "houses", Projection.empty());
        assertThat(houses.elements(), hasSize(2));
    }

    @Test
    void whenCollectionFieldContainsModels_thenTheyShouldBeReturned() {
        CollectionFragment<Model> houses = fetcher.fetch(new Street(), "bareHouses", Projection.empty());
        assertThat(houses.elements(), hasSize(2));
    }

    @Test
    void givenCollectionContainsModelObjects_whenDateFilteringIsEnabled_thenOnlyMatchingElementsShouldBeReturned() {
        Street street = new Street();
        street.bareHouses = Arrays.asList(new House(), new House());
        ZonedDateTime somewhereIn2000 = LocalDate.of(2000, Month.JANUARY, 1).atStartOfDay(ZoneId.systemDefault());
        street.bareHouses.get(0).setCreated(somewhereIn2000);
        street.bareHouses.get(1).setCreated(ZonedDateTime.now());

        ZonedDateTime somewhereIn2010 = somewhereIn2000.plusYears(10);
        ZonedDateTime somewhereIn2100 = somewhereIn2000.plusYears(100);
        Projection projection = Projection.sinceUntil(somewhereIn2010, somewhereIn2100);

        CollectionFragment<Model> houses = fetcher.fetch(street, "bareHouses", projection);
        assertThat(houses.elements(), hasSize(1));
    }

    @Test
    void givenCollectionContainsModelObjects_whenLimitIsSpecified_thenLimitShouldBeRespected() {
        Projection projection = Projection.offsetLimit(0, 1);

        CollectionFragment<Model> houses = fetcher.fetch(new Street(), "bareHouses", projection);
        assertThat(houses.elements(), hasSize(1));
    }

    @Test
    void givenCollectionContainsModelObjects_whenDeletedFlagIsOn_thenElementShouldNotBeReturned() {
        Street street = new Street();
        street.bareHouses.get(0).setDeleted(true);

        CollectionFragment<Model> houses = fetcher.fetch(street, "bareHouses", Projection.empty());
        assertThat(houses.elements(), hasSize(street.bareHouses.size() - 1));
    }

    @Test
    void givenModelStoregaTypeIsNotMongo_whenFetchingCollectionViaIds_thenAnExceptionShouldBeThrown() {
        Street model = new Street();
        model.setUuid(Descriptor.builder().storageType(StandardStorageType.ELASTICSEARCH).build());

        try {
            fetcher.fetch(model, "houses", Projection.empty());
            fail("An exception should be thrown");
        } catch (IllegalStateException e) {
            assertThat(e.getMessage(), is("Only Mongo models can use IDs to fetch collections, " +
                    "but it was 'elastic' on 'Street', attribute 'houses'"));
        }
    }

    @Test
    void givenModelIsAProxyThatDoesNotStoreDataInOurFields_whenFetchingCollection_thenGetterShouldBeUsed() {
        PersistableCommonModel<?> model = new AProxy$HibernateProxy$ThatIgnoresTheFieldValue();

        CollectionFragment<Model> houses = fetcher.fetch(model, "houses", Projection.empty());
        assertThat(houses.elements(), hasSize(2));
    }

    @Test
    void givenModelClassHasCollectionElementTypeAnnotatedBothOnFieldAndGetter_whenFetchingCollection_thenAnExceptionShouldBeThrown() {
        PersistableCommonModel<?> model = new HasElementTypeAnnotatedTwice();

        try {
            fetcher.fetch(model, "houses", Projection.empty());
            fail("An exception should be thrown");
        } catch (EverythingEverythingException e) {
            assertThat(e.getMessage(), is("For host type 'HasElementTypeAnnotatedTwice' attribute 'houses'" +
                    " has @CollectionElementType annotation on both field and getter"));
        }
    }

    @Test
    void givenCollectionElementsAreBasicModels_whenFetchingCollection_thenTheCollectionShouldBeReturned() {
        CollectionFragment<Model> items = fetcher.fetch(new HasCollectionOfBasicModel(), "items",
                Projection.empty());

        assertThat(items.elements(), hasSize(2));
        assertThat(items.total(), is(OptionalLong.of(2)));

        Iterator<Model> iterator = items.elements().iterator();
        ABasicModel first = (ABasicModel) iterator.next();
        ABasicModel second = (ABasicModel) iterator.next();
        assertThat(first.getName(), is("first"));
        assertThat(second.getName(), is("second"));
    }

    @Test
    void givenCollectionElementsAreNotBasicModels_whenFetchingCollection_thenTheCollectionShouldBeReturned() {
        CollectionFragment<Model> items = fetcher.fetch(new HasCollectionOfNonBasicModel(), "items",
                Projection.empty());

        assertThat(items.elements(), hasSize(2));
        assertThat(items.total(), is(OptionalLong.of(2)));

        Iterator<Model> iterator = items.elements().iterator();
        NonBasicModel first = (NonBasicModel) iterator.next();
        NonBasicModel second = (NonBasicModel) iterator.next();
        assertThat(first.getName(), is("first"));
        assertThat(second.getName(), is("second"));
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
        private List<String> noElementType = Arrays.asList(OBJECT_ID1.toString(), OBJECT_ID2.toString());
        private List<Object> emptyList = new ArrayList<>();
        private List<House> bareHouses = Arrays.asList(new House(), new House());
        @Getter(onMethod_ = {@CollectionElementType(House.class)})
        private List<String> collectionElementOnGetter = Arrays.asList(OBJECT_ID1.toString(), OBJECT_ID2.toString());
    }

    @ModelName("Substreet")
    private static class SubStreet extends Street {
    }

    @ModelName("Has2Ids")
    @Getter
    public static class Has2Ids extends MongoCommonModel {
        @Id
        private String id2;
    }

    @ModelName("Owner")
    @Getter
    public static class OwnerWithCollectionWith2Ids extends MongoCommonModel {
        @CollectionElementType(Has2Ids.class)
        private List<String> items = Arrays.asList(OBJECT_ID1.toString(), OBJECT_ID2.toString());
    }

    @ModelName("Proxied")
    public static class AProxy$HibernateProxy$ThatIgnoresTheFieldValue extends MongoCommonModel {
        private List<House> houses = new ArrayList<>();

        public List<House> getHouses() {
            return Arrays.asList(new House(), new House());
        }
    }

    @ModelName("HasElementTypeAnnotatedTwice")
    public static class HasElementTypeAnnotatedTwice extends MongoCommonModel {
        @CollectionElementType(House.class)
        private List<String> houses = Arrays.asList(OBJECT_ID1.toString(), OBJECT_ID2.toString());

        @CollectionElementType(House.class)
        public List<String> getHouses() {
            return houses;
        }
    }

    @ModelName("ABasicModel")
    @RequiredArgsConstructor
    @Getter
    @Setter
    public static class ABasicModel implements BasicModel<String> {
        private Descriptor uuid;
        private String iri;
        private String id;
        private final String name;
    }

    @Getter
    public static class HasCollectionOfBasicModel extends MongoCommonModel {
        private List<ABasicModel> items = Arrays.asList(new ABasicModel("first"), new ABasicModel("second"));
    }

    @ModelName("NonBasicModel")
    @RequiredArgsConstructor
    @Getter
    public static class NonBasicModel implements Model {
        private final String name;
    }

    @Getter
    public static class HasCollectionOfNonBasicModel extends MongoCommonModel {
        private List<NonBasicModel> items = Arrays.asList(new NonBasicModel("first"), new NonBasicModel("second"));
    }
}